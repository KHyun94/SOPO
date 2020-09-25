package com.delivery.sopo.repository

import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelRepository {
   suspend fun getRemoteOngoingParcels(): MutableList<Parcel>?
   suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>?
   suspend fun getLocalParcelById(regDt: String, parcelUid: String): ParcelEntity?
   suspend fun getLocalOngoingParcels(): MutableList<Parcel>?
   suspend fun saveLocalOngoingParcels(parcelList: List<Parcel>)
   suspend fun saveLocalOngoingParcel(parcel: ParcelEntity)
   suspend fun updateLocalOngoingParcel(parcel: ParcelEntity)
   suspend fun deleteRemoteOngoingParcels(): APIResult<String?>
   suspend fun deleteLocalOngoingParcelsStep1(parcelIdList: List<ParcelId>)
   suspend fun deleteLocalOngoingParcelsStep2()
   // 0922 kh 추가사항
   suspend fun getSingleParcelWithWaybilNum(waybilNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?
}