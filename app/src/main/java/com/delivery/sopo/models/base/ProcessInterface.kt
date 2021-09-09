package com.delivery.sopo.models.base

import android.content.Intent

interface ProcessInterface
{
    /**
     * 데이터 전달
     */
    fun receivedData(intent: Intent)

    /**
     * 초기 화면 세팅
     */
    fun initUI()

    /**
     * UI 세팅 이후
     */
    fun setAfterSetUI()

    /**
     * Observe 로직
     */
    fun setObserve()
}