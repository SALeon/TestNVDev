package org.leon.serg.testnvdev.data.managers;


import org.leon.serg.testnvdev.data.network.RestService;
import org.leon.serg.testnvdev.data.network.ServiceGenerator;
import org.leon.serg.testnvdev.data.network.req.LocalModelReq;
import org.leon.serg.testnvdev.data.network.res.LocalModelRes;

import retrofit2.Call;

public class DataManager {
//    private PreferencesManager mPreferencesManager;
//    private Context mContext;
    private RestService mRestService;

    private static DataManager ourInstance = new DataManager();

    private DataManager() {
//        this.mPreferencesManager=new PreferencesManager();
//        this.mContext=TestNVDevApplication.getContext();
    this.mRestService= ServiceGenerator.createService(RestService.class);
    }


    public static DataManager getInstance() {
        return ourInstance;
    }

//    public PreferencesManager getPreferencesManager(){
//        return  mPreferencesManager;
//    }
//
//    public Context getContext(){
//        return mContext;
//    }

    //region ================  Network =============
public Call<LocalModelRes> getLocation(LocalModelReq paramLocation){
    return mRestService.getLocation(paramLocation);
}


    //end region
}
