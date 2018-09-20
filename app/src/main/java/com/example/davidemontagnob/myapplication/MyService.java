package com.example.davidemontagnob.myapplication;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyService extends Service
{
    private static final String TAG = "GPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    private Location currentLocation=null;
    private Database_helper myDb;
    private   ArrayList<String> places_to_search = new ArrayList<>();
    Intent intent = new Intent("ACTION_PROXIMITY_ALERT");
    PendingIntent proximityIntent;
    IntentFilter filter = new IntentFilter("ACTION_PROXIMITY_ALERT");
    private Boolean added;
    private Boolean first_time;
    private int radius;
    private SharedPreferences prefs;
    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
        myDb = new Database_helper(this);
        proximityIntent  = PendingIntent.getBroadcast(this, 0, intent, 0);
        registerReceiver(new myLocationReceiver(),filter);
        added=false;
        first_time=true;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }
    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
            if(currentLocation==null){
                currentLocation = location;
            }
            else{

                float[] result = new float[1];

                Location.distanceBetween(currentLocation.getLatitude(),currentLocation.getLongitude(),location.getLatitude(),location.getLongitude(),result);
                Log.d("Proximity", "Distanza da ora a precedente: "+ result[0]);

                currentLocation=location;
                if(result[0]>10 &&!first_time){
                    Log.d("Proximity","Chiamate successive");
                    deleteProximityAlert();
                    showNearPlace();
                }
                if(first_time){
                    Log.d("Proximity","Prima chiamata");
                    showNearPlace();
                    first_time=false;
                }

            }

        }
        private boolean deleteProximityAlert() {
          mLocationManager.removeProximityAlert(proximityIntent);
          added=false;
            return true;
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void showNearPlace() {

            Log.d("Proximity", "sono entrato in showNearPlace");

            if (currentLocation != null) {
                Log.d("Proximity", "ed eseguo la ricerca per i posti vicini");
                Object dataTransfer[] = new Object[2];

                GetNearbyPlacesData2 getNearbyPlacesData;
                String url = "";




                Cursor data = myDb.getList2();
                HashMap<String, String> tmp_list = new HashMap<>();
                if (data.getCount() != 0) {
                    while (data.moveToNext()) {

                        tmp_list.put(data.getString(0), data.getString(1));
                    }

                }
                Log.d("Proximity", "List database 1 " + tmp_list.toString());
                data = myDb.getList();
                ArrayList<String> tmp_list2 = new ArrayList<>();
                if (data.getCount() != 0) {
                    while (data.moveToNext()) {

                        tmp_list2.add(data.getString(0));
                    }

                }
                Log.d("Proximity", "List database 2 " + tmp_list2.toString());
                for (int i = 0; i < tmp_list2.size(); i++) {
                    Log.d("Proximity", "Elemento: " +tmp_list2.get(i).toString() );
                    if (tmp_list.containsKey(tmp_list2.get(i).toString())) {
                        if (!places_to_search.contains(tmp_list.get(tmp_list2.get(i).toString()))) {
                            Log.d("Proximity", "Place to search " + places_to_search.toString());
                            places_to_search.add(tmp_list.get(tmp_list2.get(i).toString()).toLowerCase());
                            url = getUrl(currentLocation.getLatitude(), currentLocation.getLongitude(), places_to_search.get(i).toLowerCase());
                            dataTransfer[1] = url;
                            getNearbyPlacesData = new GetNearbyPlacesData2();
                            getNearbyPlacesData.execute(dataTransfer);

                        }
                    }
                }


            } else {
                Log.d("Proximity", "non ho eseguito il metodo.");
            }


    }


    private String getUrl(double latitude, double longitude, String nearbyPlace) {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");

        googlePlaceUrl.append("location=" + latitude + "," + longitude);
        googlePlaceUrl.append("&radius=" + 100000);
        googlePlaceUrl.append("&type=" + nearbyPlace);
        googlePlaceUrl.append("&key=AIzaSyDT8XcmkkHOjhLkkpnPnRvOQUG01YXPT1s");
        googlePlaceUrl.append("&sensor=true");

        Log.d("MapsActivity", "url = " + googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }


    public class GetNearbyPlacesData2 extends AsyncTask<Object, String, String> {

        private String googlePlacesData;
        private GoogleMap mMap;
        private String url;



        @Override
        protected String doInBackground(Object... objects) {

            mMap = (GoogleMap) objects[0];
            url = (String) objects[1];

            DownloadUrl downloadURL = new DownloadUrl();
            try {
                googlePlacesData = downloadURL.readUrl("https://api.myjson.com/bins/c6whk");
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
            showNearbyPlaces(nearbyPlaceList);

        }

        private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList) {
            if(prefs.getBoolean("switch_notify2",true)==false){
                return;
            }
            for (int i = 0; i < nearbyPlaceList.size(); i++) {
                HashMap<String, String> googlePlace = nearbyPlaceList.get(i);


                String types = googlePlace.get("types");
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(types);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                double lat = Double.parseDouble(googlePlace.get("lat"));
                double lng = Double.parseDouble(googlePlace.get("lng"));
                LatLng latLng = new LatLng(lat, lng);


                for(int j=0;j<jsonArray.length();j++){

                    try {
                        if(isIn(jsonArray.getString(j))){

                            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;

                            }else{

                                Log.d("Proximity", "Inserert ---> Nome: " + jsonArray.getString(j)+" LatLng: " + latLng);
                                Log.d("Proximity ",prefs.getString("notify_voltage","").toString());

                                switch(prefs.getString("notify_voltage","").toString()){
                                    case "0":
                                        mLocationManager.addProximityAlert(latLng.latitude,
                                                 latLng.longitude,  100, -1, proximityIntent);

                                        break;
                                    case "1":
                                        mLocationManager.addProximityAlert(latLng.latitude,
                                                latLng.longitude,  200, -1, proximityIntent);

                                        break;
                                    case "2":
                                        mLocationManager.addProximityAlert(latLng.latitude,
                                                latLng.longitude,  300, -1, proximityIntent);
                                        break;
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private boolean isIn(String type){
        return places_to_search.contains(type);
    }
}