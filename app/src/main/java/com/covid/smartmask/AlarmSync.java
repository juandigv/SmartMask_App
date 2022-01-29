package com.covid.smartmask;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.covid.smartmask.db.DbData;
import com.covid.smartmask.db.DbHelper;
import com.covid.smartmask.rest.OximeterData;
import com.covid.smartmask.rest.PostRequestAPI;
import com.covid.smartmask.rest.SensorData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AlarmSync extends BroadcastReceiver {
    private Retrofit retrofit;
    private String dbURL;
    private Boolean sync;
    private DbData dbData;
    private String androidId;

    @SuppressLint("HardwareIds")
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        sync = settings.getBoolean("syncData", false);
        dbURL = settings.getString("syncURL", "");
        androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        Log.d("Syncing", sync + " " + dbURL);
        resetRetrofitBuilder();
        consoleDatabase(context);
    }

    private void resetRetrofitBuilder() {
        try {
            retrofit = new Retrofit.Builder()
                    .baseUrl(dbURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } catch (Exception e) {
            Log.e("Retrofit", e.toString());
        }
    }

    @SuppressLint("Range")
    private void consoleDatabase(Context context) {
        Runnable runnable = new Runnable() {
            public void run() {
                if (sync) {
                    try {
                        PostRequestAPI postRequestAPI = retrofit.create(PostRequestAPI.class);
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        Cursor c;
                        Date cursorDate;
                        String formattedList;
                        List<Integer> syncedSensorList = new ArrayList<>();
                        dbData = new DbData(context);
                        c = dbData.getReadableDatabase().rawQuery("SELECT * FROM " + DbHelper.TABLE_SENSOR + " WHERE synced = 0", null);

                        if (c.moveToFirst()) {
                            do {
                                try {
                                    cursorDate = formatter.parse(c.getString(10) + "");
                                    SensorData sensorData = new SensorData(c.getInt(0), androidId, c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4), c.getInt(5), c.getInt(6), c.getInt(7), c.getInt(8), c.getDouble(9), cursorDate);
                                    Call<SensorData> sensorCall = postRequestAPI.PostSensorData(sensorData);
                                    sensorCall.enqueue(new Callback<SensorData>() {
                                        @Override
                                        public void onResponse(Call<SensorData> call, Response<SensorData> response) {
                                            Log.d("CRUD", "Synced Sensor Data");
                                            dbData.getWritableDatabase().execSQL("UPDATE " + DbHelper.TABLE_SENSOR + " SET synced = 1 WHERE id = " + sensorData.getId());
                                        }

                                        @Override
                                        public void onFailure(Call<SensorData> call, Throwable t) {
                                            Log.d("CRUD", "Failed Sync Oximeter Data");
                                        }
                                    });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } while (c.moveToNext());
                        }
                        c.close();
                        dbData.close();

                        dbData = new DbData(context);
                        List<Integer> syncedOxiList = new ArrayList<Integer>();
                        c = dbData.getReadableDatabase().rawQuery("SELECT * FROM " + DbHelper.TABLE_OXI + " WHERE synced = 0", null);
                        if (c.moveToFirst()) {
                            do {
                                try {
                                    cursorDate = formatter.parse(c.getString(3) + "");
                                    OximeterData oxiData = new OximeterData(c.getInt(0), androidId, c.getInt(1), c.getInt(2), cursorDate);
                                    Call<OximeterData> sensorCall = postRequestAPI.PostOximeterData(oxiData);
                                    sensorCall.enqueue(new Callback<OximeterData>() {
                                        @Override
                                        public void onResponse(Call<OximeterData> call, Response<OximeterData> response) {
                                            Log.d("CRUD", "Synced Oximeter Data");
                                            dbData.getWritableDatabase().execSQL("UPDATE " + DbHelper.TABLE_OXI + " SET synced = 1 WHERE id = " + oxiData.getId());
                                        }

                                        @Override
                                        public void onFailure(Call<OximeterData> call, Throwable t) {
                                            Log.d("CRUD", "Failed Sync Oximeter Data");
                                            Log.e("CRUD", "" + t.getCause());
                                        }
                                    });
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } while (c.moveToNext());
                        }
                        c.close();
                        dbData.close();
                    } catch (Exception e) {
                        Log.e("Retrofit", e.toString());
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
