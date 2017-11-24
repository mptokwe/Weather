package com.mpho.instantweather;

public class UserLocationInformation {
    private float latitude, longitude;
    private String country, city;

    protected void setLongitude(float lon){
        longitude=lon;
    }
    public float getLongitude(){
        return longitude;
    }
    protected void setLatitude(float lat){
        latitude=lat;
    }
    public float getLatitude(){
        return latitude;
    }
    protected void setCountry(String country_){
        country=country_;
    }
    public String getCountry(){
        return country;
    }
    protected void setCity(String city_){
        city=city_;
    }
    public String getCity(){
        return city;
    }
}