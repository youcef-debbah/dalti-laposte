package com.dalti.laposte.core.entity;

import com.dalti.laposte.core.entity.ServicesInfo;

import dz.jsoftware95.queue.api.EventsList;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.Payload;
import dz.jsoftware95.queue.api.Situation;
import dz.jsoftware95.queue.api.WebPageInfo;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.queue.api.ServiceInfo;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface CoreAPI {

    @GET(GlobalConf.API_ALL_SERVICES_FOR_GUEST)
    Call<ServicesInfo> getServicesAsGuest(@Query("target") int target,
                                          @Query("admin") String admin);

    @PUT(GlobalConf.API_SYNC_NO_SERVICE)
    Call<ServiceInfo> syncWithoutService(@Query("key") long key,
                                         @Query("target") int target,
                                         @Body Situation situation);

    @PUT(GlobalConf.API_SYNC_SERVICE)
    Call<ServiceInfo> getTokens(@Query("id") long serviceID,
                                @Query("key") long key,
                                @Query("target") int target,
                                @Body Situation situation);

    @DELETE(GlobalConf.API_CANCEL_SMS)
    Call<ServerResponse> cancelSMS(@Query("token") String token,
                                   @Query("state") int state,
                                   @Query("key") long key);

    @DELETE(GlobalConf.API_SKIP_SMS)
    Call<ServerResponse> skipOperator(@Query("sms") String smsToken,
                                      @Query("operator") String appID,
                                      @Query("key") long key);

    @POST(GlobalConf.API_UPLOAD_EVENTS)
    Call<ServerResponse> uploadEvents(@Body EventsList eventsList);

    @GET(GlobalConf.API_GET_WEB_PAGE)
    Call<WebPageInfo> fetchWebPage(@Query("name") String pageName);

    @POST(GlobalConf.API_PONG)
    Call<ServerResponse> pong(@Body Payload payload);
}
