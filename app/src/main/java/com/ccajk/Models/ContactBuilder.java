package com.ccajk.Models;

public class ContactBuilder {
    private String name;
    private String designation;
    private String officeContact;
    private String mobileContact;

    public ContactBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ContactBuilder setDesignation(String designation) {
        this.designation = designation;
        return this;
    }

    public ContactBuilder setOfficeContact(String officeContact) {
        this.officeContact = officeContact;
        return this;
    }

    public ContactBuilder setMobileContact(String mobileContact) {
        this.mobileContact = mobileContact;
        return this;
    }

    public Contact createContact() {
        return new Contact(name, designation, officeContact, mobileContact);
    }
}