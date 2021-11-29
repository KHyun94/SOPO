package com.delivery.sopo.usecase.parcel.remote

import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.models.mapper.CompletedParcelHistoryMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCompletedMonthUseCase(private val parcelRepo: ParcelRepository, private val historyRepo: CompletedParcelHistoryRepoImpl)
{
    suspend operator fun invoke(): List<CompletedParcelHistory> = withContext(Dispatchers.IO) {
            SopoLog.i("GetCompletedMonthUseCase(...)")

            val histories = parcelRepo.getRemoteMonths()

            withContext(Dispatchers.Default) {
                historyRepo.deleteAll()
                val entities = histories.map(CompletedParcelHistoryMapper::dtoToEntity)
                historyRepo.insertEntities(entities)
            }

            return@withContext histories
        }
}