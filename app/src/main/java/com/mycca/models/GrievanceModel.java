package com.mycca.models;

import java.util.Date;

public class GrievanceModel {

    private int identifierType;
    private String identifierNumber;
    private String email;
    private String mobile;
    private String details;
    private String circle;
    private String submittedBy;
    private String message;
    private String uid;
    private String referenceNo;
    private String filePathList;
    private Date date;
    private long grievanceStatus;
    private long grievanceType;
    private long fileCount;
    private boolean expanded;
    private boolean highlighted = false;
    private boolean submissionSuccess;

    public GrievanceModel() {

    }

    public GrievanceModel(int identifierType, String identifierNumber, String email, String mobile, String details, String submittedBy, String circle, String uid, String referenceNo, Date date, long grievanceStatus, long grievanceType, long fileCount) {
        this.identifierType = identifierType;
        this.identifierNumber = identifierNumber;
        this.email = email;
        this.mobile = mobile;
        this.details = details;
        this.submittedBy = submittedBy;
        this.circle=circle;
        this.uid = uid;
        this.referenceNo = referenceNo;
        this.date = date;
        this.grievanceStatus = grievanceStatus;
        this.grievanceType = grievanceType;
        this.fileCount = fileCount;
    }

    public GrievanceModel(int identifierType, String identifierNumber, String email, String mobile, String details, String circle, long grievanceType, String submittedBy, String filePathList) {
        this.identifierType = identifierType;
        this.identifierNumber = identifierNumber;
        this.email = email;
        this.mobile = mobile;
        this.details = details;
        this.circle = circle;
        this.submittedBy = submittedBy;
        this.filePathList = filePathList;
        this.grievanceType = grievanceType;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public String getFilePathList() {
        return filePathList;
    }

    public void setFilePathList(String filePathList) {
        this.filePathList = filePathList;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getIdentifierNumber() {
        return identifierNumber;
    }

    public void setIdentifierNumber(String identifierNumber) {
        this.identifierNumber = identifierNumber;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public long getGrievanceType() {
        return grievanceType;
    }

    public void setGrievanceType(long grievanceType) {
        this.grievanceType = grievanceType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public long getGrievanceStatus() {
        return grievanceStatus;
    }

    public void setGrievanceStatus(long grievanceStatus) {
        this.grievanceStatus = grievanceStatus;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public boolean isSubmissionSuccess() {
        return submissionSuccess;
    }

    public void setSubmissionSuccess(boolean submissionSuccess) {
        this.submissionSuccess = submissionSuccess;
    }

    public int getIdentifierType() {
        return identifierType;
    }

    public void setIdentifierType(int identifierType) {
        this.identifierType = identifierType;
    }

    public long getFileCount() {
        return fileCount;
    }

    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }
}
