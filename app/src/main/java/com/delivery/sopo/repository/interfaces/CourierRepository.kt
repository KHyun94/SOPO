package com.delivery.sopo.repository.interfaces

import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.models.CourierItem

interface CourierRepository
{
    suspend fun getWithLen(len:Int, cnt:Int) : MutableList<CourierItem?>?
    suspend fun getWithCode(code:String) : CourierItem?
    suspend fun getWithoutLen(len:Int, cnt:Int) : MutableList<CourierItem?>?
    suspend fun getWithLenAndCondition1(len:Int, cnt:Int, param1:String) : MutableList<CourierItem?>?
    suspend fun getWithoutLenAndCondition1(len:Int, cnt:Int, param1:String) : MutableList<CourierItem?>?
    suspend fun getWithLenAndCondition2(len:Int, cnt:Int, param1:String, param2: String) : MutableList<CourierItem?>?
    suspend fun getWithoutLenAndCondition2(len:Int, cnt:Int, param1:String, param2: String) : MutableList<CourierItem?>?
}