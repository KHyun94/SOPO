package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory

import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.ParcelDTO

interface ParcelDataSource {
   suspend fun getRemoteOngoingParcels(): MutableList<ParcelDTO>?
   suspend fun getRemoteMonths(): List<CompletedParcelHistory>?

   suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<ParcelDTO>?

   suspend fun getLocalParcelById(parcelId: Int): ParcelEntity?

   fun getLocalCompleteParcelsLiveData(): LiveData<List<ParcelDTO>>
   fun getLocalCompleteParcels(): List<ParcelDTO>
   suspend fun getLocalOngoingParcels(): List<ParcelDTO>?

   fun getSoonDataCntLiveData(): LiveData<Int>
   fun getOngoingDataCntLiveData(): LiveData<Int>

   suspend fun insetEntity(parcel: ParcelEntity)
   suspend fun insertEntities(parcelDTOList: List<ParcelDTO>)

   suspend fun updateEntity(parcel: ParcelEntity): Int
    suspend fun updateEntities(parcelDTOList: List<ParcelDTO>)

   suspend fun deleteLocalParcels(parcelIdList: List<Int>)
   suspend fun deleteRemoteParcels(): APIResult<String?>?

   // 0922 kh 추가사항
   suspend fun getSingleParcelWithWaybillNum(waybillNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?

   suspend fun isBeingUpdateParcel(parcelId:Int): LiveData<Int?>
   fun getIsUnidentifiedAsLiveData(parcelId: Int): LiveData<Int?>

   // 배송 상태인 택배의 갯수
   fun getLocalOnGoingParcelCnt() : LiveData<Int>

   // 배송 중인 택배 리스트를 LiveData로 받기
   fun getLocalOngoingParcelsAsLiveData(): LiveData<List<ParcelDTO>>
}