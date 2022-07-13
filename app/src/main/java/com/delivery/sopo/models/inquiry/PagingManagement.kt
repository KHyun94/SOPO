package com.delivery.sopo.models.inquiry

data class PagingManagement(
    var pagingNum: Int,
    var inquiryDate: String,
    var hasNext: Boolean
){
    fun isCheckDate(date: String): Boolean
    {
        return inquiryDate == date
    }

    companion object{
        fun init():PagingManagement{
            return PagingManagement(0, "", true)
        }
    }
}