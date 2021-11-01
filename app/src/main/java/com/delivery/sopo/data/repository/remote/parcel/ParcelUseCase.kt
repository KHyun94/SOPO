package com.delivery.sopo.data.repository.remote.parcel

import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.models.parcel.ParcelDTO
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

    /*suspend fun requestParcelRegister(registerDTO: ParcelRegisterDTO): ResponseResult<Int?>
    {
        when(val res = ParcelCall.registerParcel(registerDTO)){
            is NetworkResult.Success ->
            {
                val apiResult = res.data
                val data: Int = apiResult.data?:throw NullPointerException("Parcel's id가 없습니다.")

                return ResponseResult(true, CodeUtil.getCode(apiResult.code), data, "", DisplayEnum.NON_DISPLAY)
            }
            is NetworkResult.Error ->
            {
                val exception = res.exception as APIException
                val code = exception.responseCode
                return ResponseResult(false, code, null, "택배 등록이 실패했습니다.[$code]", DisplayEnum.DIALOG)
            }
        }
    }*/

    suspend fun requestRemoteParcels(): ResponseResult<List<ParcelDTO>?>
    {
        when(val res = ParcelCall.getOngoingParcels())
        {
            is NetworkResult.Success ->
            {
                val apiResult = res.data
                // TODO 업데이트할 택배가 없을 때 response code를 주는건 어떤지 의논
                val data =
                    apiResult.data ?: return ResponseResult(true, CodeUtil.getCode(apiResult.code),
                                                            emptyList(), "업데이트할 탹배가 없습니다.")

                return ResponseResult(true, CodeUtil.getCode(apiResult.code), data,
                                      "업데이트 택배(${data.size})")
            }
            is NetworkResult.Error ->
            {
                val exception = res.exception as APIException
                val code = exception.responseCode
                return ResponseResult(false, code, null, "택배 업데이트가 정상적으로 처리되지 않았습니다. 재시도해주세요,",
                                      DisplayEnum.DIALOG)
            }
        }
    }
}