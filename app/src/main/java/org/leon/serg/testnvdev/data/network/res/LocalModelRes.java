package org.leon.serg.testnvdev.data.network.res;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LocalModelRes {

    @SerializedName("html_attributions")
    @Expose
    private List<Object> htmlAttributions = null;
    @SerializedName("next_page_token")
    @Expose
    private String nextPageToken;
    @SerializedName("results")
    @Expose
    public List<Result> results = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Result> getResults(){
        return results;
    }


    public class Result {
        @SerializedName("geometry")
        @Expose
        private Geometry geometry;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("photos")
        @Expose
        public List<Photo> photos = null;
        @SerializedName("place_id")
        @Expose
        private String placeId;
        @SerializedName("reference")
        @Expose
        private String reference;
        @SerializedName("scope")
        @Expose
        private String scope;
        @SerializedName("types")
        @Expose
        private List<String> types = null;
        @SerializedName("vicinity")
        @Expose
        private String vicinity;
        @SerializedName("rating")
        @Expose
        private float rating;

        @SerializedName("opening_hours")
        @Expose
        private OpeningHours openingHours;

        public List<Photo> getPhotos(){
            return photos;
        }
    }

    public class Geometry {
        @SerializedName("location")
        @Expose
        private Location location;

        @SerializedName("viewport")
        @Expose
        private Viewport viewport;
    }
    public class Location {

        @SerializedName("lat")
        @Expose
        private float lat;
        @SerializedName("lng")
        @Expose
        private float lng;

    }

    public class Northeast {

        @SerializedName("lat")
        @Expose
        private float lat;
        @SerializedName("lng")
        @Expose
        private float lng;

    }

    public class OpeningHours {

        @SerializedName("open_now")
        @Expose
        private boolean openNow;
        @SerializedName("weekday_text")
        @Expose
        private List<Object> weekdayText = null;

    }

    public class Photo {

        @SerializedName("height")
        @Expose
        private int height;
        @SerializedName("html_attributions")
        @Expose
        private List<String> htmlAttributions = null;
        @SerializedName("photo_reference")
        @Expose
        public String photoReference;
        @SerializedName("width")
        @Expose
        private int width;

        public String getPhotoReference(){
            return photoReference;
        }

    }
    public class Southwest {

        @SerializedName("lat")
        @Expose
        private float lat;
        @SerializedName("lng")
        @Expose
        private float lng;

    }

    public class Viewport {

        @SerializedName("northeast")
        @Expose
        private Northeast northeast;
        @SerializedName("southwest")
        @Expose
        private Southwest southwest;

    }

    }
