package shareroute.nazib.com.shareroute;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener,
        LocationListener{
    private static final String TAG = "[SHARE_ROUTE]";
    Context context;
    GoogleMap mMap;
    private FABToolbarLayout layout;
    private View one, two, three, four;
    private View fab;
    ArrayList<LatLng> mPoints;
    Polyline mMovablePolyLine;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_map);
        context = this;
        mMap = null;
        mPoints = new ArrayList<LatLng>();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });


        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //

        //
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

    private void drawMarkerOnCenter() {
        LatLng center = mMap.getCameraPosition().target;

        //float max_zoom = mMap.getMaxZoomLevel();

        mMap.addMarker(new MarkerOptions().position(center)).setVisible(true);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMovablePolyLine = null;

        mMap = map;
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted");
            return;
        }
//        map.setMyLocationEnabled(true);
//
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//
//        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
//        if (location != null)
//        {
//            Log.d(TAG, "lat: "+location.getLatitude()+" long: "+ location.getLongitude());
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
//
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
//                    .zoom(17)                   // Sets the zoom
//                    .bearing(90)                // Sets the orientation of the camera to east
//                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
//                    .build();                   // Creates a CameraPosition from the builder
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        }

    }

    @Override
    public void onClick(View view) {

        Toast.makeText(this, "Element clicked", Toast.LENGTH_SHORT).show();

        //drawMarkerOnCenter();
        drawLinesToCenter();
        //moveCameraToCurrentLocation();
    }


    private void drawLinesToCenter() {
        LatLng center = mMap.getCameraPosition().target;
        mMap.addMarker(new MarkerOptions().position(center)).setVisible(true);
        mPoints.add(center);
        mMap.addPolyline(new PolylineOptions().color(Color.RED).addAll(mPoints));

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
        //Log.d(TAG, "onCameraChange " + center.toString());

        if (mMovablePolyLine != null) {
            mMovablePolyLine.remove();
        }

        if (!mPoints.isEmpty()) {
            LatLng lastLatLng = mPoints.get(mPoints.size() - 1);
            //Log.d(TAG, "onCameraChange " + lastLatLng.toString());
            mMovablePolyLine = mMap.addPolyline(new PolylineOptions().color(Color.BLUE).add(center, lastLatLng));

        }
    }

    private void moveCameraToCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission not granted");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION
            );
            return;
        }
        mMap.setMyLocationEnabled(true);


        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location =/* locationManager.getLastKnownLocation(provider);*/getLocation();
        if (location != null) {
            Log.d(TAG, "location : " + location.toString());
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
                            Log.d(TAG, "location "+location.toString());
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION:
                Log.d(TAG, "Location Request granted");
                break;
        }
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
}
