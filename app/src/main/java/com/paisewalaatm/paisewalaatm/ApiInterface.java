package com.paisewalaatm.paisewalaatm;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by krishna on 14/11/16.
 */
public interface ApiInterface {
    @GET("/get_atm")
    Call<AtmResponse> getAtms(@Query("location") String location);

    @FormUrlEncoded
    @POST("/insert_atm")
    public Call<ResponseBody> insertAtm(
            @Field("bankName") String bankName,
            @Field("location") String location,
            @Field("time") String time);
}
