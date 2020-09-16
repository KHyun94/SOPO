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
    fun postParcel(
        @Path("email") email: String,
        @Field("parcelAlias") parcelAlias: String,
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
        @Path("email") email: String
    ): APIResult<MutableList<Parcel>?>


    @HTTP(method = "DELETE", path = "api/v1/sopoMainBackEnd/delivery/{email}/parcels", hasBody = true)
    @Headers("Accept: application/json")
    suspend fun deleteParcels(
        @Path("email") email: String,
        @Body parcelIds: DeleteParcelsDTO
    ): APIResult<String?>


}