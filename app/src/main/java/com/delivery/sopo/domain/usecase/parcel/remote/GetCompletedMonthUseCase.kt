package com.delivery.sopo.domain.usecase.parcel.remote

import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.models.mapper.CompletedParcelHistoryMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCompletedMonthUseCase(private val parcelRepo: ParcelRepository, private val historyRepo: CompletedParcelHistoryRepoImpl)
{
    suspend operator fun invoke(): List<CompletedParcelHistory> = withContext(Dispatchers.IO) {
            SopoLog.i("호출")

            val histories = parcelRepo.getRemoteMonths()

            withContext(Dispatchers.Default) {
                historyRepo.deleteAll()
                val entities = histories.map(CompletedParcelHistoryMapper::dtoToEntity)
                entities.map { it.status }
                historyRepo.insertEntities(entities)
            }

            return@withContext histories
        }
}