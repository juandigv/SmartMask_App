package com.covid.smartmask.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper mInstance = null;
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "smartmask.db";
    public static final String TABLE_SENSOR = "t_smsensor";
    public static final String TABLE_OXI = "t_smoxi";

    public DbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DbHelper getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new DbHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_SENSOR + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "temperature INTEGER NOT NULL," +
                "co2 INTEGER NOT NULL," +
                "tvoc INTEGER NOT NULL," +
                "temp_freq INTEGER NOT NULL," +
                "mic_freq INTEGER NOT NULL," +
                "resp_freq INTEGER NOT NULL," +
                "valid INTEGER NOT NULL," +
                "resp_type INTEGER NOT NULL," +
                "ratio REAL NOT NULL," +
                "dataDate STRING NOT NULL DEFAULT (DATETIME('now','localtime'))," +
                "synced BIT NOT NULL DEFAULT 0" +
                ")");
        sqLiteDatabase.execSQL("CREATE TABLE " + TABLE_OXI + "(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "oxigen INTEGER NOT NULL," +
                "heartRate INTEGER NOT NULL," +
                "dataDate STRING NOT NULL DEFAULT (DATETIME('now','localtime'))," +
                "synced BIT NOT NULL DEFAULT 0" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_OXI);
        onCreate(sqLiteDatabase);
    }

}
