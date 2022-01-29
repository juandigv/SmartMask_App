package com.covid.smartmask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.covid.smartmask.db.DbData;
import com.covid.smartmask.db.DbHelper;
import com.covid.smartmask.dialog.DialogOximetro;
import com.covid.smartmask.dialog.DialogWarning;
import com.covid.smartmask.notification.AlarmExerciseReciever;
import com.covid.smartmask.service.BluetoothMessageService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ExerciseActivity extends AppCompatActivity implements DialogOximetro.DialogOximetroListener {
    private TextView textActTime;
    private TextView textActBtName;
    private TextView textActBtAddress;
    private TextView textFTemp;
    private TextView textFMic;
    private TextView textFResp;
    private TextView textValid;
    private TextView textTResp;
    private TextView textRatio;
    private TextView textTVOC;
    private TextView textCO2;
    private Button btnStart;
    private FloatingActionButton fabActOxi;
    private String androidId;
    private final int secondsUntilMessage = 20;
    private DbData dbData;
    private Vibrator phoneVibrator;
    private Boolean inExercise = false;
    private Boolean exerciseDone = false;
    private SharedPreferences settings;

    boolean mBounded;
    BluetoothMessageService BtMsgService;
    ServiceConnection mConnection;
    Intent btServiceIntent;

    private final long[] pattern1 = {0, 1000, 500, 1000, 500};
    private final long[] pattern2 = {0, 500, 500, 500, 1000, 500, 500, 500};
    private final String[] validDict = {"Concuerdan","Variación","Sin Concordancia"};
    private final String[] trepDict = {"Eupnea","Taquipnea","Bradipnea","Apnea"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        textActTime = findViewById(R.id.textActTime);
        textActBtName = findViewById(R.id.textActBtName);
        textActBtAddress = findViewById(R.id.textActBtAddress);
        textFTemp = findViewById(R.id.textFTemp);
        textFMic = findViewById(R.id.textFMic);
        textFResp = findViewById(R.id.textFResp);
        textValid = findViewById(R.id.textValid);
        textTResp = findViewById(R.id.textTResp);
        textRatio = findViewById(R.id.textRatio);
        textTVOC = findViewById(R.id.textTVOC);
        textCO2 = findViewById(R.id.textCO2);
        btnStart = findViewById(R.id.btnStart);
        fabActOxi = findViewById(R.id.fabActOxi);
        androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        phoneVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        DbHelper dbHelper = DbHelper.getInstance(ExerciseActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        if(db != null){
            // Toast.makeText(MainActivity.this, "DB Created",Toast.LENGTH_LONG).show();
            Log.d("Database Exercise","Database Created");
        }else {
            Log.d("Database Exercise","Database Failure");
            Toast.makeText(ExerciseActivity.this, "DB Creation Failed",Toast.LENGTH_LONG).show();
            finish();
        }


        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startExerciseTimer();
            }
        });

        fabActOxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOxigenDialog();
            }
        });
    }
    private void startExerciseTimer() {
        btnStart.setEnabled(false);
        inExercise = true;
        new CountDownTimer(300000, 1000) {

            public void onTick(long millisUntilFinished) {
                int seconds = (int)Math.floor(millisUntilFinished/1000);
                int minutes = (int)Math.floor(seconds/60);
                textActTime.setText( String.format("%02d:%02d",minutes,seconds-(minutes*60)));
                if(seconds % (secondsUntilMessage) == 0){
                    BtMsgService.sendBtMessage("{\"check\":1}");
                }
            }
            public void onFinish() {
                textActTime.setText("Completado!");
                phoneVibrator.vibrate(pattern1, -1);
                inExercise = false;
                exerciseDone = true;

                Intent intent = new Intent(ExerciseActivity.this, AlarmExerciseReciever.class);
                sendBroadcast(intent);

            }

        }.start();
    }

    public void showRemoveMaskDialog(){
        long[] pattern = {0, 500, 500, 500, 1000, 500, 500, 500};
        phoneVibrator.vibrate(pattern, -1);
        DialogWarning dialogWarning= new DialogWarning();
        dialogWarning.show(getSupportFragmentManager(),"Remover Máscara");
    }

    private void showOxigenDialog(){
        DialogOximetro dialogOximetro = new DialogOximetro();
        dialogOximetro.show(getSupportFragmentManager(),"Oximetro");
    }
    @Override
    public void saveValues(String oxigen, String heart) {

        if(!(oxigen.isEmpty() || heart.isEmpty())){
            dbData = new DbData(getApplicationContext());
            long id = dbData.insertDataOxi(Integer.parseInt(oxigen), Integer.parseInt(heart));
            dbData.close();
            if(id > 0){
                Log.d("Database","Data succesfully added");
                Toast.makeText(getBaseContext(), "Información de Oximetro añadida con éxito", Toast.LENGTH_SHORT).show();
            }else{
                Log.d("Database","Failure saving Data");
            }
        }else{
            Toast.makeText(getBaseContext(), "Datos no pueden estar vacios, intente nuevamente", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("Activity Exercise","Resumed");

        Intent intent = getIntent();

        exerciseDone = !(intent.getBooleanExtra("Notification", false));

        if(!mBounded) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("Service BTMessages Exercise", "Service is disconnected");

                    mBounded = false;
                    BtMsgService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("Service BTMessages Exercise", "Service is connected");
                    mBounded = true;
                    BluetoothMessageService.LocalBinder mLocalBinder = (BluetoothMessageService.LocalBinder) service;
                    BtMsgService = mLocalBinder.getBTServiceInstance();
                    BtMsgService.registerClient(getParent());

                    final Observer<Integer> co2Observer = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textCO2.setText(integer.toString() + " ppm");
                            }
                            if (integer > settings.getInt("limit_CO2", 6500) ) {
                                showRemoveMaskDialog();
                            }
                        }
                    };

                    final Observer<Integer> tvocObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textTVOC.setText(integer.toString() + " ppb");
                            }
                            if (integer > settings.getInt("limit_TVOC", 800) ) {
                                showRemoveMaskDialog();
                            }
                        }
                    };

                    final Observer<Integer> temp_freqObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textFTemp.setText(integer.toString());
                            }
                        }
                    };

                    final Observer<Integer> mic_freqObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textFMic.setText(integer.toString());
                            }
                        }
                    };

                    final Observer<Integer> resp_freqObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textFResp.setText(integer.toString());
                            }
                        }
                    };

                    final Observer<Integer> validObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textValid.setText(validDict[integer]);
                            }
                        }
                    };

                    final Observer<Integer> resp_typeObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            if (inExercise) {
                                textTResp.setText(trepDict[integer]);
                            }
                        }
                    };

                    final Observer<Float> ratioObserver = new Observer<Float>() {
                        @Override
                        public void onChanged(Float aFloat) {
                            if (inExercise) {
                                textRatio.setText("1:" + aFloat.toString());
                            }
                        }
                    };
                    final Observer<Boolean> serviceWorkingObserver = new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean value) {
                            if (!value) {
                                Toast.makeText(getBaseContext(), "La Conexión fallo, intente conectarse nuevamente", Toast.LENGTH_LONG).show();
                                Intent intend = new Intent(ExerciseActivity.this, DispositivosVinculados.class);
                                Log.d("Service Exercise", "Stopped or Lost Connection");
                                if (IsBTServiceRunning()) {
                                    stopService(btServiceIntent);
                                }
                                startActivity(intend);
                            }
                        }
                    };
                    final Observer<String> btNameObserver = new Observer<String>() {
                        @Override
                        public void onChanged(String string) {
                            textActBtName.setText(string);
                        }
                    };
                    final Observer<String> btAddressObserver = new Observer<String>() {
                        @Override
                        public void onChanged(String string) {
                            textActBtAddress.setText(string);
                        }
                    };

                    BtMsgService.getCO2().observe(ExerciseActivity.this, co2Observer);
                    BtMsgService.getTVOC().observe(ExerciseActivity.this, tvocObserver);
                    BtMsgService.getTemp_freq().observe(ExerciseActivity.this, temp_freqObserver);
                    BtMsgService.getMic_freq().observe(ExerciseActivity.this, mic_freqObserver);
                    BtMsgService.getResp_freq().observe(ExerciseActivity.this, resp_freqObserver);
                    BtMsgService.getValid().observe(ExerciseActivity.this, validObserver);
                    BtMsgService.getResp_type().observe(ExerciseActivity.this, resp_typeObserver);
                    BtMsgService.getRatio().observe(ExerciseActivity.this, ratioObserver);
                    BtMsgService.getServiceWorking().observe(ExerciseActivity.this, serviceWorkingObserver);
                    BtMsgService.getServiceWorking().observe(ExerciseActivity.this, serviceWorkingObserver);
                    BtMsgService.getServiceWorking().observe(ExerciseActivity.this, serviceWorkingObserver);
                    BtMsgService.getBtName().observe(ExerciseActivity.this, btNameObserver);
                    BtMsgService.getBtAddress().observe(ExerciseActivity.this, btAddressObserver);

                }
            };
            Log.d("Activity Exercise", "Working i guess");
            if (IsBTServiceRunning()) {
                btServiceIntent = MainActivity.btServiceIntent;
                bindService(btServiceIntent, mConnection, BIND_AUTO_CREATE);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            Log.d("BTBound Exercise","Unbounded");
            unbindService(mConnection);
            mBounded = false;
        }
    };


    public boolean IsBTServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(BluetoothMessageService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed () {
        if(!exerciseDone){
            Toast.makeText(ExerciseActivity.this, "No puedes irte, aún no realizaste tus ejercicios",Toast.LENGTH_LONG).show();
        }else{
            finish();
        }
    }
}