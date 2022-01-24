package com.covid.smartmask.rest;

import androidx.annotation.Nullable;

import java.util.Date;

public class SensorData {

    int id;
    String clientId;
    int temperature;
    int co2;
    int tvoc;
    int temp_freq;
    int mic_freq;
    int resp_freq;
    int valid;
    int resp_type;
    double ratio;
    Date dataDate;

    public SensorData(int id, String clientId, int temperature, int co2, int tvoc, int temp_freq, int mic_freq, int resp_freq, int valid, int resp_type, double ratio, Date dataDate) {
        this.id = id;
        this.clientId = clientId;
        this.temperature = temperature;
        this.co2 = co2;
        this.tvoc = tvoc;
        this.temp_freq = temp_freq;
        this.mic_freq = mic_freq;
        this.resp_freq = resp_freq;
        this.valid = valid;
        this.resp_type = resp_type;
        this.ratio = ratio;
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

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getCo2() {
        return co2;
    }

    public void setCo2(int co2) {
        this.co2 = co2;
    }

    public int getTvoc() {
        return tvoc;
    }

    public void setTvoc(int tvoc) {
        this.tvoc = tvoc;
    }

    public int getTemp_freq() {
        return temp_freq;
    }

    public void setTemp_freq(int temp_freq) {
        this.temp_freq = temp_freq;
    }

    public int getMic_freq() {
        return mic_freq;
    }

    public void setMic_freq(int mic_freq) {
        this.mic_freq = mic_freq;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public int getResp_type() {
        return resp_type;
    }

    public void setResp_type(int resp_type) {
        this.resp_type = resp_type;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public Date getDataDate() {
        return dataDate;
    }

    public void setDataDate(Date dataDate) {
        this.dataDate = dataDate;
    }

    public int getResp_freq() {
        return resp_freq;
    }

    public void setResp_freq(int resp_freq) {
        this.resp_freq = resp_freq;
    }

    @Override
    public String toString() {
        return "SensorData{" +
                "id=" + id +
                ", clientId='" + clientId + '\'' +
                ", temperature=" + temperature +
                ", co2=" + co2 +
                ", tvoc=" + tvoc +
                ", temp_freq=" + temp_freq +
                ", mic_freq=" + mic_freq +
                ", resp_freq=" + resp_freq +
                ", valid=" + valid +
                ", resp_type=" + resp_type +
                ", ratio=" + ratio +
                ", dataDate=" + dataDate +
                '}';
    }
}
