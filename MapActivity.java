package ve.com.edu.ucab.testmap.testmap;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MainActivity";
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    Context context = this;
    //Var
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GeoApiContext mGeoApiContext = null;
    private LatLng mLatLng;
    private MarkerOptions mClusterMarkers;
    private Polyline polyline;
    private int constante = 0;
    private ArrayList<Marker> mTripMarkers = new ArrayList<>();
     //widgets
    Spinner spinner;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        spinner = (Spinner) findViewById(R.id.spinner1);
        getLocationPermission();
    }


    public void zoomRoute(List<LatLng> lstLatLngRoute) {

        if (mMap == null || lstLatLngRoute == null || lstLatLngRoute.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (LatLng latLngPoint : lstLatLngRoute)
            boundsBuilder.include(latLngPoint);

        int routePadding = 120;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding),
                600,
                null
        );
    }

    private void getLocationPermission() {
        Log.d(TAG, "GetLocationPermission: Success");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        //Buscar en Internet ACCESS Couse Location, Fine Location
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }


    }

    private void init() {

        List list = new ArrayList();
        list.add("Seleccionar Parque o Plaza");
        list.add("Parque Boyaca Chacao");
        list.add("Plaza Bolivar Chacao");
        list.add("central madeirense marques");
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.
                simple_dropdown_item_1line, list);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter); // se une la lista con el spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                geolocate(String.valueOf(spinner.getSelectedItem()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void resetMap(){
        if(mMap != null) {
            mMap.clear();

            if(mClusterMarkers!= null){
                mClusterMarkers.visible(false);
            }

            if(constante != 0) {
                polyline.remove();
            }
        }
    }

    private void geolocate(String posicion) {
        Log.d(TAG, "geoLocate: geolocating");

            if (posicion != "Seleccionar Parque o Plaza") {
                resetMap();
                String searchString = posicion;
                Geocoder geocoder = new Geocoder(MapActivity.this);
                List<Address> list = new ArrayList<>();
                try {
                    list = geocoder.getFromLocationName(searchString, 1);
                } catch (IOException e) {
                    Log.e(TAG, "geoLocate: IOException" + e.getMessage());
                }
                if (list.size() > 0) {
                    Address address = list.get(0);
                    Log.d(TAG, "geoLocate: found a location" + address.toString());
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15f,
                            address.getAddressLine(0));
                }
            }
        //constante++;
        }





    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: ubicacion  del dispositivo");
        mFusedLocationProviderClient = LocationServices.
                getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();

                location.addOnCompleteListener(new OnCompleteListener() {
                     @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Ubicacion encontrada");
                            Location currentLocation = (Location) task.getResult();

                            mLatLng = new LatLng (currentLocation.getLatitude(),currentLocation.getLongitude());
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()), 15f,
                                    "Mi ubicacion");

                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this,
                                    "unable to get cyrrent location", Toast.LENGTH_SHORT).
                                    show();

                        }

                    }
                });

            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());

        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera : mueve la camara a las coordenadas dadas:" +
                latLng.latitude + "," + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        if (!title.equals("Mi ubicacion")) {
            mClusterMarkers = new MarkerOptions().position(latLng).title(title).snippet("Deseas llegar a esta posicion");
            mClusterMarkers.visible(true);
            mMap.addMarker(mClusterMarkers);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    //inicia el mapa
                    initMap();
                }
            }
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
        if (mGeoApiContext == null) {
            mGeoApiContext = new GeoApiContext.Builder().
                    apiKey(getString(R.string.google_maps_API_key)).build();

        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.
                    PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            // no me muestra el boton de localizacion
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            //mMap.getUiSettings().setZoomControlsEnabled(true);
            init();
            mMap.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(marker.getSnippet())
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {

                        Log.d(TAG, "calculateDirections: destination: " + mLatLng.latitude + ","+ mLatLng.longitude);
                        calculateDirections(marker,mLatLng);
                        dialog.dismiss();

                        //removeTripMarkers();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void calculateDirections(Marker marker , LatLng origen){
        Log.d(TAG, "calculateDirections: calculating directions.");

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                marker.getPosition().latitude,
                marker.getPosition().longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);
        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        origen.latitude,
                        origen.longitude
                )
        );

        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());
                addPolylinesToMap(result);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }


    private void addPolylinesToMap(final DirectionsResult result){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);

                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    //This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

                     Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    constante++;
                    polyline.setColor(R.color.Blue);
                    //polyline.setColor(ContextCompat.getColor(getResources(), R.color.ruta));
                    polyline.setClickable(true);
                    zoomRoute(polyline.getPoints());

                }
            }
        });
    }
    @Override
    public void onClick(View view) {

    }




}
