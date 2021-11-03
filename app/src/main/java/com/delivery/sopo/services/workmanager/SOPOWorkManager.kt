 package com.delivery.sopo.services.workmanager

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*
import java.util.concurrent.TimeUnit


object SOPOWorkManager: KoinComponent
{
    val TAG = "SOPOWorkManager"

    // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private var _workInfo = MutableLiveData<WorkInfo?>()
    val workInfo: LiveData<WorkInfo?>
        get() = _workInfo

    private val appDatabase: AppDatabase by inject()
    private fun getWorkConstraint(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresDeviceIdle(true)
        .build()

    private inline fun <reified T: ListenableWorker> getWorkRequest(intervalMin: Long, contrains: Constraints): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<T>(intervalMin, TimeUnit.MINUTES).setConstraints(contrains)
            .build()

    fun updateWorkManager(context: Context)
    {
        SopoLog.d(msg = "updateWorkManager() call")

        val workManager = WorkManager.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {

            var workUUID: UUID? = null
            var workRequest: Any? = null

            SopoLog.d(msg = "Register New Worker Start!!!")

            // work 인스턴스화
            workRequest = OneTimeWorkRequestBuilder<UpdateParcelWorker>().build()

            // work UUID
            workUUID = workRequest.id
            //work manager 등록
            workManager.enqueue(workRequest)
        }
    }

    fun refreshOAuthWorkManager(context: Context)
    {
        SopoLog.d(msg = "refreshOAuthWorkManager() 호출")

        val workManager = WorkManager.getInstance(context)

        CoroutineScope(Dispatchers.IO).launch {

            var workUUID: UUID? = null
            var workRequest: Any? = null

            // work 인스턴스화
            workRequest = OneTimeWorkRequestBuilder<RefreshOAuthWorker>().build()

            // work UUID
            workUUID = workRequest.id
            //work manager 등록
            workManager.enqueue(workRequest)
        }
    }

    suspend fun registerParcelWorkManager(context: Context, registerParcelRegister: ParcelRegister)
    {
        SopoLog.i(msg = "registerParcelWorkManager() 호출 [data:${registerParcelRegister.toString()}]")

        val workManager = WorkManager.getInstance(context)

        val jsonStr = Gson().toJson(registerParcelRegister)

        val inputData = Data.Builder().putString("RECEIVED_STR", jsonStr).build()

        // work 인스턴스화
        val workRequest = OneTimeWorkRequestBuilder<RegisterParcelWorker>().setInputData(inputData).build()

        // work UUID
        val workUUID = workRequest.id

        //work manager 등록
        workManager.enqueue(workRequest)
    }

    fun cancelWork(context: Context)
    {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWork()
    }

    fun requestPeriodTimeWorker(context: Context)
    {
        val workManager = WorkManager.getInstance(context)
        val workRequest = PeriodicWorkRequestBuilder<OneTimeWorker>(15, TimeUnit.MINUTES).build()

        SopoLog.d(msg = "Period Service Manager GO!!")

        workManager.enqueue(workRequest)
    }

    fun requestOneTimeWorker(context: Context)
    {
        val workManager = WorkManager.getInstance(context)
        val workRequest = OneTimeWorkRequestBuilder<UpdateParcelWorker>().build()

        SopoLog.d(msg = "One Service Manager GO!!")

        workManager.enqueue(workRequest)
    }

}
