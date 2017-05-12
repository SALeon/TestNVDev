package org.leon.serg.testnvdev.data.network;

import org.leon.serg.testnvdev.data.network.res.LocalModelRes;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestService  {

    @POST("nearbysearch/json")
    Call<LocalModelRes> getLocation(@Query("location") String location, @Query("radius")int radius, @Query("key")String key);



}
