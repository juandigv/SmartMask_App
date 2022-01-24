package com.covid.smartmask.service;


import static com.covid.smartmask.DispositivosVinculados.EXTRA_DEVICE_ADDRESS;
import static com.covid.smartmask.DispositivosVinculados.EXTRA_DEVICE_NAME;
import static com.covid.smartmask.MainActivity.dangerCO2;
import static com.covid.smartmask.MainActivity.dangerTVOC;
import static com.covid.smartmask.MainActivity.handlerState;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.covid.smartmask.db.DbData;
import com.covid.smartmask.db.DbHelper;
import com.covid.smartmask.notification.AlarmWarningReciever;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothMessageService extends Service {

    private Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ConnectedThread myConexionBT;
    private DbData dbData;
    private String message = "";
    private JSONObject messageJSON;
    private String androidId;

    private int co2 = 0;
    private int tvoc = 0;
    private int temp_freq = 0;
    private int mic_freq = 0;
    private int resp_freq = 0;
    private int valid = 2;
    private int resp_type = 0;
    private float ratio = 0;
    private int temperature = 0;
    private int temp_msg_time = 300000;
    private String BtAddress;
    private String BtName;

    Callbacks activity;


    public MutableLiveData<Integer> co2LiveData;
    public MutableLiveData<Integer> tvocLiveData;
    public MutableLiveData<Integer> temperatureLiveData;
    public MutableLiveData<Integer> temp_freqLiveData;
    public MutableLiveData<Integer> resp_freqLiveData;
    public MutableLiveData<Integer> mic_freqLiveData;
    public MutableLiveData<Integer> validLiveData;
    public MutableLiveData<Integer> resp_typeLiveData;
    public MutableLiveData<Float> ratioLiveData;
    public MutableLiveData<Boolean> serviceWorkingLiveData;
    public MutableLiveData<String> btNameLiveData;
    public MutableLiveData<String> btAddressLiveData;

    IBinder mBinder = new LocalBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        co2LiveData = new MutableLiveData<>();
        tvocLiveData = new MutableLiveData<>();
        temperatureLiveData = new MutableLiveData<>();
        temp_freqLiveData = new MutableLiveData<>();
        resp_freqLiveData = new MutableLiveData<>();
        mic_freqLiveData = new MutableLiveData<>();
        validLiveData = new MutableLiveData<>();
        resp_typeLiveData = new MutableLiveData<>();
        ratioLiveData = new MutableLiveData<>();
        serviceWorkingLiveData = new MutableLiveData<>();
        btNameLiveData = new MutableLiveData<>();
        btAddressLiveData = new MutableLiveData<>();

        bluetoothIn = new Handler(new IncomingHandlerCallback());
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        BtName = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        BtAddress = intent.getStringExtra(EXTRA_DEVICE_NAME);
        BluetoothDevice device = btAdapter.getRemoteDevice(BtAddress);
        btNameLiveData.setValue(BtName);
        btAddressLiveData.setValue(BtAddress);

        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        DbHelper dbHelper = DbHelper.getInstance(getApplicationContext());
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        if (db != null) {
                            // Toast.makeText(MainActivity.this, "DB Created",Toast.LENGTH_LONG).show();
                            Log.d("Database BtService", "DB was Created");
                        } else {
                            Log.d("Database BtService", "DB Creation Failed");
                            Toast.makeText(getApplicationContext(), "DB Creation Failed", Toast.LENGTH_LONG).show();
                        }

                        try {
                            btSocket = createBluetoothSocket(device);
                        } catch (IOException e) {
                            Toast.makeText(getBaseContext(), "La creación del Socket fallo", Toast.LENGTH_LONG).show();
                        }
                        try {
                            btSocket.connect();
                        } catch (IOException e) {
                            try {
                                btSocket.close();
                            } catch (IOException e2) {
                            }
                        }
                        myConexionBT = new BluetoothMessageService.ConnectedThread(btSocket);
                        myConexionBT.start();

                        sendBtMessage("{\"temp\":1}");
                    }
                }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BluetoothMessageService getBTServiceInstance() {
            return BluetoothMessageService.this;
        }
    }

    public void sendBtMessage(String message) {
        myConexionBT.write(message);
    }


    private class IncomingHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            if (msg.what == handlerState) {
                if (msg.obj != null) {
                    if (message.contains("}")) {
                        message = message.replaceAll("[\\n\\t ]", "");
                        try {
                            messageJSON = new JSONObject(message);
                            co2 = Integer.parseInt(messageJSON.get("co2").toString());
                            tvoc = Integer.parseInt(messageJSON.get("tvoc").toString());
                            try {
                                temp_freq = Integer.parseInt(messageJSON.get("ftemp").toString());
                                temp_freqLiveData.setValue(temp_freq);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                mic_freq = Integer.parseInt(messageJSON.get("fmic").toString());
                                mic_freqLiveData.setValue(mic_freq);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                resp_freq = Integer.parseInt(messageJSON.get("fresp").toString());
                                resp_freqLiveData.setValue(resp_freq);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                valid = Integer.parseInt(messageJSON.get("val").toString());
                                validLiveData.setValue(valid);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                resp_type = Integer.parseInt(messageJSON.get("tresp").toString());
                                resp_typeLiveData.setValue(resp_type);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                ratio = Float.parseFloat(messageJSON.get("ratio").toString());
                                ratioLiveData.setValue(ratio);
                                Log.d("Service Message", "ratio: " + ratio);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            try {
                                temperature = Integer.parseInt(messageJSON.get("temp").toString());
                                temperatureLiveData.setValue(temperature);
                            } catch (Exception ex) {
                                ex.toString();
                            }

                            co2LiveData.setValue(co2);
                            tvocLiveData.setValue(tvoc);

                            Log.d("Service Message", "Temperature: " + temperature);
                            Log.d("Service Message", "co2: " + co2);
                            Log.d("Service Message", "tvoc: " + tvoc);

                            dbData = new DbData(getApplicationContext());
                            long id = dbData.insertDataSensor(temperature, co2, tvoc, temp_freq, mic_freq, resp_freq, valid, resp_type, ratio);
                            dbData.close();
                            if (id > 0) {
                                Log.d("Database BTService", "Data Entry Success");
                            } else {
                                Log.d("Database BTService", "Data Entry Failure");
                            }
                            if (co2 > dangerCO2 || tvoc > dangerTVOC) {
                                //showRemoveMaskDialog();
                                Log.d("NotificationWarning", "Remove Mask");
                                Intent intent = new Intent(getApplicationContext(), AlarmWarningReciever.class);
                                sendBroadcast(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        message = "";
                    } else {
                        message = message + (char) msg.obj;
                    }
                }
            }
            return true;
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    private void VerificarEstadoBT() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Toast.makeText(getBaseContext(), "Se necesita encender bluetooth", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] byte_in = new byte[1];
            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    mmInStream.read(byte_in);
                    char ch = (char) byte_in[0];
                    bluetoothIn.obtainMessage(handlerState, ch).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "La Conexión fallo, intente conectarse nuevamente", Toast.LENGTH_LONG).show();
                serviceWorkingLiveData.setValue(false);
            }
        }
    }

    public void registerClient(Activity activity) {
        this.activity = (Callbacks) activity;
    }

    public interface Callbacks {
        void updateClient(float data);
    }

    public MutableLiveData<Integer> getCO2() {
        return co2LiveData;
    }

    public MutableLiveData<Integer> getTVOC() {
        return tvocLiveData;
    }

    public MutableLiveData<Integer> getTemperature() {
        return temperatureLiveData;
    }

    public MutableLiveData<Integer> getTemp_freq() {
        return temp_freqLiveData;
    }

    public MutableLiveData<Integer> getMic_freq() {
        return mic_freqLiveData;
    }

    public MutableLiveData<Integer> getResp_freq() {
        return resp_freqLiveData;
    }

    public MutableLiveData<Integer> getValid() {
        return validLiveData;
    }

    public MutableLiveData<Integer> getResp_type() {
        return resp_typeLiveData;
    }

    public MutableLiveData<Float> getRatio() {
        return ratioLiveData;
    }

    public MutableLiveData<Boolean> getServiceWorking() {
        return serviceWorkingLiveData;
    }

    public MutableLiveData<String> getBtName() {
        return btNameLiveData;
    }

    public MutableLiveData<String> getBtAddress() {
        return btAddressLiveData;
    }
}
