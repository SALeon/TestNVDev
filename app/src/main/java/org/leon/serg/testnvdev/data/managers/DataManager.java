package org.leon.serg.testnvdev.data.managers;


import org.leon.serg.testnvdev.data.network.RestService;
import org.leon.serg.testnvdev.data.network.ServiceGenerator;
import org.leon.serg.testnvdev.data.network.res.LocalModelRes;
import org.leon.serg.testnvdev.data.storage.PlacePhotoReference;

import java.util.List;

import retrofit2.Call;

public class DataManager {
    private RestService mRestService;
    private PhotoManager mPhotoManager;
    private static DataManager ourInstance = new DataManager();

    private DataManager() {

        this.mRestService = ServiceGenerator.createService(RestService.class);

        this.mPhotoManager = new PhotoManager();

    }


    public static DataManager getInstance() {
        return ourInstance;
    }

    //region ================  Network =============
    public Call<LocalModelRes> getLocation(String location, int radius, String key) {
        return mRestService.getLocation(location, radius, key);
    }
    //end region


    public List<PlacePhotoReference> getReferences(LocalModelRes localData){
        return  mPhotoManager.getPhotoReferences(localData);
    }


}
