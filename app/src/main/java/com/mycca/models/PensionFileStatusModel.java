package com.mycca.models;

public class PensionFileStatusModel {

    private String hrNumber;
    private String pensionFileStatus;
    private String email;
    private String mobile;

    public PensionFileStatusModel() {
    }

    public PensionFileStatusModel(String hrNumber, String pensionFileStatus, String email, String mobile) {
        this.hrNumber = hrNumber;
        this.pensionFileStatus = pensionFileStatus;
        this.email = email;
        this.mobile = mobile;
    }

    public String getHrNumber() {
        return hrNumber;
    }

    public void setHrNumber(String hrNumber) {
        this.hrNumber = hrNumber;
    }

    public String getPensionFileStatus() {
        return pensionFileStatus;
    }

    public void setPensionFileStatus(String pensionFileStatus) {
        this.pensionFileStatus = pensionFileStatus;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
