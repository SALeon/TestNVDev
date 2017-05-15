package org.leon.serg.testnvdev.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.leon.serg.testnvdev.data.managers.DataManager;
import org.leon.serg.testnvdev.data.network.res.LocalModelRes;
import org.leon.serg.testnvdev.data.storage.PlacePhotoReference;
import org.leon.serg.testnvdev.utils.AppConfig;
import org.leon.serg.testnvdev.utils.ConstantManager;
import org.leon.serg.testnvdev.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener {

    private View mScreenshotView;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private LatLng mLatLng;
    private SupportMapFragment mFragment;
    private Marker currLocationMarker;
    private GoogleMap mMap;
    private FloatingActionButton mActionButton;
    private static final String TAG = ConstantManager.PREFIX + "MapsActivity";
    private DataManager mDataManager;
    private BottomSheetBehavior mBottomSheetBehavior;
    private int mCurrentEditMode = ConstantManager.EDIT_MODE_HIDE_COLLAGE;
    private ArrayList<String> mKeyPhotoPlace;
    private String[] mPermissionsForActivity = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @BindViews({
            R.id.image_sheet_bottom_1,
            R.id.image_sheet_bottom_2,
            R.id.image_sheet_bottom_3,
            R.id.image_sheet_bottom_4,
    })
    List<ImageView> mCollageView;
    Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d(TAG, "onCreate");

        mFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mFragment.getMapAsync(this);

        mActionButton = (FloatingActionButton) findViewById(R.id.add_fab);
        mActionButton.setOnClickListener(this);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        mScreenshotView = findViewById(R.id.screenshot_view);

        mUnbinder = ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();


        if (savedInstanceState != null) {

            mKeyPhotoPlace = savedInstanceState.getStringArrayList(ConstantManager.ID_PHOTO_IN_COLLAGE_KEY);
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_SAVE_KEY);
            changeEditMode(mCurrentEditMode);


        }
