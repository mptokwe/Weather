package com.mpho.instantweather;

public class CurrentWeatherDetails {
    private int current_weather_id,humidity, pressure, clouds_perc;
    private float max_temp,min_temp,temperature,wind_speed,wind_deg;
    private String description,condition,icon;
    private UserLocationInformation user_location;

    protected void setCurrent_weather_id(int w_id){
        current_weather_id=w_id;
    }
    public int getCurrent_weather_id(){
        return current_weather_id;
    }
    protected void setHumidity(int humidity1){
        humidity=humidity1;
    }
    public int getHumidity(){
        return humidity;
    }
    protected void setPressure(int pressure1){
        pressure=pressure1;
    }
    public int getPressure(){
        return pressure;
    }
    protected void setClouds_perc(int clouds_perc1){
        clouds_perc=clouds_perc1;
    }
    public int getClouds_perc(){
        return clouds_perc;
    }
    protected void setMax_temp(float max_temp1){
        max_temp=max_temp1;
    }
    public float getMax_temp(){
        return max_temp;
    }
    protected void setMin_temp(float min_temp1){
        min_temp=min_temp1;
    }
    public float getMin_temp(){
        return min_temp;
    }
    protected void setTemperature(float temperature1){
        temperature=temperature1;
    }
    public float getTemperature(){
        return temperature;
    }
    protected void setWind_speed(float wind_speed1){
        wind_speed=wind_speed1;
    }
    public float getWind_speed(){
        return wind_speed;
    }
    protected void setWind_deg(float wind_deg1){
        wind_deg=wind_deg1;
    }
    public float getWind_deg(){
        return wind_deg;
    }
    protected void setDescription(String description1){
        description=description1;
    }
    public String getDescription(){
        return description;
    }
    protected void setCondition(String condition1){
        condition=condition1;
    }
    public String getCondition(){
        return condition;
    }
    protected void setIcon(String icon1){
        icon=icon1;
    }
    public String getIcon(){
        return icon;
    }
    protected void setUser_location(UserLocationInformation user_location1){
        user_location=user_location1;
    }
    public UserLocationInformation getUser_location(){
        return user_location;
    }
}