package com.delivery.sopo.interfaces

interface BaseDataSource<T>
{
    fun get(): List<T>
    fun insert(vararg data: T)
    fun update(vararg data: T)
    fun delete(vararg data: T)
}