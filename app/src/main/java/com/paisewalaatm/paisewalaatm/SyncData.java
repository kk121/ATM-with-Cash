package com.paisewalaatm.paisewalaatm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.content.ContentValues.TAG;

/**
 * Created by krishna on 15/11/16.
 */

public class SyncData extends IntentService {
    public SyncData() {
        super("syncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent: start");
        /* Send this ATM details to server */
        String location = PreferenceManager.getLocation(getBaseContext());
        if (location.equals("")) return;

        String bankAtmName = PreferenceManager.getAtmName(getBaseContext());
        String timestampMillis = PreferenceManager.getTime(getBaseContext());

        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ResponseBody> bodyCall = apiInterface.insertAtm(bankAtmName, location, timestampMillis + "");
        bodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "onResponse: " + response.message());
                PreferenceManager.clearAtmDetalToSync(getBaseContext());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "onFailure: " + t.getMessage());
            }
        });
        Log.i(TAG, "onHandleIntent: end");
    }
}
