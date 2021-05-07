package com.delivery.sopo.networks.api

import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.dto.parcels.RegisterParcelDTO
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
    @POST("api/v1/sopo-api/delivery/parcel")
    @Headers("Accept: application/json")
    suspend fun registerParcel(
        @Body dto: RegisterParcelDTO
    ): Response<APIResult<ParcelId?>>

    @GET("api/v1/sopo-api/delivery/parcel")
    @Headers("Accept: application/json")
    suspend fun getParcel(
        
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): APIResult<Parcel?>

    @GET("api/v1/sopo-api/delivery/parcels/months")
    @Headers("Accept: application/json")
    suspend fun getMonths(): APIResult<MutableList<TimeCountDTO>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getParcelsOngoing(): APIResult<MutableList<Parcel>?>

    // '배송완료' 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/parcels/complete")
    @Headers("Accept: application/json")
    suspend fun getParcelsComplete(
        
        @Query("page") page: Int,
        @Query("inquiryDate") inquiryDate: String
    ): APIResult<MutableList<Parcel>?>

    @HTTP(
        method = "DELETE",
        path = "api/v1/sopo-api/delivery/parcels",
        hasBody = true
    )
    @Headers("Accept: application/json")
    suspend fun deleteParcels(
        @Body parcelIds: DeleteParcelsDTO
    ): APIResult<String?>

    // alias 변경
    @PATCH("api/v1/sopo-api/delivery/parcel/{regDt}/{parcelUid}")
    @Headers("Content-Type: application/json-patch+json")
    fun patchParcel(
        @Path("email") email : String,
        @Path("regDt") regDt : String,
        @Path("parcelUid") parcelUid : String,
        @Body jsonPATCH: JsonArray
    ): Call<APIResult<ParcelEntity?>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-api/delivery/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getOngoingParcels(
        @Path("email") email: String
    ): Response<APIResult<MutableList<Parcel>?>>

    /**
     * 택배 리스트 전체 업데이트 요청
     * 서버에서 해당 통신 이외로 업데이트 데이터를 FCM을 통해 던진다.
     * @param email
     * @return
     */
    @POST("/api/v1/sopo-api/delivery/parcels/refresh")
    @Headers("Accept: application/json")
    suspend fun requestParcelForRefreshs(@Path("email") email: String): Response<APIResult<String?>>

    /**
     * 택배 리스트 단일 업데이트 요청
     * @param email
     * @param regDt
     * @param parcelUid
     * @return
     * @httpCode 204 -> not update
     * @httpCode 303 -> update
     */
    @POST("/api/v1/sopo-api/delivery/parcel/refresh")
    @Headers("Accept: application/json")
    suspend fun requestParcelForRefresh(
        @Body parcelId: ParcelId
    ) : Response<APIResult<Any?>>

    /**
     * 단일 택배 정보 요청
     * @param email
     * @param regDt
     * @param parcelUid
     * @return parcel
     */
    @GET("/api/v1/sopo-api/delivery/parcel/{regDt}/{parcelUid}")
    @Headers("Accept: application/json")
    suspend fun getSingleParcel(
        @Path("regDt") regDt: String,
        @Path("parcelUid") parcelUid: String
    ): Response<APIResult<Parcel?>>

    // TODO alias 변경 api 추가해야함
}