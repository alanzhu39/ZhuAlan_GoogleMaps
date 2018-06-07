package com.example.alanzhu39.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private EditText locationSearch;
    private Location myLocation;
    private LocationManager locationManager;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private boolean gotMyLocationOneTime;
    private double latitude, longitude;
    private boolean notTrackingMyLocation = true;

    private static final long MIN_TIME_BW_UPDATES = 1999 * 5; //updates in ms
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng annArbor = new LatLng(42.2, 83.7);
        mMap.addMarker(new MarkerOptions().position(annArbor).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(annArbor));

        /*if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp","Failed FINE location permission check");
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},2);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp","Failed COARSE location permission check");
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},2);
        }
        if((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            mMap.setMyLocationEnabled(true);*/


        gotMyLocationOneTime = false;
        getLocation();
        locationSearch = (EditText) findViewById(R.id.editText_addr);
    }

    public void onSearch(View view) {
        String location = locationSearch.getText().toString();

        List<Address> addressList = null;
        List<Address> addressListZip = null;

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "onSearch: location= " + location);
        Log.d("MyMapsApp", "onSearch: location= " + provider);

        LatLng userLocation = null;
        try {
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: locationManager is not null");
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is " + myLocation.getLatitude() + ", " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc" + myLocation.getLatitude() + myLocation.getLongitude(), Toast.LENGTH_SHORT);
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null from getLastKnownLocation with Network provider");
                }
            } else {
                Log.d("MyMapsApp", "onSearch: myLocation is null!");
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Exception getLastKnownLocation");
            Toast.makeText(this, "Exception getLastKnownLocation", Toast.LENGTH_SHORT);
        }

        if (!location.matches("")) {
            Log.d("MyMapsApp", "onSearch: location field is populated");

            Geocoder geocoder = new Geocoder(this, Locale.US);
            Log.d("MyMapsApp", "onSearch: created a new Geocoder");

            try {
                addressList = geocoder.getFromLocationName(location, 10000,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));
                Log.d("MyMapsApp", "onSearch: created addressList");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size= " + addressList.size());

                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare()
                            + " " + address.getThoroughfare()));
                    Log.d("MyMapsApp", "onSearch: added Marker");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }

    }


    public void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //getGPS status, isProviderEnabled returns true if user has enabled gps
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled) Log.d("MyMapsApp", "getLocation: GPS enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) Log.d("MyMapsApp", "getLocation : network is enabled");

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation : no provider enabled");
            } else {
                if (isNetworkEnabled) {
                    Log.d("MyMapsApp", "getLocation: network enabled - requestion location updates");
                    //Request location updates
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    //locationManager request for GPS_PROVIDER
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGps);
                }
            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation : Exception in getLocation");
            e.printStackTrace();
        }
    }

    //LocationListener to setup callbacks for requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing one time, if so remove updates to both fps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGps);
                gotMyLocationOneTime = true;
            } else {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGps = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.GPS_PROVIDER);
            //check if doing one time, if so remove updates to both gps and network
            if (gotMyLocationOneTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotMyLocationOneTime = true;
            }

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //switch on status
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "locationListenerGps: gps available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMapsApp", "locationListenerGps: gps out of service");
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("MyMapsApp", "locationListenerGps: status temporarily unavailable");
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                default:
                    Log.d("MyMapsApp", "locationListenerGps: default");
                    if (!notTrackingMyLocation) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
            }
            //case locationProvider.AVAILABLE:
            //print log.d or toast
            //break;
            //case locationProvider.OUT_OF_SERVICE:
            //printout debug log.d
            //enable network updatse
            //break;
            //case locationProvider.TEMPORARILY_UNAVAILABLE
            //enable both network and gps
            //break;
            //default:
            //enable both network and gps
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAmarker(String provider) {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        LatLng userLocation = null;
        if (myLocation == null)
            Log.d("MyMapsApp", "dropAmarker: no location found");
        else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            if (provider == locationManager.GPS_PROVIDER) {
                //add circle for the marker with 2 outer rings
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.RED));
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.RED)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
            } else {
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(1)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.BLUE));
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(3)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
                mMap.addCircle(new CircleOptions()
                        .center(userLocation)
                        .radius(5)
                        .strokeColor(Color.BLUE)
                        .strokeWidth(2)
                        .fillColor(Color.TRANSPARENT));
            }
            mMap.animateCamera(update);
        }
    }

    public void trackMyLocation (View view){
            //kick off location tracker using getLocation to srtart the locationListeners
        if (notTrackingMyLocation) {
            getLocation();
            notTrackingMyLocation = false;
            Log.d("MyMapsApp", "trackMyLocation: started tracking location");

        }
        else {
            //removeUpdates for both network and gps
            locationManager.removeUpdates(locationListenerGps);
            locationManager.removeUpdates(locationListenerNetwork);
            Log.d("MyMapsApp", "trackMyLocation: stopped tracking location");
            notTrackingMyLocation = true;
        }
    }

    public void clearMarkers(View view) {
        mMap.clear();
    }
}