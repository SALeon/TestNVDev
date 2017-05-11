package org.leon.serg.testnvdev.data.network.req;


public class LocalModelReq {

    private  float latitude;
    private float longitude;
    private int radius;
    private String Key;

    public LocalModelReq(float latitude, float longitude, int radius, String key) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        Key = key;
    }

}
