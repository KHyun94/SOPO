package com.delivery.sopo.util.fun_util

import com.delivery.sopo.enums.ResponseCode

object CodeUtil
{
    fun returnCodeMsg(code: String): String
    {
        return when (code)
        {
            ResponseCode.SUCCESS.CODE -> ResponseCode.SUCCESS.MSG
            ResponseCode.VALIDATION_ERROR.CODE -> ResponseCode.VALIDATION_ERROR.MSG
            ResponseCode.KAKAO_SIMPLE_FAIL.CODE -> ResponseCode.KAKAO_SIMPLE_FAIL.MSG
            ResponseCode.KAKAO_SIMPLE_ALREADY_USE_EMAIL.CODE -> ResponseCode.KAKAO_SIMPLE_ALREADY_USE_EMAIL.MSG
            ResponseCode.KAKAO_SIMPLE_NOT_VERIFYED_USER.CODE -> ResponseCode.KAKAO_SIMPLE_NOT_VERIFYED_USER.MSG
            ResponseCode.KAKAO_SIMPLE_CORRUPT_EMAIL.CODE -> ResponseCode.KAKAO_SIMPLE_CORRUPT_EMAIL.MSG
            ResponseCode.KAKAO_SIMPLE_FAIL_TO_PASING_DATA.CODE -> ResponseCode.KAKAO_SIMPLE_FAIL_TO_PASING_DATA.MSG
            ResponseCode.KAKAO_SIMPLE_CORRUPT_EMAIL2.CODE -> ResponseCode.KAKAO_SIMPLE_CORRUPT_EMAIL2.MSG
            ResponseCode.KAKAO_SIMPLE_CANNOT_FIND_FIREBASE_USER.CODE -> ResponseCode.KAKAO_SIMPLE_CANNOT_FIND_FIREBASE_USER.MSG
            ResponseCode.FAIL_TO_FIND_API_ACCOUNT.CODE -> ResponseCode.FAIL_TO_FIND_API_ACCOUNT.MSG
            ResponseCode.FAIL_TO_FIND_SOPO_USER.CODE -> ResponseCode.FAIL_TO_FIND_SOPO_USER.MSG
            ResponseCode.FAIL_TO_SAVE_API_ACCOUNT.CODE -> ResponseCode.FAIL_TO_SAVE_API_ACCOUNT.MSG
            ResponseCode.FAIL_TO_SAVE_SOPO_USER.CODE -> ResponseCode.FAIL_TO_SAVE_SOPO_USER.MSG
            ResponseCode.NOT_VALID_USER.CODE -> ResponseCode.NOT_VALID_USER.MSG
            ResponseCode.FAIL_TO_REQUEST_FOR_UPDATING.CODE -> ResponseCode.FAIL_TO_REQUEST_FOR_UPDATING.MSG
            ResponseCode.SEARCH_PARCEL_NULL_ERROR.CODE -> ResponseCode.SEARCH_PARCEL_NULL_ERROR.MSG
            ResponseCode.FAIL_TO_SAVE_PARCEL.CODE -> ResponseCode.FAIL_TO_SAVE_PARCEL.MSG
            ResponseCode.FAIL_TO_SEARCH_PARCEL.CODE -> ResponseCode.FAIL_TO_SEARCH_PARCEL.MSG
            ResponseCode.CAN_NOT_FIND_PARCEL.CODE -> ResponseCode.CAN_NOT_FIND_PARCEL.MSG
            ResponseCode.FAIL_TO_FIND_PARCEL.CODE -> ResponseCode.FAIL_TO_FIND_PARCEL.MSG
            ResponseCode.FAIL_TO_UPDATE_USER_FIREBASE_TOKEN.CODE -> ResponseCode.FAIL_TO_UPDATE_USER_FIREBASE_TOKEN.MSG
            ResponseCode.UNKNOWN_ERROR.CODE -> ResponseCode.UNKNOWN_ERROR.MSG
            ResponseCode.UNAUTHORIZED_ACCESS_ERROR.CODE -> ResponseCode.UNAUTHORIZED_ACCESS_ERROR.MSG
            ResponseCode.NOT_FOUND_ERROR.CODE -> ResponseCode.NOT_FOUND_ERROR.MSG
            ResponseCode.FORBIDDEN_ACCESS_ERROR.CODE -> ResponseCode.FORBIDDEN_ACCESS_ERROR.MSG
            ResponseCode.JWT_INVALID_SIGNATURE.CODE -> ResponseCode.JWT_INVALID_SIGNATURE.MSG
            ResponseCode.JWT_INVALID.CODE -> ResponseCode.JWT_INVALID.MSG
            ResponseCode.JWT_EXPIRED_TOKEN.CODE -> ResponseCode.JWT_EXPIRED_TOKEN.MSG
            ResponseCode.JWT_UNSUPPORTED_TOKEN.CODE -> ResponseCode.JWT_UNSUPPORTED_TOKEN.MSG
            ResponseCode.JWT_ILLEGALARGUMENT.CODE -> ResponseCode.JWT_ILLEGALARGUMENT.MSG
            else -> "알수 없는 오류입니다."
        }
    }
}