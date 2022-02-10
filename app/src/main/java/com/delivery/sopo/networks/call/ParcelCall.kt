package com.delivery.sopo.networks.call

import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.ResponseResult

import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject

object  ParcelCall : BaseService(), KoinComponent
{
    private val userLocalRepo : UserLocalRepository by inject()
    private val oAuthLocalRepo : OAuthLocalRepository by inject()
    val email : String
        get() = userLocalRepo.getUserId()

    var parcelAPI : ParcelAPI

    init
    {
        val oAuth = runBlocking { oAuthLocalRepo.get(userLocalRepo.getUserId()) }
        parcelAPI = NetworkManager.retro(oAuth?.accessToken).create(ParcelAPI::class.java)
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



}