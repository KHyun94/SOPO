package com.delivery.sopo.networks.api

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.data.repository.database.room.dto.DeleteParcelsDTO
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.models.parcel.ParcelDTO
import retrofit2.Response
import retrofit2.http.*

interface ParcelAPI
{
    /**
     * 단일 택배 등록 요청
     * @param parcelAlias
     * @param courier
     * @param waybillNum
     * @return Response<APIResult<ParcelId?>>
     */
    @POST("api/v1/sopo-parcel/delivery/parcel")
    @Headers("Accept: application/json")
    suspend fun registerParcel(@Body registerDto: ParcelRegisterDTO): Response<APIResult<Int>>

    /**
     * 단일 택배 정보 요청
     * @param parcelid
     * @return parcel
     */

    @GET("/api/v1/sopo-parcel/delivery/parcel/{parcelId}")
    @Headers("Accept: application/json")
    suspend fun getParcel(@Path("parcelId") parcelId: Int): Response<APIResult<ParcelDTO>>


    @GET("api/v1/sopo-parcel/delivery/parcels/months")
    @Headers("Accept: application/json")
    suspend fun getMonths(): Response<APIResult<MutableList<CompletedParcelHistory>>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-parcel/delivery/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getParcelsOngoing(): APIResult<MutableList<ParcelDTO>?>

    // '배송완료' 리스트 가져오는 api
    @GET("api/v1/sopo-parcel/delivery/parcels/complete")
    @Headers("Accept: application/json")
    suspend fun getParcelsComplete(
            @Query("page") page: Int, @Query("inquiryDate") inquiryDate: String): Response<APIResult<MutableList<ParcelDTO>?>>

    @HTTP(method = "DELETE", path = "api/v1/sopo-parcel/delivery/parcels", hasBody = true)
    @Headers("Accept: application/json")
    suspend fun deleteParcels(@Body parcelIds: DeleteParcelsDTO): APIResult<String?>

    // alias 변경
    @PATCH("api/v1/sopo-parcel/delivery/parcel/alias")
    @Headers("Content-Type: application/json")
    suspend fun updateParcelAlias(@Body req: UpdateAliasRequest): Response<APIResult<Unit?>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-parcel/delivery/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun getOngoingParcels(): Response<APIResult<List<ParcelDTO>?>>

    /**
     * 택배 리스트 전체 업데이트 요청
     * 서버에서 해당 통신 이외로 업데이트 데이터를 FCM을 통해 던진다.
     * @return
     */
    @POST("/api/v1/sopo-parcel/delivery/parcels/refresh")
    @Headers("Accept: application/json")
    suspend fun requestParcelsForRefresh(): Response<APIResult<String?>>

    /**
     * 택배 리스트 단일 업데이트 요청
     * @param email
     * @param regDt
     * @param parcelUid
     * @return
     * @httpCode 204 -> not update
     * @httpCode 303 -> update
     */
    @POST("/api/v1/sopo-parcel/delivery/parcel/refresh")
    @Headers("Accept: application/json")
    suspend fun requestParcelForRefresh(@Body parcelId: Map<String, Int>): Response<APIResult<Unit>>


}