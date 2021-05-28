package com.delivery.sopo.data.repository.local.datasource

import androidx.lifecycle.LiveData
import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelDataSource {
   suspend fun getRemoteOngoingParcels(): MutableList<ParcelDTO>?
   suspend fun getRemoteMonths(): MutableList<TimeCountDTO>?

   suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<ParcelDTO>?

   suspend fun getLocalParcelById(parcelId: ParcelId): ParcelEntity?

   fun getLocalCompleteParcelsLiveData(): LiveData<List<ParcelDTO>>
   fun getLocalCompleteParcels(): List<ParcelDTO>
   suspend fun getLocalOngoingParcels(): List<ParcelDTO>?

   fun getSoonDataCntLiveData(): LiveData<Int>
   fun getOngoingDataCntLiveData(): LiveData<Int>

   suspend fun insetEntity(parcel: ParcelEntity)
   suspend fun insertEntities(parcelDTOList: List<ParcelDTO>)

   suspend fun updateEntity(parcel: ParcelEntity): Int
    suspend fun updateEntities(parcelDTOList: List<ParcelDTO>)

   suspend fun deleteLocalParcels(parcelIdList: List<ParcelId>)
   suspend fun deleteRemoteParcels(): APIResult<String?>?

   // 0922 kh 추가사항
   suspend fun getSingleParcelWithWaybillNum(waybillNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?

   suspend fun isBeingUpdateParcel(regDt: String, parcelUid: String): LiveData<Int?>
   fun getIsUnidentifiedAsLiveData(parcelId: ParcelId): LiveData<Int?>

   // 배송 상태인 택배의 갯수
   fun getLocalOnGoingParcelCnt() : LiveData<Int>

   // 배송 중인 택배 리스트를 LiveData로 받기
   fun getLocalOngoingParcelsAsLiveData(): LiveData<List<ParcelDTO>>
}