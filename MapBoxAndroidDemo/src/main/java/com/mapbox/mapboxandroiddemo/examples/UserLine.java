package com.mapbox.mapboxandroiddemo.examples;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.lang.reflect.Array;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class UserLine extends AppCompatActivity {
    private ArrayList<LatLng> points = new ArrayList<>();
    private MapView mapView;
    private MapboxMap map;
    private String distanceStr = "";
    private double distance = 0;
    private int nbPoints = 0;
    private LatLng pointFrom = new LatLng(1000, 1000);
    FloatingActionButton floatingActionButton;
    LocationServices locationServices;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_basic);

        locationServices = LocationServices.getLocationServices(UserLine.this);

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (!locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_LOCATION);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    private int firstPoint() {
        if (pointFrom.getLongitude() == 1000 && pointFrom.getLatitude() == 1000)
            return (1);
        return (0);
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is
                        map.removeAnnotations();
                        points.add(new LatLng(location));
                        LatLng[] pointsArray = points.toArray(new LatLng[points.size()]);
                        map.addPolyline(new PolylineOptions()
                                .add(pointsArray)
                                .color(Color.parseColor("#3bb2d0"))
                                .width(4));
                        ++nbPoints;
                        LatLng pointTo = new LatLng(location);
                        if (firstPoint() == 0) {
                            distance = distance + (pointFrom.distanceTo(pointTo) * 0.001);
                        }
                        pointFrom = pointTo;
                        DecimalFormat df = new DecimalFormat("###.#");
                        df.setRoundingMode(RoundingMode.DOWN);
                        String distanceStr = df.format(distance);
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(location))
                                .title("Distance")
                                .snippet(distanceStr + " km"));
                    }
                }
            });
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            pointFrom = new LatLng(1000, 1000);
            distance = 0;
            map.removeAnnotations();
            while (--nbPoints != -1)
                points.remove(nbPoints);
            nbPoints = 0;
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_LOCATION: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableLocation(true);
                }
            }
        }
    }
}
