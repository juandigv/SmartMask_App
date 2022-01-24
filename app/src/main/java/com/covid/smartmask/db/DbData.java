package com.covid.smartmask.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

public class DbData extends DbHelper{

    Context context;

    public DbData(@Nullable Context context) {
        super(context);
        this.context = context;
    }

    public long insertDataSensor(int temperature, int co2, int tvoc, int temp_freq, int mic_freq, int resp_freq, int valid, int resp_type, float ratio){
        long id = 0;

        try {
            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("temperature", temperature);
            values.put("co2", co2);
            values.put("tvoc", tvoc);
            values.put("temp_freq", temp_freq);
            values.put("mic_freq", mic_freq);
            values.put("resp_freq", resp_freq);
            values.put("valid", valid);
            values.put("resp_type", resp_type);
            values.put("ratio", ratio);
            id = db.insert(TABLE_SENSOR, null, values);
            db.close();
        }catch (Exception ex){
            ex.toString();
        }
        return id;
    }
    public long insertDataOxi(int oxigen, int heart){
        long id = 0;

        try {
            DbHelper dbHelper = new DbHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("oxigen", oxigen);
            values.put("heartRate", heart);
            id = db.insert(TABLE_OXI, null, values);
            db.close();
        }catch (Exception ex){
            ex.toString();
        }
        return id;
    }
}
