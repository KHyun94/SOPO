package com.delivery.sopo.extensions

import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.collections.ArrayList

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

    fun <T> MutableLiveData<T>.initialize(value:T):MutableLiveData<T> {
        this.value = value
        return this
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun <T> MutableLiveData<List<T>>.add(item: T) {
        val updatedItems = this.value as ArrayList
        updatedItems.add(item)
        this.value = updatedItems
    }

    fun <T> MutableLiveData<Stack<T>>.pushItem(item: T) {
        val updatedItems = this.value as Stack
        updatedItems.push(item)
        this.value = updatedItems
    }

    fun <T> MutableLiveData<Stack<T>>.popItem() {
        val updatedItems = this.value as Stack
        updatedItems.pop()
        this.value = updatedItems
    }

}