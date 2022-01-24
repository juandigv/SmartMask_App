package com.covid.smartmask.rest;

import com.google.gson.annotations.SerializedName;

public class PostModel {
    String title;

    @SerializedName("data")
    String bodyPost;

    SensorData sensorData;

    OximeterData oximeterData;


    public PostModel(String title, String bodyPost) {
        this.title = title;
        this.bodyPost = bodyPost;
    }

    public String getTitle() {
        return title;
    }

    public String getBodyPost() {
        return bodyPost;
    }

    public SensorData getSensorDataData() {
        return sensorData;
    }

    public OximeterData getOximeterData() {
        return oximeterData;
    }

}
