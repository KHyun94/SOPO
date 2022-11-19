package com.delivery.sopo.data.resources.parcel.remote

import com.delivery.sopo.DateSelector
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.models.parcel.Parcel
import kotlinx.coroutines.flow.Flow

interface ParcelRemoteDataSource
{
    suspend fun registerParcel(parcelRegister: Parcel.Register): Int
    suspend fun fetchParcelById(parcelId: Int): Parcel.Common
    suspend fun fetchParcelById(parcelIds: List<Int>): List<Parcel.Common>
    suspend fun fetchOngoingParcels(): List<Parcel.Common>
    suspend fun fetchCompletedParcels(page: Int, inquiryDate: String): List<Parcel.Common>

    suspend fun fetchDeliveredMonth(): List<DeliveredParcelHistory>
    suspend fun fetchDeliveredParcelsByPaging(page: Int, inquiryDate: String): List<Parcel.Common>
    suspend fun fetchCompletedDateInfo(cursorDate: String?=null): DateSelector

    suspend fun updateParcelAlias(parcelId: Int, alias: String)

    suspend fun fetchCarrierInfo(): List<Carrier.Info>

    suspend fun deleteParcels(parcelIds: List<Int>)

    suspend fun requestParcelsRefresh()
    suspend fun requestParcelUpdate(parcelId: Int):Parcel.Updatable
    suspend fun reportParcelReceive(parcelIds: List<Int>)
}