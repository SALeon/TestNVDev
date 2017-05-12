package org.leon.serg.testnvdev.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
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
    private int mCurrentEditMode = 0;


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

        mUnbinder = ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

//TODO : save data photos and locale (rotate device)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                mMap.setMyLocationEnabled(true);
                buildGoogleApiClient();
            } else {
                checkLocationPermission();
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    ) {
                lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } else {
                checkLocationPermission();
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
        //place marker at current position
        //mGoogleMap.clear();

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

        //If you only need one location, unregister the listener
//        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        ConstantManager.PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ConstantManager.PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ConstantManager.PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    // permission denied! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_fab:
                if (mCurrentEditMode == 0) {

                    Log.d(TAG, "button fab add work");
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

                                for (int i = 0; i < ConstantManager.COUNT_RANDOM_PHOTO_IN_COLLAGE; i++) {

                                    String URL = AppConfig.PHOTO_URL.concat(AppConfig.MAX_WITH_PHOTO_URL_ATR).concat(ConstantManager.MAX_WITH_PHOTO)
                                            .concat(AppConfig.REFERENCE_PHOTO_URL_ATR).concat(listReference.get(i).getIdPhoto())
                                            .concat(AppConfig.KEY_URL_ATR).concat(ConstantManager.GOOGLE_PLACE_API_KEY);

                                    Picasso.with(getApplicationContext()).load(URL)
                                            .fit()
                                            .centerCrop()
                                            .into(mCollageView.get(i));
                                }

                            } else {
                                Log.d(TAG, "Error response server");
                            }
                        }

                        @Override
                        public void onFailure(Call<LocalModelRes> call, Throwable t) {
                            Log.d(TAG, "Error");
                        }
                    });

                    changeEditMode(mCurrentEditMode);
                    mCurrentEditMode=1;


                }else {
                    changeEditMode(mCurrentEditMode);
                    mCurrentEditMode=0;
//                    TODO: shared photo
                }
        }

        //TODO:default
    }


    private String convertLocation(float latitude, float longitude) {
        return new String(Float.toString(latitude) + "," + Float.toString(longitude));
    }

    private void changeEditMode(int mode) {
        if (mode == 1) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            mActionButton.setImageResource(R.drawable.ic_add_white_24dp);


        } else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            mActionButton.setImageResource(R.drawable.ic_share_white_24dp);

        }
    }

}
