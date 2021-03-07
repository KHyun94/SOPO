package com.delivery.sopo.repository.interfaces

import androidx.lifecycle.LiveData
import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelRepository {
   suspend fun getRemoteOngoingParcels(): MutableList<Parcel>?
   suspend fun getRemoteOngoingParcel(regDt: String, parcelUid: String): Parcel?
   suspend fun getRemoteMonths(): MutableList<TimeCountDTO>?

   suspend fun getRemoteCompleteParcels(page: Int, inquiryDate: String): MutableList<Parcel>?

   suspend fun getLocalParcelById(parcelId: ParcelId): ParcelEntity?

   fun getLocalCompleteParcelsLiveData(): LiveData<List<Parcel>>
   fun getLocalCompleteParcels(): List<Parcel>
   suspend fun getLocalOngoingParcels(): List<Parcel>?

   fun getSoonDataCntLiveData(): LiveData<Int>
   fun getOngoingDataCntLiveData(): LiveData<Int>

   suspend fun insetEntity(parcel: ParcelEntity)
   suspend fun insertEntities(parcelList: List<Parcel>)

   suspend fun updateEntity(parcel: ParcelEntity): Int
    suspend fun updateEntities(parcelList: List<Parcel>)

   suspend fun deleteLocalParcels(parcelIdList: List<ParcelId>)
   suspend fun deleteRemoteParcels(): APIResult<String?>?

   // 0922 kh 추가사항
   suspend fun getSingleParcelWithwayBilNum(wayBilNum:String) : ParcelEntity?
   suspend fun getOnGoingDataCnt() : Int?

   suspend fun isBeingUpdateParcel(regDt: String, parcelUid: String): LiveData<Int?>
   suspend fun getIsUnidentifiedAsLiveData(parcelId: ParcelId): LiveData<Int?>

   // 배송 상태인 택배의 갯수
   fun getLocalOnGoingParcelCnt() : LiveData<Int>

   // 배송 중인 택배 리스트를 LiveData로 받기
   fun getLocalOngoingParcelsAsLiveData(): LiveData<List<Parcel>>
}