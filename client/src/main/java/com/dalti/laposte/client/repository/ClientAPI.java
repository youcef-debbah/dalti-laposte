package com.dalti.laposte.client.repository;

import com.dalti.laposte.core.entity.ExternalAPI;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.api.ServerResponse;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ClientAPI extends ExternalAPI {

    @POST(GlobalConf.API_ACTIVATE_CLIENT)
    Call<ServerResponse> activateClient(@Query("activation-code") String code,
                                        @Query("app-id") String applicationID,
                                        @Query("app-version") int applicationVersion,
                                        @Query("android-version") int androidVersion,
                                        @Query("google-services") Long googleServices,
                                        @Query("target") int target,
                                        @Header(GlobalConf.APP_CHECK_HEADER) String appCheckToken);
}
