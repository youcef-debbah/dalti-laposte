package com.dalti.laposte.admin.entity;

import com.dalti.laposte.core.entity.ActivationsInfo;
import com.dalti.laposte.core.entity.AlarmsInfo;

import dz.jsoftware95.queue.api.AlarmInfo;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.api.Situation;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.queue.api.UpdateResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface AdminAPI {

    @PATCH(GlobalConf.API_SET_TOKENS)
    Call<UpdateResult> setTokens(@Query("id") long progressID,
                                 @Query("current") Integer current,
                                 @Query("waiting") Integer waiting,
                                 @Query("availability") Integer availability,
                                 @Query("username") String username,
                                 @Query("password") String password,
                                 @Query("target") int target,
                                 @Body Situation situation);

    @PATCH(GlobalConf.API_REST_TOKENS)
    Call<UpdateResult> resetTokens(@Query("id") long progressID,
                                   @Query("username") String username,
                                   @Query("password") String password,
                                   @Query("target") int target,
                                   @Body Situation situation);

    @PATCH(GlobalConf.API_SET_NOTE)
    Call<UpdateResult> setNote(@Query("id") long progressID,
                               @Query("note_eng") String noteEng,
                               @Query("note_fre") String noteFre,
                               @Query("note_arb") String noteArb,
                               @Query("note_state") Integer noteState,
                               @Query("close_time") Long closeTime,
                               @Query("username") String username,
                               @Query("password") String password,
                               @Query("target") int target,
                               @Body Situation situation);

    @GET(GlobalConf.API_ADMIN_ACTIVATIONS)
    Call<ActivationsInfo> getActivations(@Query("username") String username,
                                         @Query("password") String password,
                                         @Query("target") int target);

    @POST(GlobalConf.API_ACTIVATE_ADMIN)
    Call<ServerResponse> activateAdmin(@Query("app-id") String applicationID,
                                       @Query("app-version") int applicationVersion,
                                       @Query("android-version") int androidVersion,
                                       @Query("google-services") Long googleServices,
                                       @Query("username") String username,
                                       @Query("password") String password,
                                       @Query("target") int target,
                                       @Header(GlobalConf.APP_CHECK_HEADER) String appCheckToken);

    @PUT(GlobalConf.API_PUT_ADMIN_ALARM)
    Call<AlarmsInfo> putAdminAlarm(@Query("username") String username,
                                   @Query("password") String password,
                                   @Query("target") int target,
                                   @Query("confirm") boolean confirm,
                                   @Query("id") Long id,
                                   @Body AlarmInfo info);

    @PUT(GlobalConf.API_DELETE_ADMIN_ALARM)
    Call<AlarmsInfo> deleteAdminAlarm(@Query("username") String username,
                                      @Query("password") String password,
                                      @Query("target") int target,
                                      @Query("id") long id);

    @GET(GlobalConf.API_ADMIN_ALARMS)
    Call<AlarmsInfo> getAdminAlarms(@Query("username") String username,
                                    @Query("password") String password,
                                    @Query("target") int target);
}
