package com.delivery.sopo.models.inquiry

data class PagingManagement(
    var pagingNum: Int,
    var InquiryDate: String,
    var hasNext: Boolean
)