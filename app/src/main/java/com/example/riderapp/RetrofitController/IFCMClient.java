package com.example.riderapp.RetrofitController;

import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Sender;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class IFCMClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl){
        if(retrofit == null){

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}
