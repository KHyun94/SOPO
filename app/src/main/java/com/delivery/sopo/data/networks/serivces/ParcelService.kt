package com.delivery.sopo.data.networks.serivces

import com.delivery.sopo.DateSelector
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*

interface ParcelService
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
    suspend fun registerParcel(@Body parcelRegister: Parcel.Register): Response<APIResult<Int>>

    /**
     * 단일 택배 정보 요청
     * @param parcelid
     * @return parcel
     */
    @GET("/api/v1/sopo-parcel/delivery/parcel/{parcelId}")
    @Headers("Accept: application/json")
    suspend fun fetchParcelById(@Path("parcelId") parcelId: Int): Response<APIResult<Parcel.Common>>

    @GET("/api/v1/sopo-parcel/delivery/parcels")
    @Headers("Accept: application/json")
    suspend fun fetchParcelById(@Query("parcel") parcelId: String): Response<APIResult<List<Parcel.Common>>>

    @GET("api/v1/sopo-parcel/delivery/parcels/months")
    @Headers("Accept: application/json")
    suspend fun fetchDeliveredMonth(): Response<APIResult<List<DeliveredParcelHistory>>>

    // 배송중 & 곧 도착 리스트 가져오는 api
    @GET("api/v1/sopo-parcel/delivery/parcels/ongoing")
    @Headers("Accept: application/json")
    suspend fun fetchOngoingParcels(): Response<APIResult<List<Parcel.Common>>>

    @HTTP(method = "DELETE", path = "api/v1/sopo-parcel/delivery/parcels", hasBody = true)
    @Headers("Accept: application/json")
    suspend fun deleteParcels(@Body parcelIds: HashMap<String, List<Int>>): Response<Unit>

    // alias 변경
    @PATCH("api/v1/sopo-parcel/delivery/parcel/{parcelId}/alias")
    @Headers("Content-Type: application/json")
    suspend fun updateParcelAlias(@Path("parcelId") parcelId: Int, @Body parcelAlias:Map<String, String>): Response<Unit>

    /**
     * 택배 리스트 전체 업데이트 요청
     * 서버에서 해당 통신 이외로 업데이트 데이터를 FCM을 통해 던진다.
     * @return
     */
    @POST("/api/v1/sopo-parcel/delivery/parcels/refresh")
    @Headers("Accept: application/json")
    suspend fun requestParcelsRefresh(): Response<APIResult<String>>

    /**
     * 택배 리스트 전체 업데이트 요청
     * 서버에서 해당 통신 이외로 업데이트 데이터를 FCM을 통해 던진다.
     * @return
     */
    @GET("/api/v1/sopo-parcel/delivery/parcels/complete/monthly-page-info")
    @Headers("Accept: application/json")
    suspend fun fetchCompletedDateInfo(@Query("cursorDate") cursorDate: String? = null): Response<APIResult<DateSelector>>

    // '배송완료' 리스트 가져오는 api
    @GET("api/v1/sopo-parcel/delivery/parcels/complete")
    @Headers("Accept: application/json")
    suspend fun fetchDeliveredParcelsByPaging(@Query("page") page: Int,  @Query("inquiryDate") inquiryDate: String, @Query("itemCnt") itemCnt: Int = 20): Response<APIResult<List<Parcel.Common>>>


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
    suspend fun requestParcelsUpdate(@Body parcelId: Map<String, Int>): Response<APIResult<Parcel.Updatable>>

    @POST("/api/v1/sopo-parcel/delivery/parcel/reporting")
    @Headers("Accept: application/json")
    suspend fun reportParcelStatus(@Body parcelIds: HashMap<String, List<Int>>): Response<Unit>

}