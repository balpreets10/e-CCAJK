package com.mycca.models;

public class StatusModel {

    private long statusCode;
    private String statusString;

    public StatusModel(long statusCode, String statusString) {
        this.statusCode = statusCode;
        this.statusString = statusString;
    }

    public long getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(long statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }
}
