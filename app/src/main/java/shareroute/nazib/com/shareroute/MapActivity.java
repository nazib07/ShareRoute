package shareroute.nazib.com.shareroute;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.GeometryCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.MultiPoint;
import com.cocoahero.android.geojson.Position;
import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import static shareroute.nazib.com.shareroute.FileUtils.createSharedRouteFile;
import static shareroute.nazib.com.shareroute.FileUtils.getCreatedRouteFileObject;
import static shareroute.nazib.com.shareroute.FileUtils.getSharedRouteFileObject;
import static shareroute.nazib.com.shareroute.FileUtils.readFromFile;
import static shareroute.nazib.com.shareroute.FileUtils.writeToFile;

enum MAP_DRAW_TYPE {
    DRAW_NONE,
    DRAW_POINT,
    DRAW_LINE,
}

enum ROUTE_CREATION_MODE {
    ROUTE_CREATION_MODE_EDIT,
    ROUTE_CREATION_MODE_RECORD
}

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "[SHARE_ROUTE]";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 3;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private static final int mZoom = 17;
    Context context;
    GoogleMap mMap;
    ArrayList<LatLng> mPoints;
    ArrayList<LatLng> mCenterPoints;
    Polyline mMovablePolyLine;
    private FABToolbarLayout layout;
    private View one, two, three, four, rec;
    private View fab;
    private boolean mPermissionDenied = false;
    private ArrayList<Marker> centerMarkerList;
    private ArrayList<Polyline> mPolyLines;
    private ArrayList<Marker> mPolyLineMarkers;
    private MAP_DRAW_TYPE draw_type;
    private LatLng zoomLatlngAtRoute;
    private String incomingFileName;
    private static boolean mIsDrawMovableLine;
    private Intent mExternalDataIntent;
    private MenuItem item;
    private Menu mOptionsMenu;
    private boolean isSharedFile;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Location mSavedLocation;
    private static int flag = 1;
    private boolean isRecordRoute;
    private boolean mIsRecodrdingStarted;
    private boolean mIsCameraNotMovedToCurrentLoc;
    private float mMinRecordDistance = CommonUtils.DEFAULT_DISTANCE_TUNER_VALUE;
    private boolean isMoveToCurrentLocation;

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
        mLastLocation = null;
        mSavedLocation = null;
        isRecordRoute = false;
        mIsRecodrdingStarted = false;
        mIsCameraNotMovedToCurrentLoc = false;
        mMinRecordDistance = CommonUtils.getTunerValueFromPreference(this);
        isMoveToCurrentLocation = true;


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
                    //mMap.addMarker(new MarkerOptions().position(place.getLatLng())).setVisible(true);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), mZoom));
                }
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);

        layout = (FABToolbarLayout) findViewById(R.id.fabtoolbar);
        rec = findViewById(R.id.rec);
        one = findViewById(R.id.one);
        two = findViewById(R.id.two);
        three = findViewById(R.id.three);
        four = findViewById(R.id.four);
        fab = findViewById(R.id.fabtoolbar_fab);

        one.setOnClickListener(this);
        two.setOnClickListener(this);
        three.setOnClickListener(this);
        four.setOnClickListener(this);
        rec.setOnClickListener(this);

        rec.setClickable(false);
        rec.setEnabled(false);
        rec.setAlpha(.5f);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.show();
                item.setVisible(true);
            }
        });

        ////////////Handle incoming intents////////////////
        incomingFileName = null;
        Intent intent = getIntent();
        String intentAction = intent.getAction();

        Log.d(TAG, "intent= " + intent);


        if (CommonUtils.INTENT_ACTION_CUSTOM_1.equals(intentAction)) {
            Log.d(TAG, "Created Route Intent");
            incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
            if (incomingFileName != null) {
                try {
                    getSupportActionBar().setTitle(incomingFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isSharedFile = false;
                incomingFileName += ".geojson";

                loadMapData(getCreatedRouteFileObject(incomingFileName).getAbsolutePath());
            }
            Log.d(TAG, "Incoming intent extra " + intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME));
        } else if (CommonUtils.INTENT_ACTION_CUSTOM_3.equals(intentAction)) {
            Log.d(TAG, "Record Route Intent");
            incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
            if (incomingFileName != null) {
                try {
                    getSupportActionBar().setTitle(incomingFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isSharedFile = false;
                incomingFileName += ".geojson";
                isRecordRoute = true;
                rec.setEnabled(true);
                rec.setClickable(true);
                rec.setAlpha(1.0f);
                loadMapData(getCreatedRouteFileObject(incomingFileName).getAbsolutePath());
            }
        } else if (CommonUtils.INTENT_ACTION_CUSTOM_2.equals(intentAction)) {
            Log.d(TAG, "Shared Route Intent");
            incomingFileName = intent.getStringExtra(CommonUtils.SELECTED_ROUTE_FILE_NAME);
            if (incomingFileName != null) {
                try {
                    getSupportActionBar().setTitle(incomingFileName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                isSharedFile = false;
                incomingFileName += ".geojson";

                Log.d(TAG, "shared filename : " + incomingFileName);

                loadMapData(getSharedRouteFileObject(incomingFileName).getAbsolutePath());
            }
        } else if (Intent.ACTION_VIEW.equals(intentAction)) {

            if (intent != null) {
                mExternalDataIntent = intent;

                ////////////////////////////////////////////////////
                isSharedFile = true;
                showSaveDialog();
                loadDataFromExternalFile(intent);
                ///////////////////////////////////////////////////
            }
        }
    }

    private void zoomToRoute() {
        if (mMap != null) {
            if (zoomLatlngAtRoute != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(zoomLatlngAtRoute, mZoom));
            }
        }
    }

    private void showSaveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View textEntryView = inflater.inflate(R.layout.dialog_input_route_name, null);
        TextView textview = (TextView) textEntryView.findViewById(R.id.textView);
        textview.setText("Save Route?");
        builder.setView(textEntryView)
                // Add action buttons
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        try {

                            EditText editText = (EditText) textEntryView.findViewById(R.id.username);

                            String route_name;
                            route_name = editText.getText().toString();
                            if (route_name.length() > 0) {
                                incomingFileName = route_name + ".geojson";
                                createSharedRouteFile(incomingFileName);
                                saveMapData(incomingFileName);
                            } else {
                                //onBackPressed();
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //onBackPressed();
                    }
                }).create().show();
    }


    private void loadDataFromExternalFile(Intent intent) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
        } else {
            Uri uri = intent.getData();
            String sharedFileName = getPath(context, uri);
            Log.d(TAG, "uri= " + getPath(context, uri));
            loadMapData(sharedFileName);

            //
            if (mMap != null) {
                clearAllMarkersOnPoint();
                drawMarkersOnPoint();

                clearAllMarkersOnPolyline();
                clearAllPolylines();

                drawMarkersOnPolyline();
                drawPolyLines();
                zoomToRoute();
            }
            //
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //    finish();

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
        zoomToRoute();
        //

    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        Log.d(TAG, "enableMyLocation....");
        statusCheck();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            moveCameraToCurrentLocation();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult.........");
//
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadDataFromExternalFile(mExternalDataIntent);
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

        switch (view.getId()) {
            case R.id.rec:

                mIsRecodrdingStarted ^= true;
                if (mIsRecodrdingStarted) {
                    if (mMovablePolyLine != null) {
                        mMovablePolyLine.remove();
                        mMovablePolyLine = null;
                        mIsDrawMovableLine = false;
                    }

                    ((ImageView) rec).setImageResource(R.drawable.ic_videocam_off_white_24dp);
                    one.setClickable(false);
                    one.setEnabled(false);
                    one.setAlpha(.5f);
                    two.setClickable(false);
                    two.setEnabled(false);
                    two.setAlpha(.5f);
                } else {
                    ((ImageView) rec).setImageResource(R.drawable.ic_videocam_white_24dp);
                    one.setClickable(true);
                    one.setEnabled(true);
                    one.setAlpha(1.0f);
                    two.setClickable(true);
                    two.setEnabled(true);
                    two.setAlpha(1.0f);
                }
                break;
            case R.id.one:
                if (mMovablePolyLine != null) {
                    mMovablePolyLine.remove();
                    mMovablePolyLine = null;
                }
                drawMarkerOnCenter();
                draw_type = MAP_DRAW_TYPE.DRAW_POINT;
                break;
            case R.id.two:
                drawLinesToCenter();
                draw_type = MAP_DRAW_TYPE.DRAW_LINE;
                break;
            case R.id.three:
                if (isMoveToCurrentLocation) {
                    moveCameraToCurrentLocation();
                } else {
                    zoomToRoute();
                }
                isMoveToCurrentLocation ^= true;
                break;
            case R.id.four:
                if (draw_type == MAP_DRAW_TYPE.DRAW_POINT) {
                    removeLastMarker();
                } else if (draw_type == MAP_DRAW_TYPE.DRAW_LINE) {
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

    private void drawRecordedLines(LatLng point) {
        mPoints.add(point);

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
        if (!centerMarkerList.isEmpty()) {
            Log.d(TAG, "List not empty");
            Marker marker = centerMarkerList.get(centerMarkerList.size() - 1);
            if (marker != null) {
                Log.d(TAG, "Marker not null");
                marker.remove();
                centerMarkerList.remove(centerMarkerList.size() - 1);
                if (!mCenterPoints.isEmpty()) {
                    mCenterPoints.remove(mCenterPoints.size() - 1);
                }
            }
        }
    }

    private void removeLastPointOnLine() {
        Log.d(TAG, "removeLastMarker");

        if (!mPolyLines.isEmpty()) {
            Polyline polyline = mPolyLines.get(mPolyLines.size() - 1);
            polyline.remove();
            mPolyLines.remove(mPolyLines.size() - 1);
            if (!mPoints.isEmpty()) {
                mPoints.remove(mPoints.size() - 1);
            }
        }

        if (!mPoints.isEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mPoints.get(mPoints.size() - 1), mMap.getCameraPosition().zoom));

        }

        if (!mPolyLineMarkers.isEmpty() && mPolyLineMarkers.size() > 1) {
            Marker marker = mPolyLineMarkers.get(mPolyLineMarkers.size() - 1);
            marker.remove();
            mPolyLineMarkers.remove(mPolyLineMarkers.size() - 1);

        } else {
            if (mPoints.size() == 1) {
                Log.d(TAG, "mPoints size 1");
                mPoints.remove(mPoints.size() - 1);
                Marker marker = mPolyLineMarkers.get(mPolyLineMarkers.size() - 1);
                marker.remove();
            }
        }
    }

    private void loadMapData(String filepath) {
        Log.d(TAG, "loadMapData " + filepath);
        String strContent = readFromFile(filepath);
        GeoJSONObject geoJSON = null;
        ArrayList<LatLng> markersLatLng = new ArrayList<>();
        ArrayList<LatLng> markersPolylineLatLng = new ArrayList<>();

        Log.d(TAG, "STRING CONTENT : " + strContent);

        try {
            geoJSON = GeoJSON.parse(strContent);
            GeometryCollection geometryCollection = new GeometryCollection(geoJSON.toJSON());
            ArrayList<Geometry> geometries = new ArrayList<>(geometryCollection.getGeometries());

            MultiPoint markerpoints = (MultiPoint) geometries.get(0);
            MultiPoint multiPoint = (MultiPoint) geometries.get(1);

            for (Position position : markerpoints.getPositions()) {
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                markersLatLng.add(latLng);
            }

            for (Position position : multiPoint.getPositions()) {
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                markersPolylineLatLng.add(latLng);
            }

            Log.d(TAG, "markersPolylineLatLng " + markersPolylineLatLng.toString());
            Log.d(TAG, "markersLatLng " + markersLatLng.toString());

            mPoints.clear();
            mCenterPoints.clear();

            for (LatLng latLng : markersPolylineLatLng) {
                mPoints.add(latLng);
            }
            for (LatLng latLng : markersLatLng) {
                mCenterPoints.add(latLng);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void clearAllMarkersOnPoint() {
        for (int i = 0; i < centerMarkerList.size(); i++) {
            centerMarkerList.get(i).remove();
        }
        centerMarkerList.clear();
    }

    private void clearAllMarkersOnPolyline() {
        for (int i = 0; i < mPolyLineMarkers.size(); i++) {
            mPolyLineMarkers.get(i).remove();
        }
        mPolyLineMarkers.clear();
    }

    private void clearAllPolylines() {
        for (int i = 0; i < mPolyLines.size(); i++) {
            mPolyLines.get(i).remove();
        }
        mPolyLines.clear();
    }

    private void drawMarkersOnPoint() {
        if (mCenterPoints == null) {
            Log.d(TAG, "mCenterPoints is null");
            return;
        }
        draw_type = MAP_DRAW_TYPE.DRAW_POINT;
        if (!mCenterPoints.isEmpty()) {
            for (LatLng latLng : mCenterPoints) {
                Marker marker = drawMarkerAtLatLng(latLng);
                centerMarkerList.add(marker);
                zoomLatlngAtRoute = latLng;
            }
        }
    }

    private void drawMarkersOnPolyline() {
        if (mPoints == null) {
            Log.d(TAG, "mPoints is null");
            return;
        }
        draw_type = MAP_DRAW_TYPE.DRAW_LINE;
        if (!mPoints.isEmpty()) {
            for (LatLng latLng : mPoints) {
                Marker marker = drawMarkerAtLatLng(latLng);
                mPolyLineMarkers.add(marker);
                zoomLatlngAtRoute = latLng;
            }
        }
    }

    private void drawPolyLines() {
        if (mPoints == null) {
            Log.d(TAG, "mPoints is null");
            return;
        }
        for (int i = 0; i < mPoints.size() - 1; i += 1) {
            Polyline polyline = drawPolyLineAt(mPoints.get(i), mPoints.get(i + 1));
            mPolyLines.add(polyline);
        }
    }


    private Polyline drawPolyLineAt(LatLng start, LatLng end) {
        Polyline polyline = null;
        if (mMap != null) {
            polyline = mMap.addPolyline(new PolylineOptions().color(Color.BLACK).add(start, end));
        }
        return polyline;
    }

    private Marker drawMarkerAtLatLng(LatLng latLng) {
        Marker marker = null;

        Bitmap smallMarker = null;
        if (draw_type == MAP_DRAW_TYPE.DRAW_LINE) {
            int height = 50;
            int width = 50;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.mipmap_round_marker);
            Bitmap b = bitmapdraw.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        }
        if (draw_type == MAP_DRAW_TYPE.DRAW_POINT) {
            int height = 100;
            int width = 100;
            BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.mipmap.map_marker);
            Bitmap b = bitmapdraw.getBitmap();
            smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        }

        if (mMap != null) {
            marker = mMap.addMarker(new MarkerOptions().position(latLng));
            if (smallMarker != null) {
                marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                if (draw_type == MAP_DRAW_TYPE.DRAW_LINE) {
                    marker.setAnchor(0.5f, 0.5f);
                }
            }
            marker.setVisible(true);
        }
        return marker;
    }

    private void saveMapData(String filepath) {
        if (mPoints.isEmpty()) {
            return;
        }

        JSONObject mapObjects = null;

        ArrayList<Position> polyLineMarkerPositionList = new ArrayList<>();
        ArrayList<Position> centerMarkerPositionList = new ArrayList<>();

        for (LatLng latLng : mPoints) {
            Position position = new Position(latLng.latitude, latLng.longitude);
            polyLineMarkerPositionList.add(position);
        }

        for (LatLng latLng : mCenterPoints) {
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

        Log.d(TAG, "isSharedFile " + isSharedFile);
        if (mapObjects != null) {
            File file;
            if (!isSharedFile) {
                file = getCreatedRouteFileObject(filepath);
                writeToFile(file.getAbsolutePath(), mapObjects.toString());
            } else if (isSharedFile) {
                file = getSharedRouteFileObject(filepath);
                writeToFile(file.getAbsolutePath(), mapObjects.toString());
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
        if (mIsDrawMovableLine) {
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
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), mZoom));
            }
        } else {
            Log.d(TAG, "location is null");
            mIsCameraNotMovedToCurrentLoc = true;
        }
    }

    private Location getLocation() {
      /*  Location location = null;
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
        }*/

        return mLastLocation;
    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged : " + location.toString());
        mLastLocation = location;
        if (mIsCameraNotMovedToCurrentLoc) {
            moveCameraToCurrentLocation();
            zoomToRoute(); //FIXME: this is temporary solution, need to fix
            mIsCameraNotMovedToCurrentLoc = false;
        }
        if (isRecordRoute) {
            if (mIsRecodrdingStarted) {
                if (mPoints.size() <= 0) {
                    flag = 1;
                }
                if (flag == 1) {
                    mSavedLocation = mLastLocation;
                    drawRecordedLines(new LatLng(mSavedLocation.getLatitude(), mSavedLocation.getLongitude()));
                    flag = 2;
                } else {
                    float dist = mSavedLocation.distanceTo(mLastLocation);
                    Log.d(TAG, "distance : " + dist + " mMinRecordDistance: " + mMinRecordDistance);
                    if (dist >= mMinRecordDistance) {
                        //drawMarkerAtLatLng(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                        mSavedLocation = mLastLocation;
                        drawRecordedLines(new LatLng(mSavedLocation.getLatitude(), mSavedLocation.getLongitude()));
                    }
                }
            }
        }

    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        mOptionsMenu = menu;
        item = mOptionsMenu.findItem(R.id.action_menu_done);
        item.setVisible(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                Log.d(TAG, "Done button clicked.......");
                if (incomingFileName != null) {
                    Log.d(TAG, "incomingFileName " + incomingFileName);
                    saveMapData(incomingFileName);
                    layout.hide();
                    item.setVisible(false);
                } else {
                    if (isSharedFile) {
                        showSaveDialog();
                    }
                }
                break;
            case R.id.map_menu_help:
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
                break;

        }
        return true;
    }


    /////////////////////////

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    //////////////////////////

}
