package com.covid.smartmask;
//Remove until you stop seeing this
import static com.covid.smartmask.DispositivosVinculados.EXTRA_DEVICE_ADDRESS;
import static com.covid.smartmask.DispositivosVinculados.EXTRA_DEVICE_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.covid.smartmask.db.DbData;
import com.covid.smartmask.db.DbHelper;
import com.covid.smartmask.dialog.DialogActivities;
import com.covid.smartmask.dialog.DialogOximetro;
import com.covid.smartmask.dialog.DialogTimer;
import com.covid.smartmask.dialog.DialogWarning;
import com.covid.smartmask.notification.AlarmReciever;
import com.covid.smartmask.rest.OximeterData;
import com.covid.smartmask.rest.PostModel;
import com.covid.smartmask.rest.PostRequestAPI;
import com.covid.smartmask.rest.SensorData;
import com.covid.smartmask.service.BluetoothMessageService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Console;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements DialogOximetro.DialogOximetroListener, DialogTimer.DialogTimerListener, DialogActivities.DialogActivitiesListener{

    public static String BASE = "https://smartmask-api.herokuapp.com/api/";
    public static String SENSORURL = BASE + "sensor";
    public static String OXIMETERURL = BASE + "oximeter";
    private TextView textBtName;
    private TextView textBtAddress;
    private TextView textTemp;
    private TextView textCO2;
    private TextView textTVOC;
    private Button btnActivity;
    private Button btnMsg;
    private FloatingActionButton fabOxi;
    private Button buttonSync;
    private LineChart chartData;
    private Thread thread;
    private boolean plotData = true;

    public static final int handlerState = 0;

    private DbData dbData;
    private Vibrator phoneVibrator;
    private String androidId;

    private int co2 = 0;
    private int tvoc = 0;
    private int temp_freq = 0;
    private int mic_freq  = 0;
    private int valid = 2;
    private int resp_type = 0;
    private int ratio = 0;
    private int temperature = 0;
    private int temp_msg_time = 300000;

    public static final int dangerCO2 = 600;
    public static final int dangerTVOC = 60;


    private Calendar calendarStart;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private String BtAddress;
    private String BtName;
    private Retrofit retrofit;

    boolean mBounded;
    BluetoothMessageService BtMsgService;
    ServiceConnection mConnection;
    static Intent btServiceIntent;


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannelExercise();
        createNotificationChannelWarning();

        DbHelper dbHelper =  DbHelper.getInstance(MainActivity.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        if(db != null){
           // Toast.makeText(MainActivity.this, "DB Created",Toast.LENGTH_LONG).show();
           Log.d("Database","DB was Created");

        }else {
            Log.d("Database","DB Creation Failed");
            Toast.makeText(MainActivity.this, "DB Creation Failed",Toast.LENGTH_LONG).show();
            if (Build.VERSION.SDK_INT >= 21) {
                finishAndRemoveTask();
            } else {
                finish();
            }
        }

        retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.30:4004/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        textBtName = findViewById(R.id.textBtName);
        textBtAddress = findViewById(R.id.textBtAddress);
        btnActivity = findViewById(R.id.btnActivity);
        btnMsg = findViewById(R.id.btnMsg);
        textTemp = findViewById(R.id.textTemp);
        textCO2 = findViewById(R.id.textCO2);
        textTVOC = findViewById(R.id.textTVOC);
        chartData = findViewById(R.id.chartData);
        fabOxi = findViewById(R.id.fabOxi);
        buttonSync = findViewById(R.id.buttonSync);


        androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        phoneVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        initializeChart();

        btnActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,ExerciseActivity.class);
                startActivity(i);
            }
        });
        btnMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogTimer dialogTimer = new DialogTimer();
                dialogTimer.show(getSupportFragmentManager(),"Timer");
            }
        });
        btnActivity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                int calendarStartHour = settings.getInt("calendarStartHour", -1);
                int calendarStartMinute = settings.getInt("calendarStartMinute", -1);
                int calendarEndHour = settings.getInt("calendarEndHour", -1);
                int calendarEndMinute = settings.getInt("calendarEndMinute", -1);
                DialogActivities dialogActivities= new DialogActivities(calendarStartHour, calendarStartMinute, calendarEndHour, calendarEndMinute);
                dialogActivities.show(getSupportFragmentManager(),"Horas de Actividades");
                return true;
            }
        });

        fabOxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOxigenDialog();
            }
        });
        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                consoleDatabase();
            }
        });
    }

    @SuppressLint("Range")
    private void consoleDatabase(){
        Runnable runnable = new Runnable(){
            public void run() {
                PostRequestAPI postRequestAPI = retrofit.create(PostRequestAPI.class);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Cursor c;
                Date cursorDate;
                String formattedList;
                List<Integer> syncedSensorList = new ArrayList<Integer>();
                dbData = new DbData(MainActivity.this);
                c = dbData.getReadableDatabase().rawQuery("SELECT * FROM "+DbHelper.TABLE_SENSOR + " WHERE synced = 0", null);

                if (c.moveToFirst()){
                    do {
                        try {
                            cursorDate = formatter.parse(c.getString(9)+"");

                            SensorData sensorData = new SensorData(c.getInt(0),androidId,c.getInt(1),c.getInt(2),c.getInt(3),c.getInt(4),c.getInt(5),c.getInt(6),c.getInt(7),c.getDouble(8),cursorDate);
                            syncedSensorList.add(sensorData.getId());

                            Call<SensorData> sensorCall = postRequestAPI.PostSensorData(sensorData);
                            sensorCall.enqueue(new Callback<SensorData>() {
                                @Override
                                public void onResponse(Call<SensorData> call, Response<SensorData> response) {
                                    Log.d("CRUD","Synced Sensor Data");
                                }

                                @Override
                                public void onFailure(Call<SensorData> call, Throwable t) {
                                   //
                                }
                            });
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } while(c.moveToNext());
                }
                c.close();
                formattedList = syncedSensorList.toString().replace("[","(").replace("]",")");
                for(Integer id: syncedSensorList) {
                    dbData.getWritableDatabase().execSQL("UPDATE " + DbHelper.TABLE_SENSOR + " SET synced = 1 WHERE id in " + formattedList);
                }
                dbData.close();

                dbData = new DbData(MainActivity.this);
                List<Integer> syncedOxiList = new ArrayList<Integer>();
                c = dbData.getReadableDatabase().rawQuery("SELECT * FROM "+DbHelper.TABLE_OXI + " WHERE synced = 0", null);
                if (c.moveToFirst()){
                    do {
                        try {
                            cursorDate = formatter.parse(c.getString(3)+"");


                            OximeterData oxiData = new OximeterData(c.getInt(0),androidId,c.getInt(1),c.getInt(2),cursorDate);
                            Call<OximeterData> sensorCall = postRequestAPI.PostOximeterData(oxiData);
                            syncedOxiList.add(oxiData.getId());
                            sensorCall.enqueue(new Callback<OximeterData>() {
                                @Override
                                public void onResponse(Call<OximeterData> call, Response<OximeterData> response) {
                                    Log.d("CRUD","Synced Oximeter Data");
                                }

                                @Override
                                public void onFailure(Call<OximeterData> call, Throwable t) {
                                    //
                                }
                            });
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } while(c.moveToNext());
                }
                c.close();
                formattedList = syncedOxiList.toString().replace("[","(").replace("]",")");
                dbData.getWritableDatabase().execSQL("UPDATE "+DbHelper.TABLE_OXI+" SET synced = 1 WHERE id in "+formattedList);
                dbData.close();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();

    }

    private void createNotificationChannelExercise() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "SmartMaskReminderChannel";
            String description = "Channel for Mask Alarm Exercises";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            NotificationChannel channel = new NotificationChannel("smartmaskExercise",name,importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,audioAttributes);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotificationChannelWarning() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "SmartMaskWarningChannel";
            String description = "Channel for Mask Alarm Warnings";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            NotificationChannel channel = new NotificationChannel("smartmaskWarning",name,importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,audioAttributes);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
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

    private void initializeChart(){

        chartData.getDescription().setEnabled(true);
        chartData.getDescription().setText("Smart Mask Data Plot");
        chartData.setTouchEnabled(true);
        chartData.setDragEnabled(true);
        chartData.setScaleEnabled(true);
        chartData.setDrawGridBackground(false);
        chartData.setPinchZoom(false);


        LineData data = new LineData();
        chartData.setData(data);

        // get the legend (only possible after setting data)
        Legend l = chartData.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        XAxis xl = chartData.getXAxis();
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        YAxis leftAxis = chartData.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = chartData.getAxisRight();
        rightAxis.setEnabled(false);

        chartData.getAxisLeft().setDrawGridLines(false);
        chartData.getXAxis().setDrawGridLines(false);
        chartData.setDrawBorders(false);

    }

    private LineDataSet createSet(String label, int color){
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    @Override
    public void saveValues(String oxigen, String heart) {

        if(!(oxigen.isEmpty() || heart.isEmpty())){
            dbData = new DbData(getApplicationContext());
            long id = dbData.insertDataOxi(Integer.parseInt(oxigen), Integer.parseInt(heart));
            dbData.close();
            if(id > 0){
                Log.d("Database Main","Data succesfully Added");
                Toast.makeText(getBaseContext(), "Información de Oximetro añadida con éxito", Toast.LENGTH_SHORT).show();
            }else{
                Log.d("Database Main","Failure Saving Data");
            }
        }else{
            Toast.makeText(getBaseContext(), "Datos no pueden estar vacios, intente nuevamente", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void updateTempTimer(int newMillisTime) {
        temp_msg_time = newMillisTime;
        Toast.makeText(getBaseContext(), "Se ha cambiado el tiempo para los datos de temperatura", Toast.LENGTH_LONG).show();
        String jsonString = String.format("{\"timeInterval\":%d}",temp_msg_time);
        BtMsgService.sendBtMessage(jsonString);
    }

    @Override
    public void saveActivityHours(TimePicker time_start, TimePicker time_end) {

        int start = (time_start.getHour()*60) +time_start.getMinute();
        int end = (time_end.getHour() *60)+time_end.getMinute();

        int difference = end - start;
        if (difference == 0 || difference < 0) {
            Toast.makeText(getBaseContext(), "Horas seleccionadas son inválidas, intente nuevamente", Toast.LENGTH_LONG).show();
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            int calendarStartHour = settings.getInt("calendarStartHour", -1);
            int calendarStartMinute = settings.getInt("calendarStartMinute", -1);
            int calendarEndHour = settings.getInt("calendarEndHour", -1);
            int calendarEndMinute = settings.getInt("calendarEndMinute", -1);
            DialogActivities dialogActivities= new DialogActivities(calendarStartHour, calendarStartMinute, calendarEndHour, calendarEndMinute);
            dialogActivities.show(getSupportFragmentManager(),"Horas de Actividades");
        }
        else {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("calendarStartHour", time_start.getHour());
            editor.putInt("calendarStartMinute", time_start.getMinute());
            editor.putInt("calendarEndHour", time_end.getHour());
            editor.putInt("calendarEndMinute", time_end.getMinute());
            editor.commit();
            setExerciseAlarms(true,time_start.getHour(),time_start.getMinute(),time_end.getHour(),time_end.getMinute());
        }
    }

    public void setExerciseAlarms(boolean showToasts, int calendarStartHour, int calendarStartMinute, int calendarEndHour, int calendarEndMinute){
        int start = (calendarStartHour*60) +calendarStartMinute;
        int end = (calendarEndHour *60)+calendarEndMinute;
        if(alarmManager == null){
            alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
        int numberOfAlarms = 0;
        for(int i = start; i<=end;i=i+180){
            numberOfAlarms++;
        }
        calendarStart = Calendar.getInstance();
        calendarStart.set(Calendar.HOUR_OF_DAY,calendarStartHour);
        calendarStart.set(Calendar.MINUTE, calendarStartMinute);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Intent intent = new Intent(this, AlarmReciever.class);
        for(int i = 0;i<=8;i++){
            pendingIntent = PendingIntent.getBroadcast(this,0,intent,i);
            alarmManager.cancel(pendingIntent);
        }
        for(int i = 0; i< numberOfAlarms; i++){
            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY,calendarStartHour+(3*i));
            calendar.set(Calendar.MINUTE, calendarStartMinute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            pendingIntent = PendingIntent.getBroadcast(this,0,intent,i);

            Log.d("Calendar","Setting Up Alarms");
            if(Calendar.getInstance().getTimeInMillis() - calendar.getTimeInMillis() < 0){
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,pendingIntent);
                if(showToasts){
                    Toast.makeText(getBaseContext(), "Activando alarma para "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
                }
              }else{
                if(showToasts){
                    Toast.makeText(getBaseContext(), "Desactivando alarma atrasada "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = getIntent();
        // MAC Address
        BtAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        BtName = intent.getStringExtra(EXTRA_DEVICE_NAME);

        textBtName.setText(BtName);
        textBtAddress.setText(BtAddress);
        if(!mBounded) {
            mConnection = new ServiceConnection() {
                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("Service BTMessages", "Service is disconnected");

                    mBounded = false;
                    BtMsgService = null;
                }

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("Service BTMessages", "Service is connected");
                    mBounded = true;
                    BluetoothMessageService.LocalBinder mLocalBinder = (BluetoothMessageService.LocalBinder) service;
                    BtMsgService = mLocalBinder.getBTServiceInstance();
                    BtMsgService.registerClient(getParent());

                    final Observer<Integer> co2Observer = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            textCO2.setText(integer.toString() + " ppm (CO2)");
                            addEntry(integer, 1);
                            if (integer > dangerCO2) {
                                showRemoveMaskDialog();
                            }
                        }
                    };

                    final Observer<Integer> tvocObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            textTVOC.setText(integer.toString() + " ppb (TVOC)");
                            addEntry(integer, 2);
                            if (integer > dangerTVOC) {
                                showRemoveMaskDialog();
                            }
                        }
                    };

                    final Observer<Integer> temperatureObserver = new Observer<Integer>() {
                        @Override
                        public void onChanged(Integer integer) {
                            textTemp.setText(integer.toString() + " °C");
                            addEntry(integer, 0);
                        }
                    };

                    final Observer<Boolean> serviceWorkingObserver = new Observer<Boolean>() {
                        @Override
                        public void onChanged(Boolean value) {
                            if (!value) {
                                Toast.makeText(getBaseContext(), "La Conexión fallo, intente conectarse nuevamente", Toast.LENGTH_LONG).show();
                                Intent intend = new Intent(MainActivity.this, DispositivosVinculados.class);
                                Log.d("Service", "Stopped or Lost Connection");
                                if (IsBTServiceRunning()) {
                                    stopService(btServiceIntent);
                                }
                                startActivity(intend);
                            }
                        }
                    };

                    BtMsgService.getCO2().observe(MainActivity.this, co2Observer);
                    BtMsgService.getTVOC().observe(MainActivity.this, tvocObserver);
                    BtMsgService.getTemperature().observe(MainActivity.this, temperatureObserver);
                    BtMsgService.getServiceWorking().observe(MainActivity.this, serviceWorkingObserver);
                }
            };
            if (!IsBTServiceRunning()) {
                btServiceIntent = new Intent(this, BluetoothMessageService.class);
                btServiceIntent.putExtra(EXTRA_DEVICE_NAME, BtAddress);
                btServiceIntent.putExtra(EXTRA_DEVICE_ADDRESS, BtName);
                startService(btServiceIntent);

                bindService(btServiceIntent, mConnection, BIND_AUTO_CREATE);
            }
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        int calendarStartHour = settings.getInt("calendarStartHour", -1);
        int calendarStartMinute = settings.getInt("calendarStartMinute", -1);
        int calendarEndHour = settings.getInt("calendarEndHour", -1);
        int calendarEndMinute = settings.getInt("calendarEndMinute", -1);
        Log.d("SharedPreferences",calendarStartHour+"");
        if(calendarStartHour == -1){
            DialogActivities dialogActivities= new DialogActivities(calendarStartHour, calendarStartMinute, calendarEndHour, calendarEndMinute);
            dialogActivities.show(getSupportFragmentManager(),"Horas de Actividades");
        }else{
            setExerciseAlarms(false, calendarStartHour, calendarStartMinute, calendarEndHour, calendarEndMinute);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            Log.d("BTBind","Unbinded");
            unbindService(mConnection);
            mBounded = false;
        }
    };



    private void addEntry(int value, int setNumber){
        LineData data = chartData.getData();

        if(data != null){

            ILineDataSet set0 = data.getDataSetByIndex(0);
            ILineDataSet set1 = data.getDataSetByIndex(1);
            ILineDataSet set2 = data.getDataSetByIndex(2);

            if (set0 == null) {
                set0 = createSet("temperature", Color.RED);
                data.addDataSet(set0);
            }
            if (set1 == null) {
                set1 = createSet("CO2 (1x10)", Color.GREEN);
                data.addDataSet(set1);
            }
            if (set2 == null) {
                set2 = createSet("TVOC", Color.BLUE);
                data.addDataSet(set2);
            }


            switch (setNumber){
                case 0:
                    data.addEntry(new Entry(set0.getEntryCount(), (int) value),0);
                    break;
                case 1:
                    data.addEntry(new Entry(set1.getEntryCount(), (int) (value/10)),1);
                    break;
                case 2:
                    data.addEntry(new Entry(set2.getEntryCount(), (int) value),2);
                    break;
            }
            data.notifyDataChanged();
            chartData.notifyDataSetChanged();
            chartData.setVisibleXRangeMaximum(10);
            chartData.moveViewToX(data.getEntryCount());

        }
    }

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
      //
    }
}