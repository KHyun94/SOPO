package com.delivery.sopo.data.resources.parcel.remote

import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.extensions.wrapBodyAliasToMap
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.services.network_handler.BaseService

class ParcelRemoteDataSourceImpl(private val parcelService: ParcelService): ParcelRemoteDataSource,
        BaseService()
{
    override suspend fun registerParcel(parcelRegister: Parcel.Register): Int
    {
        val result = apiCall { parcelService.registerParcel(parcelRegister = parcelRegister) }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun fetchParcelById(parcelId: Int): Parcel.Common
    {
        val result = apiCall { parcelService.fetchParcelById(parcelId = parcelId) }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun fetchParcelById(parcelIds: List<Int>): List<Parcel.Common>
    {
        val parcelIdsStr = parcelIds.joinToString(", ")

        val result = apiCall { parcelService.fetchParcelById(parcelId = parcelIdsStr) }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun fetchOngoingParcels(): List<Parcel.Common>
    {
        val result = apiCall { parcelService.fetchOngoingParcels() }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun fetchDeliveredMonth(): List<DeliveredParcelHistory>
    {
        val result = apiCall { parcelService.fetchDeliveredMonth() }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun fetchDeliveredParcelsByPaging(page: Int, inquiryDate: String): List<Parcel.Common>
    {
        val result =
            apiCall { parcelService.fetchDeliveredParcelsByPaging(page = page, inquiryDate = inquiryDate) }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun updateParcelAlias(parcelId: Int, alias: String)
    {
        val parameter = alias.wrapBodyAliasToMap("alias")
        apiCall { parcelService.updateParcelAlias(parcelId = parcelId, parcelAlias = parameter) }
    }

    override suspend fun deleteParcels(parcelIds: List<Int>)
    {
        val parameter = parcelIds.wrapBodyAliasToHashMap("alias")
        apiCall { parcelService.deleteParcels(parcelIds = parameter) }
    }

    override suspend fun requestParcelsRefresh()
    {
        apiCall { parcelService.requestParcelsRefresh() }
    }

    override suspend fun requestParcelUpdate(parcelId: Int): Parcel.Updatable
    {
        val parameter = parcelId.wrapBodyAliasToHashMap("parcelId")
        val result = apiCall { parcelService.requestParcelsUpdate(parcelId = parameter) }
        return result.data?.data ?: throw SOPOApiException.create(ErrorCode.PARCEL_NOT_FOUND)
    }

    override suspend fun reportParcelReceive(parcelIds: List<Int>)
    {
        val parameter = parcelIds.wrapBodyAliasToHashMap("parcelIds")
        apiCall { parcelService.reportParcelStatus(parcelIds = parameter) }
    }
}