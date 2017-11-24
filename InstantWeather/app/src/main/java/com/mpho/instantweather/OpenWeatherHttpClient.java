package com.mpho.instantweather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class OpenWeatherHttpClient {

    public String getCurrentWeatherData(Double lat, Double lon){
        HttpsURLConnection connector=null;
        InputStream input_s=null;

        try {
            String OPW_API_KEY = "aa5a227bc77023574ceaaeec61e30b64";
            String OPW_URL = "http://api.openweathermap.org/data/2.5/weather?mode=json&lat=%f&lon=%f";
            String url = OPW_URL + lat + lon + "&APPID=" + OPW_API_KEY;
            connector = (HttpsURLConnection) (new URL(url)).openConnection();
            connector.setDoInput(true);
            connector.setDoOutput(true);
            connector.connect();
            int responseCode = connector.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                //read response
                StringBuffer buff = null;
                try {
                    buff = new StringBuffer();
                    input_s = connector.getInputStream();
                    BufferedReader buffer_read = new BufferedReader(new InputStreamReader(input_s));
                    String line;
                    while ((line = buffer_read.readLine()) != null) {
                        buff.append(line);
                        buff.append("\r\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (input_s != null) {
                    input_s.close();
                }
                connector.disconnect();
                return buff.toString();
            }
        }catch (IOException t) {
            t.printStackTrace();
        } finally {
            try {
                if (input_s != null) {
                    input_s.close();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            try {
                if (connector != null) {
                    connector.disconnect();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    public Bitmap getImage(String code){
        HttpsURLConnection connector=null;
        InputStream input_st=null;
        try {
            String IMG_URL = "http://openweather.org/img/w/";
            connector = (HttpsURLConnection) (new URL(IMG_URL + code+".png")).openConnection();
            connector.setRequestMethod("GET");
            connector.setDoInput(true);
            connector.setDoOutput(true);
            connector.connect();
            int responseCode = connector.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {

                input_st = connector.getInputStream();
                byte[] buffer = new byte[1024];
                ByteArrayOutputStream byte_aos = new ByteArrayOutputStream();

                while (input_st.read(buffer) != -1)
                    byte_aos.write(buffer);

                //return byte_aos.toByteArray();

                return BitmapFactory.decodeByteArray(byte_aos.toByteArray(),0,byte_aos.toByteArray().length);

            }
        }catch (Throwable t){
            t.printStackTrace();
        }
        finally {
            try{
                if (input_st == null) {
                    throw new AssertionError();
                }
                input_st.close();
            } catch (Throwable t){
                t.printStackTrace();
            }
            try{
                if (connector != null) {
                    connector.disconnect();
                }
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
        return null;
    }
}