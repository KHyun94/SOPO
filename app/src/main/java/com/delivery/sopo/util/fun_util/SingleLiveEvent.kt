package com.delivery.sopo.util.fun_util

import android.util.Log
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

open class SingleLiveEvent<T> : MutableLiveData<T>()
{
    private val mPending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>)
    {
        if (hasActiveObservers())
        {
            Log.w("LOG.SOPO", "Multiple observers registered but only one will be notified of changes.")
        }
        super.observe(owner, Observer {
            if(mPending.compareAndSet(true, false)){
                observer.onChanged(it)
            }
        })
    }
    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    // postValue : Background Thread에서 처리
    override fun postValue(value: T) {
        super.postValue(value)
        // 여기서는 어떤 역할도 하지 않는다.
    }

    /**
     * 데이터의 속성을 지정해주지 않아도 call만으로 setValue를 호출 가능
     */
    @MainThread
    fun call() {
        value = null
    }

    companion object {
        private val TAG = "SingleLiveEvent"
    }
}