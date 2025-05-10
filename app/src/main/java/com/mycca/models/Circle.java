package com.mycca.models;

public class Circle {
    private String code;
    private String en;
    private String hi;
    private String mails;
    private boolean active;

    public Circle() {
    }

    public Circle(String code, String en, String hi, String mails, boolean active) {
        this.code = code;
        this.en = en;
        this.hi = hi;
        this.mails = mails;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getHi() {
        return hi;
    }

    public void setHi(String hi) {
        this.hi = hi;
    }

    public String getMails() {
        return mails;
    }

    public void setMails(String mails) {
        this.mails = mails;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
