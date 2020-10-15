package com.delivery.sopo.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.work.*
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.WorkEntity
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

object SOPOWorkeManager
{
    val TAG = "LOG.SOPO"

    private fun getWorkConstraint(): Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    private inline fun <reified T : ListenableWorker> getWorkRequest(
        intervalMin: Long,
        contrains: Constraints
    ): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<T>(intervalMin, TimeUnit.MINUTES).setConstraints(contrains)
            .build()

    fun updateWorkManager(lifecycleOwner: LifecycleOwner?,context: Context, appDatabase: AppDatabase)
    {
        val workManager = WorkManager.getInstance(context)

        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.Default) {

                val works = appDatabase.workDao().getAll()

                var workUUID: UUID? = null
                var workInfo: LiveData<WorkInfo>? = null
                var workRequest: PeriodicWorkRequest? = null

                if (works == null || works.isEmpty())
                {
                    // work 인스턴스화
                    workRequest = getWorkRequest<SOPOWorker>(15, getWorkConstraint())

                    // work UUID
                    workUUID = workRequest.id
                    //work manager 등록
                    workManager.enqueue(workRequest)

                    // 등록한 workRequest의 UUID를 Room에 저장
                    appDatabase.workDao()
                        .insert(
                            WorkEntity(
                                workUUID = workUUID.toString(),
                                workRegDt = TimeUtil.getDateTime()
                            )
                        )

                    // 워크 상태 조회
                    workInfo = workManager.getWorkInfoByIdLiveData(workUUID)
                }
                else
                {
                    val workEntity = works[works.size - 1]
                    workInfo = workManager.getWorkInfoByIdLiveData(UUID.fromString(workEntity.workUUID))
                }


//                if(lifecycleOwner != null)
//                {
//                    workInfo.observe(lifecycleOwner, androidx.lifecycle.Observer{workInfo ->
//                        Toast.makeText(context, "WorkManager State =>\n${workInfo.state}", Toast.LENGTH_LONG).show()
//                        when(workInfo.state)
//                        {
//                            WorkInfo.State.SUCCEEDED ->
//                            {
//
//                            }
//                            WorkInfo.State.RUNNING ->
//                            {
//
//                            }
//                            else ->
//                            {
//
//                            }
//                        }
//
//                    })
//                }
            }
        }

    }

    fun cancelWork(context : Context)
    {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }
}
