package org.example;

import org.example.models.ActionsDTO;
import org.example.responses.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface Api {
    @POST("play/zombidef/command")
    Call<ActionsResponse> makeAction(@Body ActionsDTO dto, @Header("X-Auth-Token") String token);

    @PUT("play/zombidef/participate")
    Call<ParticipateResponse> participate(@Header("X-Auth-Token") String token);

    @GET("play/zombidef/units")
    Call<ChangingEnvironmentResponse> getChangingEnvironment(@Header("X-Auth-Token") String token);

    @GET("play/zombidef/world")
    Call<ConstantEnvironmentResponse> getConstantEnvironment(@Header("X-Auth-Token") String token);

    @GET("rounds/zombidef")
    Call<RoundsResponse> getRoundsInfo(@Header("X-Auth-Token") String token);
}
