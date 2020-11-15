package com.delivery.sopo.networks.api

import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.google.gson.JsonArray
import retrofit2.Call
import retrofit2.http.*

interface ParcelAPI
{
    @FormUrlEncoded
    @POST("api/v1/sopo-api/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    fun postParcel(
        @Path("email") email: String,
        @Field("parcelAlias") parcelAlias: String?,
        @Field("trackCompany") trackCompany: String,
        @Field("trackNum") trackNum: String
    ): Call<APIResult<ParcelId?>>

    @GET("api/v1/sopo-api/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    suspend fun getParcel(
        @Path("email") email: String,
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): APIResult<Parcel?>

    @GET("api/v1/sopo-api/delivery/{email}/months")
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

//<<<<<<< HEAD

//=======
//    @PATCH("/api/v1/sopo-api/delivery/{email}/parcels")
//    @Headers("Accept: application/json")
//    suspend fun requestRenewal2(@Path("email") email: String): APIResult<String?>
//
//    // 0915 추가 - 택배 상태 업데이트 체크 api
//    @PATCH("/api/v1/sopo-api/delivery/{email}/parcels")
//    @Headers("Accept: application/json")
//    suspend fun requestRenewal(@Path("email") email: String): APIResult<String?>?
//
//    // 0923 추가 - 택배 상태 설정에 따른 해당 상태 택배 전부 불러오기 api
//    @GET("/api/v1/sopo-api/delivery/{email}/parcels/{status}")
//    @Headers("Accept: application/json")
//    suspend fun requestRenewal(
//        @Path("email") email: String,
//        @Path("status") status: Int
//    ): Call<APIResult<String?>>
//
//    // 1002 단일 택배 업데이트 및 가져오기
//    @PATCH("/api/v1/sopo-api/delivery/{email}/parcel")
//>>>>>>> feature-revise

    // 0915 추가 - 택배 상태 업데이트 체크 api
    @POST("/api/v1/sopo-api/delivery/{email}/parcels/refresh")
    @Headers("Accept: application/json")
    suspend fun parcelsRefreshing(@Path("email") email: String): APIResult<String?>

    // 1002 단일 택배 업데이트 및 가져오기
    @POST("/api/v1/sopo-api/delivery/{email}/parcel/{regDt}/{parcelUid}/refresh")
    @Headers("Accept: application/json")
    fun parcelRefreshing(
        @Path("email") email: String,
        @Path("regDt") regDt : String,
        @Path("parcelUid") parcelUid : String
    ) : Call<APIResult<Parcel?>>

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
}