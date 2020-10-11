package com.delivery.sopo.repository

import androidx.lifecycle.LiveData
import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelRepository {
   suspend fun getRemoteOngoingParcels(): MutableList<Parcel>?
   suspend fun getRemoteOngoingParcel(regDt: String, parcelUid: String): Parcel?
   suspend fun getRemoteMonthList(): MutableList<TimeCountDTO>?

   suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>?

   suspend fun getLocalParcelById(regDt: String, parcelUid: String): ParcelEntity?
   fun getLocalOngoingParcelsLiveData(): LiveData<List<Parcel>>
   suspend fun getLocalOngoingParcels(): List<Parcel>?

   fun getSoonDataCntLiveData(): LiveData<Int>
   fun getOngoingDataCntLiveData(): LiveData<Int>

   suspend fun saveLocalOngoingParcels(parcelList: List<Parcel>)
   suspend fun saveLocalOngoingParcel(parcel: ParcelEntity)
   suspend fun updateLocalOngoingParcel(parcel: ParcelEntity)
   suspend fun updateLocalOngoingParcels(parcelList: List<ParcelEntity>)
   suspend fun deleteLocalOngoingParcels(parcelIdList: List<ParcelId>)
   suspend fun deleteRemoteParcels(): APIResult<String?>?

   // 0922 kh 추가사항
   suspend fun getSingleParcelWithWaybilNum(waybilNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?
}