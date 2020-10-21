package com.delivery.sopo.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.LogEntity
import com.delivery.sopo.database.room.entity.WorkEntity
import com.delivery.sopo.services.worker.OneTimeWorker
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit
import com.delivery.sopo.services.worker.SOPOWorker


object SOPOWorkeManager
{
    val TAG = "LOG.SOPO"

    // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private var _workInfo = MutableLiveData<WorkInfo?>()
    val workInfo: LiveData<WorkInfo?>
        get() = _workInfo


    private fun getWorkConstraint(): Constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

    private inline fun <reified T : ListenableWorker> getWorkRequest(intervalMin: Long, contrains: Constraints): PeriodicWorkRequest = PeriodicWorkRequestBuilder<T>(intervalMin, TimeUnit.MINUTES).setConstraints(contrains).build()

    fun updateWorkManager(context: Context, appDatabase: AppDatabase)
    {
        val workManager = WorkManager.getInstance(context)

        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.IO) {

                val works = appDatabase.workDao().getAll()

                var workUUID: UUID? = null
                var workRequest: PeriodicWorkRequest? = null

                if (works == null || works.isEmpty())
                {
                    Log.d(TAG, "워크매니저 새로 등록")
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
                    _workInfo = workManager.getWorkInfoByIdLiveData(workUUID) as MutableLiveData<WorkInfo?>
                }
                else
                {
                    val workEntity = works[works.size - 1]
                    Log.d(TAG, "워크매니저 이미 등록 ===> $workEntity")
                    _workInfo = workManager.getWorkInfoByIdLiveData(UUID.fromString(workEntity.workUUID)) as MutableLiveData<WorkInfo?>
                }

            }
        }

    }

    fun cancelWork(context : Context)
    {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }

    fun requestOneTimeWorker(context : Context)
    {
        val workManager = WorkManager.getInstance(context)
        val workRequest = PeriodicWorkRequestBuilder<OneTimeWorker>(15, TimeUnit.MINUTES).build()

        Log.d(TAG, "Period Service Manager GO!!")

        workManager.enqueue(workRequest)
    }


}
