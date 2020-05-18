package com.example.chatapp.Fragments;

import com.example.chatapp.Notification.MyResponse;
import com.example.chatapp.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {

                    "Content-Type:application/json",
                    "Authorization:key=AAAAFokhIDA:APA91bEO01UlOZAMpWZKtYd742-lCMpRsNGlAMZU3NwMYZnRxzLPgI9EBictIm2zegE5246JOt2wlB3UxeHfvAYUfrWyEZvv1IIu_5JnPHidfaKZjWiDaTo-s4COL_vfQ18UmjSPc4OZ"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
