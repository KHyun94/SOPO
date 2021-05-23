package com.delivery.sopo.data.repository.local.repository

import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.room.entity.CarrierEntity
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.data.repository.local.datasource.CarrierDataSource
import com.delivery.sopo.models.mapper.CarrierMapper

class CarrierRepository(private val appDB: AppDatabase)
{
    suspend fun getAll(): List<CarrierDTO?>
    {
        return appDB.carrierDAO().getAll().flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getAllCnt(): Int
    {
        return appDB.carrierDAO().getAllCnt()
    }

    suspend fun getWithLen(len: Int, cnt: Int): List<CarrierDTO?>
    {
        val res = appDB.carrierDAO().getWithLen(
            len = len, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getCarrierWithCode(code: String): CarrierDTO?
    {
        return CarrierMapper.entityToObject(appDB.carrierDAO().getWithCode(code = code))
    }

    suspend fun getWithoutLen(len: Int, cnt: Int): List<CarrierDTO?>?
    {
        val res = appDB.carrierDAO().getWithoutLen(
            len = len, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getWithLenAndCondition1(len: Int, cnt: Int, param1: String): List<CarrierDTO?>
    {
        val res = appDB.carrierDAO().getWithLenAndCondition1(
            len = len, param1 = param1, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getWithoutLenAndCondition1(len: Int, cnt: Int, param1: String): List<CarrierDTO?>
    {
        val res = appDB.carrierDAO().getWithoutLenAndCondition1(
            len = len, param1 = param1, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getWithLenAndCondition2(len: Int, cnt: Int, param1: String, param2: String): List<CarrierDTO?>
    {
        val res = appDB.carrierDAO().getWithLenAndCondition2(
            len = len, param1 = param1, param2 = param2, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getWithoutLenAndCondition2(len: Int, cnt: Int, param1: String, param2: String): List<CarrierDTO?>
    {
        val res = appDB.carrierDAO().getWithoutLenAndCondition2(
            len = len, param1 = param1, param2 = param2, cnt = cnt
        )

        if (res.isEmpty())
        {
            return emptyList<CarrierDTO>()
        }

        return res.flatMap {
            listOf(CarrierMapper.entityToObject(it))
        }
    }

    suspend fun getCarrierEntityWithCode(carrierCode: String): CarrierEntity
    {
        return appDB.carrierDAO().getCarrierEntityWithCode(carrierCode = carrierCode)
    }

    fun getCarrierEntityWithPartName(name: String): List<CarrierEntity?>
    {
        return appDB.carrierDAO().getWithPartName(name)
    }

    fun insert(list: List<CarrierEntity>)
    {
        return appDB.carrierDAO().insert(list)
    }
}