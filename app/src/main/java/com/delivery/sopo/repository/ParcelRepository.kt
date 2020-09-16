package com.delivery.sopo.repository

import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

interface ParcelRepository {
   suspend fun getRemoteParcels(): MutableList<Parcel>?
   suspend fun getLocalParcels(): MutableList<Parcel>?
   suspend fun saveLocalParcels(parcelList: List<Parcel>)
   suspend fun deleteRemoteParcels(): APIResult<String?>
   suspend fun deleteLocalParcelsStep1(parcelIdList: List<ParcelId>)
   suspend fun deleteLocalParcelsStep2()

}