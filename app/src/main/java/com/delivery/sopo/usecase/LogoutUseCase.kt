package com.delivery.sopo.usecase

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogoutUseCase(private val userLocalRepo: UserLocalRepository, private val appDatabase: AppDatabase)
{
    operator fun invoke() = CoroutineScope(Dispatchers.Default).launch {
        appDatabase.clearAllTables()
        userLocalRepo.removeUserRepo()
    }
}