package shareroute.nazib.com.shareroute;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.GeometryCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.MultiPoint;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.PositionList;
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
import com.google.maps.android.geojson.GeoJsonLayer;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteFileObject;
import static shareroute.nazib.com.shareroute.FileUtils.readFromFile;
import static shareroute.nazib.com.shareroute.FileUtils.writeToFile;

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
    ArrayList<LatLng> mCenterPoints;
    Polyline mMovablePolyLine;
    private FABToolbarLayout layout;
    private View one, two, three, four;
    private View fab;
    private boolean mPermissionDenied = false;
    private ArrayList<Marker> centerMarkerList;
    private ArrayList<Polyline> mPolyLines;
    private ArrayList<Marker> mPolyLineMarkers;
    private MAP_DRAW_TYPE draw_type;

    private String incomingFileName;
    private static boolean mIsDrawMovableLine;

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
        mCenterPoints = new ArrayList<>();
        centerMarkerList = new ArrayList<>();
        mPolyLines = new ArrayList<>();
        mPolyLineMarkers = new ArrayList<>();
        draw_type = MAP_DRAW_TYPE.DRAW_NONE;
        mIsDrawMovableLine = false;

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

        ////////////Handle incoming intents////////////////
        incomingFileName = null;
        Intent intent = getIntent();
        incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
        if(incomingFileName != null){
            incomingFileName += ".geojson";

            loadMapData(getCreatedRouteFileObject(incomingFileName).getAbsolutePath());
        }
        Log.d(TAG, "Incoming intent extra " + intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME));

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


        //
        clearAllMarkersOnPoint();
        drawMarkersOnPoint();

        clearAllMarkersOnPolyline();
        clearAllPolylines();

        drawMarkersOnPolyline();
        drawPolyLines();
        //

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
                //moveCameraToCurrentLocation();
                if(incomingFileName != null){
                    saveMapData(incomingFileName);
                }
                break;
            case R.id.four:
                if(draw_type == MAP_DRAW_TYPE.DRAW_POINT){
                    removeLastMarker();
                }else if(draw_type == MAP_DRAW_TYPE.DRAW_LINE){
                removeLastPointOnLine();
                }
                break;
        }

    }


    private void drawLinesToCenter() {
        mIsDrawMovableLine = true;
        LatLng center = mMap.getCameraPosition().target;

        mPoints.add(center);

        clearAllMarkersOnPolyline();
        clearAllPolylines();

        drawMarkersOnPolyline();
        drawPolyLines();

    }

    private void drawMarkerOnCenter() {
        mIsDrawMovableLine = false;
        LatLng center = mMap.getCameraPosition().target;
        //float max_zoom = mMap.getMaxZoomLevel();
        mCenterPoints.add(center);
        clearAllMarkersOnPoint();
        drawMarkersOnPoint();
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
                if(!mCenterPoints.isEmpty()) {
                    mCenterPoints.remove(mCenterPoints.size() - 1);
                }
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

    private void loadMapData(String filepath){
        String strContent = readFromFile(filepath);
        GeoJSONObject geoJSON = null;
        JSONObject jsonObject = null;
        ArrayList<LatLng> markersLatLng = new ArrayList<>();
        ArrayList<LatLng> markersPolylineLatLng = new ArrayList<>();

        Log.d(TAG, "STRING CONTENT : " + strContent);

        try {
            geoJSON = GeoJSON.parse(strContent);
            GeometryCollection geometryCollection = new GeometryCollection(geoJSON.toJSON());
            ArrayList<Geometry> geometries = new ArrayList<>(geometryCollection.getGeometries());

            MultiPoint markerpoints = (MultiPoint) geometries.get(0);
           //Log.d(TAG, "markerpoints : " + markerpoints.getPositions().toString());
            MultiPoint multiPoint = (MultiPoint) geometries.get(1);
            //Log.d(TAG, "multiPoint : " + multiPoint.getPositions().toString());
            LineString lineString = (LineString) geometries.get(2);
            //Log.d(TAG, "lineString : " + lineString.getPositions().toString());


            for(Position position : markerpoints.getPositions()){
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                markersLatLng.add(latLng);
            }

            for(Position position : multiPoint.getPositions()){
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                markersPolylineLatLng.add(latLng);
            }

            Log.d(TAG, "markersPolylineLatLng " + markersPolylineLatLng.toString());
            Log.d(TAG, "markersLatLng " + markersLatLng.toString());

            mPoints.clear();
            mCenterPoints.clear();

            for(LatLng latLng : markersPolylineLatLng){
                mPoints.add(latLng);
            }
            for(LatLng latLng : markersLatLng){
                mCenterPoints.add(latLng);
            }

//            for(LatLng latLng : markersPolylineLatLng){
//                mMap.addMarker(new MarkerOptions().position(latLng));
//            }



//            jsonObject = geoJSON.toJSON();
//            Log.d(TAG, "1...................");
//
//            JSONArray geometriesArray = jsonObject.getJSONArray("geometries");
//            for (int i = 0; i < geometriesArray.length(); i++) {
//                JSONObject jsonobject = geometriesArray.getJSONObject(i);
//                String type = jsonobject.getString("type");
//                Log.d(TAG, "GEOMETRY TYPE " + type);
//            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

//        try {
//            GeoJsonLayer layer = new GeoJsonLayer(mMap, geoJSON.toJSON());
//            layer.addLayerToMap();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    private  void clearAllMarkersOnPoint(){
        for(int i=0; i<centerMarkerList.size(); i++){
            centerMarkerList.get(i).remove();
        }
        centerMarkerList.clear();
    }

    private  void clearAllMarkersOnPolyline(){
        for(int i=0; i<mPolyLineMarkers.size(); i++){
            mPolyLineMarkers.get(i).remove();
        }
        mPolyLineMarkers.clear();
    }

    private  void clearAllPolylines(){
        for(int i=0; i<mPolyLines.size(); i++){
            mPolyLines.get(i).remove();
        }
        mPolyLines.clear();
    }

    private void drawMarkersOnPoint(){
        if(mCenterPoints == null){
            Log.d(TAG, "mCenterPoints is null");
            return;
        }
        if(!mCenterPoints.isEmpty()){
            for(LatLng latLng : mCenterPoints){
                Marker marker = drawMarkerAtLatLng(latLng);
                centerMarkerList.add(marker);
            }
        }
    }

    private void drawMarkersOnPolyline(){
        if(mPoints == null){
            Log.d(TAG, "mPoints is null");
            return;
        }
        if(!mPoints.isEmpty()){
            for(LatLng latLng : mPoints){
                Marker marker = drawMarkerAtLatLng(latLng);
                mPolyLineMarkers.add(marker);
            }
        }
    }

    private void drawPolyLines(){
        if(mPoints == null){
            Log.d(TAG, "mPoints is null");
            return;
        }
        for(int i=0; i<mPoints.size()-1; i+=1)
        {
            Polyline polyline = drawPolyLineAt(mPoints.get(i), mPoints.get(i+1));
            mPolyLines.add(polyline);
        }
    }


    private Polyline drawPolyLineAt(LatLng start, LatLng end){
        Polyline polyline = null;
        if(mMap != null){
            polyline = mMap.addPolyline(new PolylineOptions().color(Color.RED).add(start, end));
        }
        return polyline;
    }

    private Marker drawMarkerAtLatLng(LatLng latLng){
        Marker marker = null;
        if(mMap != null){
            marker = mMap.addMarker(new MarkerOptions().position(latLng));
            marker.setVisible(true);
        }
        return marker;
    }

    private void saveMapData(String filepath){
        if(mPoints.isEmpty()){
            return;
        }

        JSONObject mapObjects = null;

        ArrayList<Position> polyLineMarkerPositionList = new ArrayList<>();
        ArrayList<Position> centerMarkerPositionList = new ArrayList<>();

        for(LatLng latLng:mPoints){
            Position position = new Position(latLng.latitude, latLng.longitude);
            polyLineMarkerPositionList.add(position);
        }

        for(LatLng latLng:mCenterPoints){
            Position position = new Position(latLng.latitude, latLng.longitude);
            centerMarkerPositionList.add(position);
        }


        GeometryCollection geometryCollection = new GeometryCollection();

        MultiPoint markerpoints = new MultiPoint();
        markerpoints.setPositions(centerMarkerPositionList);

        MultiPoint multiPoint = new MultiPoint();
        multiPoint.setPositions(polyLineMarkerPositionList);

        LineString lineString = new LineString();
        lineString.setPositions(polyLineMarkerPositionList);

        geometryCollection.addGeometry(markerpoints);
        geometryCollection.addGeometry(multiPoint);
        geometryCollection.addGeometry(lineString);

        try {
            mapObjects = geometryCollection.toJSON();

            Log.d(TAG, mapObjects.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(mapObjects != null) {
            File file = getCreatedRouteFileObject(filepath);
            writeToFile(file.getAbsolutePath(), mapObjects.toString());
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
        if(mIsDrawMovableLine){
            drawMovableLine();
        }
    }

    private void drawMovableLine() {
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
