package com.mycca.models;

public class PanAdhaar {

    private String identifierType;
    private String identifierNumber;
    private String cardNumOrUserName;
    private String filename;
    private String circle;

    public PanAdhaar() {
    }

    public PanAdhaar(String identifierType, String identifierNumber, String cardNumOrUserName) {
        this.identifierType = identifierType;
        this.identifierNumber = identifierNumber;
        this.cardNumOrUserName = cardNumOrUserName;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public String getIdentifierNumber() {
        return identifierNumber;
    }

    public String getCardNumOrUserName() {
        return cardNumOrUserName;
    }

    public String getFilename() {
        return filename;
    }
}
