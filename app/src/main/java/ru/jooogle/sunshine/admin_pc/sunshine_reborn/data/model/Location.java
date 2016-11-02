package ru.jooogle.sunshine.admin_pc.sunshine_reborn.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Location extends RealmObject {
    @PrimaryKey
    private long id;
    private String locationSettings;
    private String city;
    private double latitude;
    private double longitude;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLocationSettings() {
        return locationSettings;
    }

    public void setLocationSettings(String locationSettings) {
        this.locationSettings = locationSettings;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
