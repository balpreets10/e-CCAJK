package com.mycca.models;

public class LocationModel {

    private String LocationName;
    private String District;
    private String Block;
    private String SSA;
    private Double Latitude;
    private Double Longitude;

    public LocationModel() {

    }

    public LocationModel(String locationName, Double latitude, Double longitude, String stateID, String district, String block, String SSA) {

        LocationName = locationName;
        Latitude = latitude;
        Longitude = longitude;
        District = district;
        Block = block;
        this.SSA = SSA;
    }

    public String getBlock() {
        return Block;
    }

    public void setBlock(String block) {
        Block = block;
    }

    public String getSSA() {
        return SSA;
    }

    public void setSSA(String SSA) {
        this.SSA = SSA;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        this.Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        this.Longitude = longitude;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        this.LocationName = locationName;
    }

    public String getDistrict() {
        return District;
    }

    public void setDistrict(String district) {
        this.District = district;
    }

}
