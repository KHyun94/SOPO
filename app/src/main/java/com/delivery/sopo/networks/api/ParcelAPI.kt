package com.delivery.sopo.networks.api

import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ParcelAPI
{
    /**
     * 단일 택배 등록 요청
     * @param email
     * @param parcelAlias
     * @param trackCompany
     * @param trackNum
     * @return Response<APIResult<ParcelId?>>
     */
    @FormUrlEncoded
    @POST("api/v1/sopo-api/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    suspend fun registerParcel(
        @Path("email") email: String,
        @Field("parcelAlias") parcelAlias: String?,
        @Field("trackCompany") trackCompany: String,
        @Field("trackNum") trackNum: String
    ): Response<APIResult<ParcelId?>>

    @GET("api/v1/sopo-api/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    suspend fun getParcel(
        @Path("email") email: String,
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): APIResult<Parcel?>

    @GET("api/v1/sopo-api/delivery/{email}/parcels/months")
    @Headers("Accept: application/json")
    suspend fun getMonths( @Path("email") email: String): APIResult<MutableList<TimeCountDTO>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/{email}/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getParcelsOngoing(
        @Path("email") email: String
    ): APIResult<MutableList<Parcel>?>

    // '배송완료' 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/{email}/parcels/complete")
    @Headers("Accept: application/json")
    suspend fun getParcelsComplete(
        @Path("email") email: String,
        @Query("page") page: Int,
        @Query("inquiryDate") inquiryDate: String
    ): APIResult<MutableList<Parcel>?>

    @HTTP(
        method = "DELETE",
        path = "api/v1/sopo-api/delivery/{email}/parcels",
        hasBody = true
    )
    @Headers("Accept: application/json")
    suspend fun deleteParcels(
        @Path("email") email: String,
        @Body parcelIds: DeleteParcelsDTO
    ): APIResult<String?>

    // alias 변경
    @PATCH("api/v1/sopo-api/delivery/{email}/parcel/{regDt}/{parcelUid}")
    @Headers("Content-Type: application/json-patch+json")
    fun patchParcel(
        @Path("email") email : String,
        @Path("regDt") regDt : String,
        @Path("parcelUid") parcelUid : String,
        @Body jsonPATCH: JsonArray
    ): Call<APIResult<ParcelEntity?>>


    // 1113
    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/{email}/parcels/ongoing")
    @Headers("Accept: application/json")
    fun getParcelsOngoingTmp(
        @Path("email") email: String
    ): Call<APIResult<MutableList<Parcel>?>?>


    /**
     * 택배 리스트 전체 업데이트 요청
     * 서버에서 해당 통신 이외로 업데이트 데이터를 FCM을 통해 던진다.
     * @param email
     * @return
     */
    @POST("/api/v1/sopo-api/delivery/{email}/parcels/refresh")
    @Headers("Accept: application/json")
    suspend fun requestRefreshParcels(@Path("email") email: String): Response<APIResult<String?>>

    /**
     * 택배 리스트 단일 업데이트 요청
     * @param email
     * @param regDt
     * @param parcelUid
     * @return
     * @httpCode 204 -> not update
     * @httpCode 303 -> update
     */
    @POST("/api/v1/sopo-api/delivery/{email}/parcel/{regDt}/{parcelUid}/refresh")
    @Headers("Accept: application/json")
    suspend fun requestRefreshParcel(
        @Path("email") email: String,
        @Path("regDt") regDt : String,
        @Path("parcelUid") parcelUid : String
    ) : Response<APIResult<Any?>>

    /**
     * 단일 택배 정보 요청
     * @param email
     * @param regDt
     * @param parcelUid
     * @return parcel
     */
    @GET("api/v1/sopo-api/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    fun getSingleParcel(
        @Path("email") email: String,
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): Response<APIResult<Parcel?>>
}