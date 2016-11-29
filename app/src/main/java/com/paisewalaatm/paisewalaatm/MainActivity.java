package com.paisewalaatm.paisewalaatm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.R.attr.name;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int SMS_PERMISSION_REQ = 515;
    private static final long LOCATION_REFRESH_TIME = 4;
    private static final float LOCATION_REFRESH_DISTANCE = 1;
    private static final int PERMISSION_LOCATION_REQ = 516;
    private RecyclerView recyclerView;
    private List<AtmResponse.AtmObject> dataList = new ArrayList<>();
    private LocationManager mLocationManager;
    private GPSTracker gps;
    private CustomRecyclerViewAdapter viewAdapter;
    private View emptyView;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        emptyView = findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        viewAdapter = new CustomRecyclerViewAdapter(this, dataList);
        recyclerView.setAdapter(viewAdapter);
        viewAdapter.setOnItemClickListener(new CustomRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int adapterPosition) {
                Log.d(TAG, "onItemClick: " + adapterPosition);
            }
        });
        showOrHideEmptyView();
        requestSmsReadPermission();
        requestLocationPermission();
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        mAdView = (AdView) findViewById(R.id.adView);
        scheduleSyncService();
        GAEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);
    }

    private void GAEvents() {
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Share")
                .build());
    }

    private void showOrHideEmptyView() {
        if (dataList.size() == 0) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQ);
        } else {
            getGpsLocation();
            makeApiRequest();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                LOCATION_REFRESH_DISTANCE, mLocationListener);
    }

    private void makeApiRequest() {
        String location = PreferenceManager.getLocation(this);
        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<AtmResponse> responseCall = apiInterface.getAtms(location);
        responseCall.enqueue(new Callback<AtmResponse>() {
            @Override
            public void onResponse(Call<AtmResponse> call, Response<AtmResponse> response) {
                Log.i(TAG, "onResponse: " + call.request() + ",Response: " + response.message());
                AtmResponse atmResponse = response.body();
                List<AtmResponse.AtmObject> atmObjects = atmResponse.AtmObjectList;
                dataList.clear();
                dataList.addAll(atmResponse.AtmObjectList);
                showOrHideEmptyView();
                viewAdapter.notifyDataSetChanged();
                Gson gson = new Gson();
                String atmString = gson.toJson(atmResponse);
                PreferenceManager.saveAtms(MainActivity.this, atmString);
                Log.i(TAG, "onResponse: " + atmString);
            }

            @Override
            public void onFailure(Call<AtmResponse> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage() + ",call: " + call.request());
            }
        });
    }

    private void requestSmsReadPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) !=
                        PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_REQ);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION_REQ:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //getLocation();
                    getGpsLocation();
                    makeApiRequest();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            PreferenceManager.saveCurLocation(MainActivity.this, location.getLatitude() + "," + location.getLongitude());
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
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gps != null)
            gps.stopUsingGPS();
    }

    private void getGpsLocation() {
        gps = new GPSTracker(MainActivity.this);
        // check if GPS enabled
        if (gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            PreferenceManager.saveCurLocation(MainActivity.this, latitude + "," + longitude);
            Log.i(TAG, "getGpsLocation: " + latitude + "," + longitude);
        } else {
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void scheduleSyncService() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        /*
         * set alarm at 11 am
         */
        Intent receiverIntent = new Intent(getApplicationContext(), SyncData.class);
        calendar.set(Calendar.HOUR_OF_DAY, 11);
        Long calTime = calendar.getTimeInMillis();
        if (calTime < System.currentTimeMillis())
            calTime += 1000 * 60 * 60 * 24L;
        PendingIntent alarmIntent1 = PendingIntent.getService(getApplicationContext(), 1, receiverIntent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calTime,
                AlarmManager.INTERVAL_DAY, alarmIntent1);

        /*
         * set alarm at 8 pm
         */
        Intent receiverIntent1 = new Intent(getApplicationContext(), SyncData.class);
        PendingIntent pi1 = PendingIntent.getService(getApplicationContext(), 4, receiverIntent1, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calTime = calendar.getTimeInMillis();
        if (calTime < System.currentTimeMillis())
            calTime += 1000 * 60 * 60 * 24L;

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calTime,
                AlarmManager.INTERVAL_DAY, pi1);
    }
}
