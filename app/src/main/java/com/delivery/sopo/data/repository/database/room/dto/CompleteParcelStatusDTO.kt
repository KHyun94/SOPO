package com.delivery.sopo.data.repository.database.room.dto

import com.delivery.sopo.data.repository.database.room.entity.CompleteParcelStatusEntity
import com.delivery.sopo.util.TimeUtil

data class CompleteParcelStatusDTO(var year: String, var month: String, var count: Int, var visibility: Int = 0, var status: Int = 1, var auditDte: String = TimeUtil.getDateTime())

