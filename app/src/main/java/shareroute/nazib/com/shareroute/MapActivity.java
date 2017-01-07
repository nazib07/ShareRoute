package shareroute.nazib.com.shareroute;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

enum MAP_DRAW_TYPE{
    DRAW_NONE,
    DRAW_POINT,
    DRAW_LINE,

    ALL_TYPES
}

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "[SHARE_ROUTE]";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    Context context;
    GoogleMap mMap;
    ArrayList<LatLng> mPoints;
    Polyline mMovablePolyLine;
    private FABToolbarLayout layout;
    private View one, two, three, four;
    private View fab;
    private boolean mPermissionDenied = false;
    private ArrayList<Marker> centerMarkerList;
    private ArrayList<Polyline> mPolyLines;
    private ArrayList<Marker> mPolyLineMarkers;
    private MAP_DRAW_TYPE draw_type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_map);
        context = this;
        mMap = null;
        mPoints = new ArrayList<>();
        centerMarkerList = new ArrayList<>();
        mPolyLines = new ArrayList<>();
        mPolyLineMarkers = new ArrayList<>();
        draw_type = MAP_DRAW_TYPE.DRAW_NONE;

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());
                if (mMap != null) {
                    mMap.addMarker(new MarkerOptions().position(place.getLatLng())).setVisible(true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        layout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        fab = findViewById(R.id.fabtoolbar_fab);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMovablePolyLine = null;
        mMap = map;

        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            moveCameraToCurrentLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onClick(View view) {

        Toast.makeText(this, "Element clicked " + view.getId(), Toast.LENGTH_SHORT).show();

        switch (view.getId()){
            case R.id.one:
                drawMarkerOnCenter();
                draw_type = MAP_DRAW_TYPE.DRAW_POINT;
                break;
            case R.id.two:
                drawLinesToCenter();
                draw_type = MAP_DRAW_TYPE.DRAW_LINE;
                break;
            case R.id.three:
                moveCameraToCurrentLocation();
                break;
            case R.id.four:
                if(draw_type == MAP_DRAW_TYPE.DRAW_POINT){
                    removeLastMarker();
                }else if(draw_type == MAP_DRAW_TYPE.DRAW_LINE){
                removeLastPointOnLine();
                }
                break;
        }


        //drawMarkerOnCenter();
        //drawLinesToCenter();
        //moveCameraToCurrentLocation();
    }


    private void drawLinesToCenter() {
        LatLng center = mMap.getCameraPosition().target;
        Marker marker = mMap.addMarker(new MarkerOptions().position(center));
        marker.setVisible(true);
        mPolyLineMarkers.add(marker);

        mPoints.add(center);

        for(int i=0; i<mPolyLines.size(); i+=1){
            mPolyLines.get(i).remove();
        }
        mPolyLines.clear();

        for(int i=0; i<mPoints.size()-1; i+=1)
        {
            Polyline polyline = mMap.addPolyline(new PolylineOptions().color(Color.RED).add(mPoints.get(i), mPoints.get(i+1)));
            mPolyLines.add(polyline);
        }

        //mMap.addPolyline(new PolylineOptions().color(Color.RED).addAll(mPoints));

    }

    private void drawMarkerOnCenter() {
        LatLng center = mMap.getCameraPosition().target;
        //float max_zoom = mMap.getMaxZoomLevel();
        Marker markerCenter;
        markerCenter = mMap.addMarker(new MarkerOptions().position(center));
        markerCenter.setVisible(true);
        centerMarkerList.add(markerCenter);
    }

    private void removeLastMarker() {
        Log.d(TAG, "removeLastMarker");
        if(!centerMarkerList.isEmpty()){
            Log.d(TAG, "List not empty");
            Marker marker = centerMarkerList.get(centerMarkerList.size()-1);
            if(marker != null) {
                Log.d(TAG, "Marker not null");
                marker.remove();
                centerMarkerList.remove(centerMarkerList.size()-1);
            }
        }
    }

    private void removeLastPointOnLine()
    {
        Log.d(TAG, "removeLastMarker");

        if(!mPolyLines.isEmpty()){
            Polyline polyline = mPolyLines.get(mPolyLines.size()-1);
            polyline.remove();
            mPolyLines.remove(mPolyLines.size()-1);
            if(!mPoints.isEmpty()){
                mPoints.remove(mPoints.size()-1);
            }
        }

        if(!mPoints.isEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPoints.get(mPoints.size() - 1), mMap.getCameraPosition().zoom));

        }

        if(!mPolyLineMarkers.isEmpty() && mPolyLineMarkers.size() >1){
            Marker marker = mPolyLineMarkers.get(mPolyLineMarkers.size()-1);
            marker.remove();
            mPolyLineMarkers.remove(mPolyLineMarkers.size()-1);

        }else{
            if(mPoints.size() == 1){
                Log.d(TAG, "mPoints size 1");
                mPoints.remove(mPoints.size()-1);
                Marker marker = mPolyLineMarkers.get(mPolyLineMarkers.size()-1);
                marker.remove();
            }
        }
    }



    @Override
    public void onCameraMoveStarted(int i) {
        Log.d(TAG, "onCameraMoveStarted");
    }

    @Override
    public void onCameraIdle() {
        Log.d(TAG, "onCameraIdle");
    }

    @Override
    public void onCameraMoveCanceled() {
        Log.d(TAG, "onCameraMoveCanceled");
    }

    @Override
    public void onCameraMove() {
        LatLng center = mMap.getCameraPosition().target;

        if (mMovablePolyLine != null) {
            mMovablePolyLine.remove();
        }

        if (!mPoints.isEmpty()) {
            LatLng lastLatLng = mPoints.get(mPoints.size() - 1);
            mMovablePolyLine = mMap.addPolyline(new PolylineOptions().color(Color.BLUE).add(center, lastLatLng));
        }
    }

    private void moveCameraToCurrentLocation() {

        Location location = getLocation();
        if (location != null) {
            Log.d(TAG, "location : " + location.toString());
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 19));
            }
        } else {
            Log.d(TAG, "location is null");
        }
    }

    private Location getLocation() {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                //this.canGetLocation = true;
                double latitude;
                double longitude;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network Enabled");
                    if (locationManager != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "Permission not granted");
                            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION
                            );
                            return location;
                        }
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            Log.d(TAG, "location " + location.toString());
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }
}
