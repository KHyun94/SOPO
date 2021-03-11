package com.delivery.sopo.models.inquiry

data class PagingManagement(
    var pagingNum: Int,
    var inquiryDate: String,
    var hasNext: Boolean
)