package com.delivery.sopo.networks.call

import com.delivery.sopo.data.repository.local.o_auth.OAuthEntity
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.ParcelRegisterDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.lang.Exception

object ParcelCall : BaseService(), KoinComponent
{
    private val userLocalRepo : UserLocalRepository by inject()
    private val oAuthLocalRepo : OAuthLocalRepository by inject()
    val email : String
        get() = userLocalRepo.getUserId()

    var parcelAPI : ParcelAPI

    init
    {
        val oAuth : OAuthEntity? = runBlocking {  oAuthLocalRepo.get(userId = email) }
        parcelAPI = NetworkManager.retro(oAuth?.accessToken).create(ParcelAPI::class.java)
    }

    suspend fun registerParcel(registerDto: ParcelRegisterDTO):NetworkResult<APIResult<Int?>>
    {
        val result = parcelAPI.registerParcel(registerDto = registerDto)
        return apiCall(call = {result})
    }

    suspend fun getOngoingParcels(): NetworkResult<APIResult<List<ParcelDTO>?>>
    {
        val result = parcelAPI.getOngoingParcels()
        return apiCall(call = {result})
    }


    suspend fun requestParcelsForRefresh() : NetworkResult<APIResult<String?>>
    {
        val result = parcelAPI.requestParcelsForRefresh()
        return apiCall(call = { result })
    }

    suspend fun requestParcelForRefresh(parcelId : Int) : ResponseResult<Unit?>
    {
        val mapper = mapOf<String, Int>(Pair("parcelId", parcelId))
        val result = parcelAPI.requestParcelForRefresh(mapper)

        when(val res = apiCall(call = { result }))
        {
            is NetworkResult.Success ->
            {
                val apiResult = res.data
                return ResponseResult.success(CodeUtil.getCode(apiResult.code), Unit, "성공")
            }
            is NetworkResult.Error ->
            {
                val exception = res.exception as APIException

                val code = when(exception.httpStatusCode)
                {
                    204 -> ResponseCode.PARCEL_NOTHING_TO_UPDATES
                    303 -> ResponseCode.PARCEL_SOMETHING_TO_UPDATES
                    else -> exception.responseCode
                }

                SopoLog.e("wtf ${code} ${exception.httpStatusCode}")

                return ResponseResult.fail(code, Unit, code.MSG, DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun getSingleParcel(parcelId:Int): ResponseResult<ParcelDTO?>
    {
        val result = parcelAPI.getSingleParcel(parcelId)

        when(val res = apiCall(call = { result }))
        {
            is NetworkResult.Success ->
            {
                val apiResult = res.data
                val data: ParcelDTO = apiResult.data?:throw Exception("Parcel data가 조회되지 않습니다.")

                return ResponseResult.success(CodeUtil.getCode(apiResult.code), data, "성공")
            }
            is NetworkResult.Error ->
            {
                val exception = res.exception as APIException
                val code = exception.responseCode
                return ResponseResult.fail(code, null, "단일 택배 조회 실패", DisplayEnum.DIALOG)
            }
        }
    }

    suspend fun updateParcelAlias(req: UpdateAliasRequest): NetworkResult<APIResult<Unit?>>
    {
        val result = parcelAPI.updateParcelAlias(req = req)
        return apiCall(call = { result })
    }
}