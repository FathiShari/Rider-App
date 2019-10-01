package com.example.riderapp.RetrofitController;

import com.example.riderapp.Model.FCMResponse;
import com.example.riderapp.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAARDjd5Zc:APA91bEywPqze6qa6XrW1ltr1-GULPK1n7371dUZJh3bxaJ-QBixiJ5pdpEVP1D2x2J98hSeQAC3OaX0uqetKIsY_RCCLTH0qdWUkJM1uLpfL2ZW1xhFAWGeHM28jfH6Z4LPnYtA0fS4"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}
