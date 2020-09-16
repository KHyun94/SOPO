package com.delivery.sopo.networks

import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import kotlinx.coroutines.Deferred
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
    fun getParcel(
        @Path("email") email: String,
        // 택배 고유 uid
        @Query("parcelUid") parcelUid: String,
        // 등록일자
        @Query("regDt") regDt: String
    ): Call<APIResult<String?>>

    @GET("api/v1/sopoMainBackEnd/delivery/{email}/parcels")
    @Headers("Accept: application/json")
    fun getParcelsAsync(
        @Path("email") email: String
    ): Call<APIResult<MutableList<Parcel>?>>

//    @FormUrlEncoded
//    @DELETE("api/v1/sopoMainBackEnd/delivery/{email}/delete-parcels")
    @HTTP(method = "DELETE", path = "api/v1/sopoMainBackEnd/delivery/{email}/parcels", hasBody = true)
    @Headers("Accept: application/json")
    fun deleteParcels(
        @Path("email") email: String,
        @Body parcelIds: DeleteParcelsDTO
    ): Call<APIResult<String?>>

    // 0915 추가 - 택배 상태 업데이트 체크 api
    @FormUrlEncoded
    @PATCH("/api/v1/sopoMainBackEnd/delivery/{email}/parcels")
    @Headers("Accept: application/json")
    fun requestRenewal(@Path("email") email: String) : Call<APIResult<String?>>

}