package com.mycca.models;

import java.util.Date;

public class NewsModel {

    private Date dateAdded;
    private Date dateUpdated;
    private String headline;
    private String description;
    private String state;
    private String key;
    private String updatedBy;
    private String addedBy;

    public NewsModel() {
    }

    public NewsModel(Date date, Date dateUpdated, String headline, String description, String state, String key, String updatedBy, String addedBy) {
        this.dateAdded = date;
        this.dateUpdated = dateUpdated;
        this.headline = headline;
        this.description = description;
        this.state = state;
        this.key = key;
        this.updatedBy = updatedBy;
        this.addedBy = addedBy;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
