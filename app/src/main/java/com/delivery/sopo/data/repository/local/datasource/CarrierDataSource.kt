package com.delivery.sopo.data.repository.local.datasource

import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.models.CarrierDTO

interface CarrierDataSource
{
    suspend fun getWithLen(len:Int, cnt:Int) : MutableList<CarrierDTO?>?
    suspend fun getCarrierWithCode(code:String) : CarrierDTO?
    suspend fun getWithoutLen(len:Int, cnt:Int) : MutableList<CarrierDTO?>?
    suspend fun getWithLenAndCondition1(len:Int, cnt:Int, param1:String) : MutableList<CarrierDTO?>?
    suspend fun getWithoutLenAndCondition1(len:Int, cnt:Int, param1:String) : MutableList<CarrierDTO?>?
    suspend fun getWithLenAndCondition2(len:Int, cnt:Int, param1:String, param2: String) : MutableList<CarrierDTO?>?
    suspend fun getWithoutLenAndCondition2(len:Int, cnt:Int, param1:String, param2: String) : MutableList<CarrierDTO?>?
    suspend fun getCarrierEntityWithCode(carrierCode: String) : CarrierEntity
}