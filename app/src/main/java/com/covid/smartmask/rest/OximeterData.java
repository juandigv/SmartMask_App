package com.covid.smartmask.rest;

import java.util.Date;

public class OximeterData {

    int id;
    String clientId;
    int oxigen;
    int heartRate;
    Date dataDate;

    public OximeterData(int id, String clientId, int oxigen, int heartRate, Date dataDate) {
        this.id = id;
        this.clientId = clientId;
        this.oxigen = oxigen;
        this.heartRate = heartRate;
        this.dataDate = dataDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getOxigen() {
        return oxigen;
    }

    public void setOxigen(int oxigen) {
        this.oxigen = oxigen;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }
}
