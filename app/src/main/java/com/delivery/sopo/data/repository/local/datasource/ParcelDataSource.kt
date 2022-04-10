package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory

import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel

interface ParcelDataSource {
   suspend fun getOngoingParcelsFromRemote(): List<Parcel.Common>
   suspend fun getRemoteMonths(): List<CompletedParcelHistory>?

   suspend fun getCompleteParcelsByRemote(page: Int, inquiryDate: String): List<Parcel.Common>

   suspend fun getLocalParcelById(parcelId: Int): Parcel.Common?

   fun getLocalCompleteParcelsLiveData(): LiveData<List<Parcel.Common>>
   fun getLocalCompleteParcels(): List<Parcel.Common>
   suspend fun getLocalOngoingParcels(): List<Parcel.Common>?

   fun getSoonDataCntLiveData(): LiveData<Int>
   fun getOngoingDataCntLiveData(): LiveData<Int>

   suspend fun insetEntity(parcel: ParcelEntity)
   suspend fun insertParcels(parcelResponseList: List<Parcel.Common>)

   suspend fun update(parcel: ParcelEntity): Int
    suspend fun updateLocalParcels(parcelResponseList: List<Parcel.Common>)

   suspend fun updateParcelsToDeletable(parcelIdList: List<Int>)

   // 0922 kh 추가사항
   suspend fun getSingleParcelWithWaybillNum(waybillNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?

   suspend fun isBeingUpdateParcel(parcelId:Int): LiveData<Int?>
   fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>

   // 배송 상태인 택배의 갯수
   fun getLocalOnGoingParcelCnt() : LiveData<Int>

   // 배송 중인 택배 리스트를 LiveData로 받기
   fun getLocalOngoingParcelsAsLiveData(): LiveData<List<Parcel.Common>>
}