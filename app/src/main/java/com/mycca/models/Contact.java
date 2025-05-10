package com.mycca.models;

public class Contact {
    private String name;
    private String designation;
    private String officeContact;
    private String mobileContact;
    private String email;

    public Contact() {
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    private boolean isExpanded;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getOfficeContact() {
        return officeContact;
    }

    public void setOfficeContact(String officeContact) {
        this.officeContact = officeContact;
    }

    public String getMobileContact() {
        return mobileContact;
    }

    public void setMobileContact(String mobileContact) {
        this.mobileContact = mobileContact;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    Contact(String name, String designation, String email, String officeContact, String mobileContact) {

        this.name = name;
        this.email=email;
        this.designation = designation;
        this.officeContact = officeContact;
        this.mobileContact = mobileContact;

    }


}
