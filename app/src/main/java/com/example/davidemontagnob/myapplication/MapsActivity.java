package com.example.davidemontagnob.myapplication;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
       View.OnClickListener, android.location.LocationListener {


    private GoogleMap mMap;
    private Database_helper myDb;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private ImageView btn_search;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 100000;
    double latitude, longitude;
    private static final float DEFAULT_ZOOM = 18f; //not to save
    private LocationManager locationManager;
    private  ArrayList<String> places_to_search = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        myDb = new Database_helper(this);
        mapFragment.getMapAsync(this);
        btn_search = findViewById(R.id.gps_my_location);
    }

    @Override
    protected void onResume() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager != null)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, this);

        super.onResume();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission Denied" , Toast.LENGTH_LONG).show();
                }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        btn_search.setOnClickListener(this);

    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastlocation = location;
        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }

        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        Log.d("Proximity", "Posizione attuale: " + location.getLatitude()+","+location.getLongitude());
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.f));

        if(client != null)
        {
            locationManager.removeUpdates(this);
            //LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
        if(currentLocationmMarker!=null) showNearPlace();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void showNearPlace(){


        Object dataTransfer[] = new Object[2];
        dataTransfer[0] = mMap;
        GetNearbyPlacesData getNearbyPlacesData;
        places_to_search.clear();





        Log.d("Download", "i'm inside addMarkerPreferite");
        Cursor data = myDb.getList2();
        HashMap<String,String> tmp_list = new HashMap<>();
        if (data.getCount() != 0) {
            while (data.moveToNext()) {

                tmp_list.put(data.getString(0),data.getString(1));
            }

        }
        Log.d("Download", "List database 2 " + tmp_list.toString());
        data = myDb.getList();
        ArrayList<String> tmp_list2 = new ArrayList<>();
        if (data.getCount() != 0) {
            while (data.moveToNext()) {

                tmp_list2.add(data.getString(0));
            }

        }
        String url="";
        Log.d("Download", "List database 1 " + tmp_list2.toString());
        for(int i=0;i<tmp_list2.size();i++){
            if(tmp_list.containsKey(tmp_list2.get(i).toString())){
                if(!places_to_search.contains( tmp_list.get(tmp_list2.get(i).toString()))){
                    places_to_search.add(tmp_list.get(tmp_list2.get(i).toString()).toLowerCase());
                    if(currentLocationmMarker==null)
                    Log.d("Marker", "Current Location Marker is null");
                    else if(currentLocationmMarker!=null){
                       url = getUrl(currentLocationmMarker.getPosition().latitude, currentLocationmMarker.getPosition().longitude,tmp_list2.get(i).toLowerCase());
                        dataTransfer[1] = url;
                        getNearbyPlacesData = new GetNearbyPlacesData();
                        getNearbyPlacesData.execute(dataTransfer);

                    }


                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.gps_my_location:
                if(lastlocation!=null) {
                    Log.d("Location", lastlocation.getLatitude() + "," + lastlocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude()), DEFAULT_ZOOM));
                }
                break;

        }
    }

    private String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&key=AIzaSyDT8XcmkkHOjhLkkpnPnRvOQUG01YXPT1s");
        googlePlaceUrl.append("&sensor=true");

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

      /*  locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }*/
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

        private String googlePlacesData;

        private String url;

        @Override
        protected String doInBackground(Object... objects) {


            url = (String) objects[1];

            DownloadUrl downloadURL = new DownloadUrl();
            try {
                googlePlacesData = downloadURL.readUrl("https://api.myjson.com/bins/1cxyw0");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return googlePlacesData;



        }

        @Override
        protected void onPostExecute(String s) {

            List<HashMap<String, String>> nearbyPlaceList;
            DataParser parser = new DataParser();
            nearbyPlaceList = parser.parse(s);
            Log.d("nearbyplacesdata", "called parse method");
            try {
                showNearbyPlaces(nearbyPlaceList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) throws JSONException {
            for (int i = 0; i < nearbyPlaceList.size(); i++) {

                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

                String placeName = googlePlace.get("place_name");

                String types = googlePlace.get("types");
                JSONArray jsonArray = new JSONArray(types);
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                LatLng latLng = new LatLng(lat, lng);


                for(int j=0;j<jsonArray.length();j++){
                   // Log.d("Marker", placeName+"/"+vicinity+"/"+lat+","+lng+" ------- types:"+jsonArray.getString(j));
                    if(isIn(jsonArray.getString(j))){
                        Log.d("Marker", "Inserert ---> Nome: "+placeName+" "+jsonArray.getString(j));
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(placeName);
                        markerOptions.visible(true);
                        markerOptions.snippet("All'interno puoi trovare: " + types);

                        float[] result = new float[1];
                        Location.distanceBetween(lastlocation.getLatitude(),lastlocation.getLongitude(),latLng.latitude,latLng.longitude,
                        result);
                        if(result[0]<200){

                            mMap.addMarker(markerOptions);
                            mMap.addCircle(new CircleOptions().center(latLng).radius(20));
                        }
                        break;
                    }
                }


            }
        }
    }
    private boolean isIn(String type){
       return places_to_search.contains(type);
    }

}