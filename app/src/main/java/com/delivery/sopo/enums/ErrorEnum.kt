package com.delivery.sopo.enums

enum class ErrorEnum(val httpStatusCode: Int, val code: Int, val errorType: ErrorType, val content: String, var message: String)
{
    // 사실상 발생하지 않는 에러
    AUTHORIZE_FAIL(403, 101, ErrorType.AUTHORIZE, "(권한 오류) 해당 API에 대한 요청 권한이 없습니다.", "허가되지 않은 접근입니다."),
    AUTHENTICATION_FAIL(401, 102, ErrorType.AUTHORIZE, "해당 자원에 대한 유효한 인증 자격 증명이 없습니다.", "인증에 실패한 유저입니다."),
    VALIDATION(400, 103, ErrorType.VALIDATION, "요청의 인자값이나 파라미터의 유효성 오류 발생했습니다.", ""),
    UNKNOWN_ERROR(500, 199, ErrorType.UNKNOWN_ERROR, "정의되지 않은 에러", "현재 서비스를 이용할 수 없습니다. 다음에 다시 시도해주세요."),
    SYSTEM_ERROR(500, 999, ErrorType.SYSTEM, "서버 내부에서 처리 중 에러가 발생했습니다.", "현재 서비스를 이용할 수 없습니다. 다음에 다시 시도해주세요."),

   /* OAUTH2_INVALID_CLIENT(401, 801, ErrorType.OAUTH2, "Client 인증이 실패했습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_UNAUTHORIZED_CLIENT(400, 802, ErrorType.OAUTH2, "해당 client가 resource에 대한 접근권한이 존재하지 않습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_INVALID_GRANT(400, 803, ErrorType.OAUTH2, "인가코드, refrehs token이 틀리거나, 유효(만료)하지 않았습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_INVALID_SCOPE(403, 804, ErrorType.OAUTH2, "지정한 Scope 설정이 잘못되었습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_INVALID_TOKEN(401, 805, ErrorType.OAUTH2, "토큰의 유효기간이 지났거나 잘못된 토큰입니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_INVALID_REQUEST(400, 806, ErrorType.OAUTH2, "유효하지 않은 요청입니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_REDIRECT_URI_MISMATCH(400, 807, ErrorType.OAUTH2, "등록된 Redirect URL과 일치하지 않습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_UNSUPPORTED_GRANT_TYPE(400, 808, ErrorType.OAUTH2, "Grant Type 설정이 잘못되었습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_UNSUPPORTED_RESPONSE_TYPE(400, 809, ErrorType.OAUTH2, "지원하지 않는 응답 타입입니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_ACCOUNT_NOT_FOUND(403, 810, ErrorType.OAUTH2, "사용자의 계정을 찾을 수 없습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_UNKNOWN(401, 811, ErrorType.OAUTH2, "이 외 정의된 사유로 에러가 발생했습니다.", "인증에 실패했습니다. 다시 시도해주세요."),
    OAUTH2_DELETE_TOKEN(400, 812, ErrorType.OAUTH2, "존재하지 않는 토큰입니다.(중복 로그인)", "다른 기기에 로그인되어있습니다. 다시 확인해주세요."),
*/

    INVALID_JWT_TOKEN(401, 601, ErrorType.AUTHENTICATION, "비밀번호 초기화 API에서 JWT 토큰 검증에 실패했습니다.", ""),
    FAIL_TO_CREATE_JWT_TOKEN(500, 602, ErrorType.SYSTEM, "인증서버에서 인증토큰을 생성하는데 실패했습니다.", ""),
    FAIL_TO_CONNECT_INTERNAL_SERVICE(500, 603, ErrorType.SYSTEM, "서버간 서비스 연결에 실패했습니다.", "현재 서비스를 이용할 수 없습니다. 다음에 다시 시도해주세요."),
    ALREADY_REGISTERED_USER(409, 604, ErrorType.CONFLICT, "회원가입, 해당 id로 등록한 사람이 있습니다.", "이미 등록된 사용자가 있습니다."),
    INVALID_USER(401, 605, ErrorType.AUTHENTICATION, "유효하지 않은 유저입니다.", "정상적인 사용자가 아닙니다."),
    KAKAO_ERROR(401, 606, ErrorType.AUTHENTICATION, "서버 내 카카오 서버와 연동할 때 에러가 발생했습니다.", ""),
    USER_NOT_FOUND(404, 607, ErrorType.NO_RESOURCE, "해당하는 id에 부합하는 유저를 찾을 수 없습니다.", ""),
    FCM_TOKEN_NOT_FOUND(404,608, ErrorType.NO_RESOURCE,"fcm-token이 존재하지 않습니다.", ""),
    NICK_NAME_NOT_FOUND(404, 609, ErrorType.NO_RESOURCE, "Nickname이 등록되지 않았습니다.", "Nickname이 등록되지 않았습니다."),

    INVALID_AUTH_CODE(401, 615, ErrorType.AUTHENTICATION, "입력한 인증코드가 일치하지 않습니다.", "입력한 인증코드가 일치하지 않습니다."),
    DUPLICATE_LOGIN(401, 616, ErrorType.AUTHENTICATION, "다른 디바이스에서 로그인 중 입니다.", "다른 디바이스에서 로그인 중 입니다."),
    INVALID_TOKEN(401, 617, ErrorType.AUTHENTICATION, "만료된 토큰입니다.", "만료된 토큰입니다."),

    ALREADY_REGISTERED_PARCEL(409, 701, ErrorType.CONFLICT, "이미 등록된 택배입니다.", "이미 등록된 택배입니다."),
    OVER_REGISTERED_PARCEL(409, 702, ErrorType.CONFLICT, "등록할 수 있는 택배의 개수를 초과하였습니다.", "등록할 수 있는 택배의 개수를 초과하였습니다."),
    PARCEL_NOT_FOUND(404, 703, ErrorType.NO_RESOURCE, "해당하는 id에 부합하는 택배를 찾을 수 없습니다.", "해당하는 id에 부합하는 택배를 찾을 수 없습니다."),
    FAIL_TO_SEARCH_PARCEL(500, 704, ErrorType.DELIVERY, "택배 조회에 실패하였습니다.", "택배 조회에 실패하였습니다."),
    PARCEL_BAD_REQUEST(400, 705, ErrorType.VALIDATION, "송장 번호를 확인해주세요.", "송장 번호를 확인해주세요.");

    companion object{
        fun getErrorCode(code: Int): ErrorEnum
        {
            return try
            {
                enumValues<ErrorEnum>().first { it.code == code }
            }
            catch(e: Exception)
            {
                SYSTEM_ERROR
            }
        }
    }
}