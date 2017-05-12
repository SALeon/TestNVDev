package org.leon.serg.testnvdev.data.managers;

import android.util.Log;

import org.leon.serg.testnvdev.data.network.res.LocalModelRes;
import org.leon.serg.testnvdev.data.storage.PlacePhotoReference;
import org.leon.serg.testnvdev.utils.ConstantManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhotoManager {
    private static final String TAG = ConstantManager.PREFIX + "PhotoManager";
    private PlacePhotoReference mPlacePhotoReference;

    public PhotoManager() {
        Log.d(TAG, "create PhotoManager");
    }

    public List<PlacePhotoReference> getPhotoReferences(LocalModelRes localData) {
        Log.d(TAG, "getIdPhoto");
        List<PlacePhotoReference> idPhoto = new ArrayList<>();

        for (LocalModelRes.Result result : localData.getResults()) {
            if (result.getPhotos() != null) {

                for (LocalModelRes.Photo photo : result.getPhotos()) {
                    if (photo.getPhotoReference() != null) {
                        idPhoto.add(new PlacePhotoReference(photo.getPhotoReference()));
                        Log.d(TAG, "create Photo");
                    }
                }
            }
        }
        return randomReference(idPhoto);
    }

    private List<PlacePhotoReference> randomReference(List<PlacePhotoReference> idPhoto) {
        List<PlacePhotoReference> photos = new ArrayList<>();
        Random randomGenerator = new Random();
        for (int i = ConstantManager.COUNT_RANDOM_PHOTO_IN_COLLAGE; i > 0; i--) {
            photos.add(idPhoto.get(randomGenerator.nextInt(idPhoto.size() - 1)));
            Log.d(TAG, "create random Photo");
        }
        return photos;
    }



}
