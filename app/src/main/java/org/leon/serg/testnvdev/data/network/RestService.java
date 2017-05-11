package org.leon.serg.testnvdev.data.network;

import org.leon.serg.testnvdev.data.network.req.LocalModelReq;
import org.leon.serg.testnvdev.data.network.res.LocalModelRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestService  {

    @POST("nearbysearch/json")
    Call<LocalModelRes> getLocation(@Body LocalModelReq req);



}
