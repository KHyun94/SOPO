package com.delivery.sopo.networks

import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.kakao.usermgmt.StringSet.email
import retrofit2.Call
import retrofit2.http.*

interface ParcelAPI
{
    @FormUrlEncoded
    @POST("api/v1/sopoMainBackEnd/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    fun registerParcel(
        @Path("email") email: String,
        @Field("parcelAlias") parcelAlias: String?,
        @Field("trackCompany") trackCompany: String,
        @Field("trackNum") trackNum: String
    ): Call<APIResult<ParcelId?>>

    @GET("api/v1/sopoMainBackEnd/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    suspend fun getParcel(
        @Path("email") email: String,
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): APIResult<Parcel?>

    @GET("api/v1/sopoMainBackEnd/delivery/{email}/months")
    @Headers("Accept: application/json")
    suspend fun getMonthList( @Path("email") email: String): APIResult<MutableList<TimeCountDTO>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopoMainBackEnd/delivery/{email}/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getParcelsOngoing(
        @Path("email") email: String
    ): APIResult<MutableList<Parcel>?>

    // '배송완료' 리스트 가져오는 api
    @GET("api/v1/sopoMainBackEnd/delivery/{email}/parcels/complete")
    @Headers("Accept: application/json")
    suspend fun getParcelsComplete(
        @Path("email") email: String,
        @Query("page") page: Int,
        @Query("inquiryDate") inquiryDate: String
    ): APIResult<MutableList<Parcel>?>

    @HTTP(
        method = "DELETE",
        path = "api/v1/sopoMainBackEnd/delivery/{email}/parcels",
        hasBody = true
    )
    @Headers("Accept: application/json")
    suspend fun deleteParcels(
        @Path("email") email: String,
        @Body parcelIds: DeleteParcelsDTO
    ): APIResult<String?>

    @PATCH("/api/v1/sopoMainBackEnd/delivery/{email}/parcels")
    @Headers("Accept: application/json")
    suspend fun requestRenewal2(@Path("email") email: String): APIResult<String?>

    // 0915 추가 - 택배 상태 업데이트 체크 api
    @PATCH("/api/v1/sopoMainBackEnd/delivery/{email}/parcels")
    @Headers("Accept: application/json")
    suspend fun requestRenewal(@Path("email") email: String): Call<APIResult<String?>>

    // 0923 추가 - 택배 상태 설정에 따른 해당 상태 택배 전부 불러오기 api
    @GET("/api/v1/sopoMainBackEnd/delivery/{email}/parcels/{status}")
    @Headers("Accept: application/json")
    suspend fun requestRenewal(
        @Path("email") email: String,
        @Path("status") status: Int
    ): Call<APIResult<String?>>

    // 1002 단일 택배 업데이트 및 가져오기
    @PATCH("/api/v1/sopoMainBackEnd/delivery/{email}/parcel")
    @Headers("Accept: application/json")
    suspend fun requestRenewalOneParcel(
        @Path("email") email: String,
        @Query("parcelUid") parcelUid : String,
        @Query("regDt") regDt : String
    ) : Call<APIResult<Parcel?>>

}