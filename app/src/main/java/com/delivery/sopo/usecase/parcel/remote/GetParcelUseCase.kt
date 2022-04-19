package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetParcelUseCase(private val parcelRepo: ParcelRepository, private val parcelStatusRepo: ParcelManagementRepoImpl)
{
    suspend operator fun invoke(parcelId: Int) = withContext(Dispatchers.IO) {
        val remoteParcel = parcelRepo.getRemoteParcelById(parcelId = parcelId)
        val localParcel = parcelRepo.getParcelById(remoteParcel.parcelId)

        parcelRepo.insertParcels(listOf(remoteParcel))
        parcelRepo.updateParcels(listOf(remoteParcel))
//        if(localParcel == null)
//        {
//            val status = ParcelMapper.parcelToParcelStatus(remoteParcel).apply {
//                unidentifiedStatus = if(!remoteParcel.reported)
//                {
//                    SopoLog.d("[ParcelId:${remoteParcel.parcelId}] New Status")
//                    StatusConst.ACTIVATE
//                }
//                else
//                {
//                    StatusConst.DEACTIVATE
//                }
//                auditDte = TimeUtil.getDateTime()
//            }
//
//            parcelRepo.insert(remoteParcel)
//            parcelStatusRepo.insertParcelStatus(status)
//
//            return@withContext remoteParcel
//        }
//
//        val status = parcelStatusRepo.getParcelStatus(parcelId = localParcel.parcelId).apply {
//            unidentifiedStatus = if(!remoteParcel.reported)
//            {
//                SopoLog.d("[ParcelId:${remoteParcel.parcelId}] New Status")
//                StatusConst.ACTIVATE
//            }
//            else
//            {
//                StatusConst.DEACTIVATE
//            }
//
//            auditDte = TimeUtil.getDateTime()
//        }
//
//        parcelRepo.update(parcel = remoteParcel)
//        parcelStatusRepo.update(status)

         if(!remoteParcel.reported)
         {
             CoroutineScope(Dispatchers.IO).launch { parcelRepo.reportParcelStatus(listOf(parcelId)) }
         }

        return@withContext remoteParcel
    }
}