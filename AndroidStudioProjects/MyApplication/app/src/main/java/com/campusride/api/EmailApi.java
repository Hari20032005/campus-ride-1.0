package com.campusride.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * EmailJS API Interface using recommended Retrofit approach
 * Following the standard EmailJS REST API structure
 */
public interface EmailApi {
    
    @Headers({
        "Content-Type: application/json",
        "User-Agent: CampusRide-Mobile/1.0"
    })
    @POST("api/v1.0/email/send")
    Call<Void> sendEmail(@Body EmailRequest request);
}