//TODO : save data photos and locale (rotate device)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            } else {
                checkPermissions("необходимо разрешение для получения местоположения"
                        , "для продолжения работы приложения необходимо разрешение для определения местоположения "
                        , mPermissionsForActivity[0]);
            }
        } else {
            mMap.setMyLocationEnabled(true);
            buildGoogleApiClient();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location lastLocation = null;
        Log.d(TAG, "onConnected");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } else {
                checkPermissions("необходимо разрешение для получения местоположения"
                        , "для продолжения работы приложения необходимо разрешение для определения местоположения "
                        , mPermissionsForActivity[0]);
            }
        } else {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }
        if (lastLocation != null) {
            mLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mLatLng);
            markerOptions.title(getString(R.string.marker_current_position));
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            currLocationMarker = mMap.addMarker(markerOptions);
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(30000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (currLocationMarker != null) {
            currLocationMarker.remove();
        }
        mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mLatLng);
        markerOptions.title(getString(R.string.marker_current_position));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        currLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, ConstantManager.VALUE_ZOOM_LOCATION));

    }


    private void checkPermissions(String titleText, String messageText, final String permission) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    permission)) {

                new AlertDialog.Builder(this)
                        .setTitle(titleText)
                        .setMessage(messageText)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        mPermissionsForActivity,
                                        ConstantManager.PERMISSION_CODE);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        mPermissionsForActivity,
                        ConstantManager.PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.PERMISSION_CODE && grantResults.length == 2)

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    if (mGoogleApiClient == null) {
                        buildGoogleApiClient();
                    }
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(this, "в разрешении gps отказано", Toast.LENGTH_LONG).show();
            }
        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(this, "в разрешении записи в галерею отказано", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(ConstantManager.EDIT_MODE_SAVE_KEY, ConstantManager.EDIT_MODE_ROTATE_COLLAGE);
        if (mKeyPhotoPlace != null) {
            outState.putStringArrayList(ConstantManager.ID_PHOTO_IN_COLLAGE_KEY, mKeyPhotoPlace);
        }
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        if(mCurrentEditMode==ConstantManager.EDIT_MODE_SHOW_COLLAGE){
            mActionButton.setImageResource(R.drawable.ic_add_white_24dp);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mCurrentEditMode=ConstantManager.EDIT_MODE_HIDE_COLLAGE;
        }else
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_fab:
                if (mCurrentEditMode == ConstantManager.EDIT_MODE_HIDE_COLLAGE) {
                    changeEditMode(mCurrentEditMode);
                    mCurrentEditMode = ConstantManager.EDIT_MODE_SHOW_COLLAGE;
                    Call<LocalModelRes> call = mDataManager.getLocation(
                            convertLocation((float) mLatLng.latitude
                                    , (float) mLatLng.longitude)
                            , ConstantManager.RADIUS_SEARCH_LOCATION
                            , ConstantManager.GOOGLE_PLACE_API_KEY
                    );
                    call.enqueue(new Callback<LocalModelRes>() {
                        @Override
                        public void onResponse(Call<LocalModelRes> call, Response<LocalModelRes> response) {
                            if (response.code() == 200) {
                                List<PlacePhotoReference> listReference = mDataManager.getReferences(response.body());
                                mKeyPhotoPlace = new ArrayList<String>();
                                for (PlacePhotoReference element : listReference) {
                                    mKeyPhotoPlace.add(element.getIdPhoto());
                                }
                                loadPhoto();
                            } else {
                                Log.d(TAG, "Error response server");
                            }
                        }

                        @Override
                        public void onFailure(Call<LocalModelRes> call, Throwable t) {
                            Log.d(TAG, "Error");
                        }
                    });


                } else if (mCurrentEditMode == ConstantManager.EDIT_MODE_SHOW_COLLAGE) {
                    changeEditMode(mCurrentEditMode);
                    mCurrentEditMode = 0;
                    takeScreenshot();
                }
        }

        //TODO:default
    }


    private String convertLocation(float latitude, float longitude) {
        return new String(Float.toString(latitude) + "," + Float.toString(longitude));
    }

    private void changeEditMode(int mode) {

        if (mode == ConstantManager.EDIT_MODE_HIDE_COLLAGE) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mActionButton.setImageResource(R.drawable.ic_share_white_24dp);

        } else if (mode == ConstantManager.EDIT_MODE_SHOW_COLLAGE) {
            mActionButton.setImageResource(R.drawable.ic_add_white_24dp);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        } else {
            if (mKeyPhotoPlace != null) {
                mCurrentEditMode = ConstantManager.EDIT_MODE_ROTATE_COLLAGE;
                loadPhoto();
                mActionButton.setImageResource(R.drawable.ic_share_white_24dp);
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    private void loadPhoto() {
        for (int i = 0; i < ConstantManager.COUNT_RANDOM_PHOTO_IN_COLLAGE; i++) {

            String URL = AppConfig.PHOTO_URL.concat(AppConfig.MAX_WITH_PHOTO_URL_ATR).concat(ConstantManager.MAX_WITH_PHOTO)
                    .concat(AppConfig.REFERENCE_PHOTO_URL_ATR).concat(mKeyPhotoPlace.get(i))
                    .concat(AppConfig.KEY_URL_ATR).concat(ConstantManager.GOOGLE_PLACE_API_KEY);

            Picasso.with(getApplicationContext()).load(URL)
                    .fit()
                    .centerCrop()
                    .into(mCollageView.get(i));

        }
    }

    private void takeScreenshot() {

        mScreenshotView.setDrawingCacheEnabled(true);
        mScreenshotView.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(mScreenshotView.getDrawingCache());
        mScreenshotView.setDrawingCacheEnabled(false);


        //Save bitmap
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = new SimpleDateFormat("yyyyMMddhhmm'_report.jpg'").format(new Date());
        File myPath = new File(storageDir, fileName);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(myPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Screen", "screen");

            shareImage(myPath);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void shareImage(File path){
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(path));
        shareIntent.setType("image/jpeg");
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.send_to)));

    }
}