package com.delivery.sopo.data.repository.remote.parcel

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil

object ParcelUseCase
{
    suspend fun updateParcelAlias(req: UpdateAliasRequest): ResponseResult<Unit?>{
        when(val res = ParcelCall.updateParcelAlias(req = req))
        {
            is NetworkResult.Success ->
            {
                val apiResult = res.data
                return ResponseResult(true, CodeUtil.getCode(apiResult.code), apiResult.data, "", DisplayEnum.NON_DISPLAY)
            }
            is NetworkResult.Error ->
            {
                val exception = res.exception as APIException
                val code = exception.responseCode
                return ResponseResult(false, code, null, "택배 별칭을 ${req.alias}로 변경아 실패했습니다.", DisplayEnum.DIALOG)
            }
        }
    }
}