package com.covid.smartmask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class DispositivosVinculados extends AppCompatActivity {

    //Depuracion de LOGCAT
    private static final String TAG = "DispositivosVinculados";
    //Declaracion de ListView
    ListView IdLista;
    // String que se enviara a la actividad principal, main activity
    public static String EXTRA_DEVICE_NAME = "device_info";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    //Declaracion de campos

    // Comprueba que el dispositivo tiene Bluetooth y que está encendido.
    private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_vinculados);
    }

    @Override
    public void onResume() {
        super.onResume();
        VerificarEstadoBT();
        mPairedDevicesArrayAdapter = new ArrayAdapter(this, R.layout.dispositivos_encontrados);
        IdLista = (ListView) findViewById(R.id.IdLista);
        IdLista.setAdapter(mPairedDevicesArrayAdapter);
        IdLista.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set pairedDevices = mBtAdapter.getBondedDevices();
        BluetoothDevice device;
        if (pairedDevices.size() > 0) {
            for (Object pairDevice : pairedDevices) {
                device = (BluetoothDevice) pairDevice;
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString().replaceAll("[\\n\\t ]", "");
            String address = info.substring(info.length() - 17).replaceAll("[\\n\\t ]", "");
            String name = info.substring(0, info.length() - 17).replaceAll("[\\n\\t ]", "");

            if (name.equalsIgnoreCase("ESP32-FaceMask")) {
                finishAffinity();
                Intent intend = new Intent(DispositivosVinculados.this, MainActivity.class);
                intend.putExtra(EXTRA_DEVICE_NAME, name);
                intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intend);
            } else {
                Toast.makeText(DispositivosVinculados.this, "Este no es un dispositivo válido, intente con otro", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void VerificarEstadoBT() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(getBaseContext(), "Es necesario encender Bluetooth para usar la App. \nCerrando...", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        }
    }
}
