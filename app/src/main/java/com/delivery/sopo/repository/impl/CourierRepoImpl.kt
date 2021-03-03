package com.delivery.sopo.repository.impl

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.entity.CourierEntity
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.repository.interfaces.CourierRepository

class CourierRepoImpl(
    private val appDB: AppDatabase
) : CourierRepository
{
    override suspend fun getWithLen(len: Int, cnt: Int): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithLen(
                len = len,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getWithCode(code: String): CourierItem?
    {
        return appDB.courierDao().getWithCode(code = code)
    }

    override suspend fun getWithoutLen(len: Int, cnt: Int): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithoutLen(
                len = len,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getWithLenAndCondition1(
        len: Int,
        cnt: Int,
        param1: String
    ): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithLenAndCondition1(
                len = len,
                param1 = param1,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getWithoutLenAndCondition1(
        len: Int,
        cnt: Int,
        param1: String
    ): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithoutLenAndCondition1(
                len = len,
                param1 = param1,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getWithLenAndCondition2(
        len: Int,
        cnt: Int,
        param1: String,
        param2: String
    ): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithLenAndCondition2(
                len = len,
                param1 = param1,
                param2 = param2,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getWithoutLenAndCondition2(
        len: Int,
        cnt: Int,
        param1: String,
        param2: String
    ): MutableList<CourierItem?>?
    {
        return appDB.courierDao()
            .getWithoutLenAndCondition2(
                len = len,
                param1 = param1,
                param2 = param2,
                cnt = cnt
            ) as MutableList<CourierItem?>
    }

    override suspend fun getCourierEntityWithCode(courierCode: String): CourierEntity
    {
        return appDB.courierDao()
            .getCourierEntityWithCode(courierCode = courierCode)
    }
}