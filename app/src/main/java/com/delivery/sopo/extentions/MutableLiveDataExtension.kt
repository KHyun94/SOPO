package com.delivery.sopo.extentions

import androidx.lifecycle.MutableLiveData

object MutableLiveDataExtension
{
    operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(item: MutableList<T>) {
        val value = this.value ?: mutableListOf()
        value.addAll(item)
        this.value = value
    }

    operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(item: T) {
        val value = this.value ?: mutableListOf()
        value.add(item)
        this.value = value
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun <T> MutableLiveData<List<T>>.add(item: T) {
        val updatedItems = this.value as ArrayList
        updatedItems.add(item)
        this.value = updatedItems
    }

}