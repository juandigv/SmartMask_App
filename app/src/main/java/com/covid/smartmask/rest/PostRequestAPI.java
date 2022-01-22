package com.covid.smartmask.rest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PostRequestAPI {

    @POST("sensor")
    Call<SensorData> PostSensorData(@Body SensorData sensorData);


    @POST("oximeter")
    Call<OximeterData> PostOximeterData(@Body OximeterData oximeterData);
}
