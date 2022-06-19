package com.delivery.sopo.presentation.consts

object NavigatorConst {

    object Screen {
        const val MAIN = "SCREEN_MAIN"
        const val RESET_PASSWORD = "SCREEN_RESET_PASSWORD"
        const val UPDATE_NICKNAME = "SCREEN_UPDATE_NICKNAME"
        const val LOGIN_SELECT = "SCREEN_LOGIN_SELECT"
    }

    object Event{
        const val BACK = "EVENT_BACK_SCREEN"
        const val COMPLETE = "EVENT_COMPLETE"

        const val INPUT_EMAIL_FOR_SEND = "EVENT_INPUT_EMAIL_FOR_SEND"
        const val INPUT_EMAIL_FOR_RESEND = "EVENT_INPUT_EMAIL_FOR_RESEND"
        const val INPUT_AUTH_CODE = "EVENT_INPUT_AUTH_CODE"
        const val INPUT_PASSWORD_FOR_RESET = "EVENT_INPUT_PASSWORD_FOR_RESET"
        const val COMPLETED_RESET_PASSWORD = "EVENT_COMPLETED_RESET_PASSWORD"
    }

    object Error{
        const val INVALID_JWT_TOKEN = "INVALID_JWT_TOKEN"
    }

    const val CONFIRM_SIGN_OUT:String = "CONFIRM_SIGN_OUT"
    const val EXIT: String = "EXIT"
    const val DUPLICATE_LOGIN: String = "DUPLICATE_LOGIN"
    const val TO_SIGN_OUT = "TO_SIGN_OUT"
    const val TO_LOGIN = "TO_LOGIN"

    const val TO_SIGN_UP = "TO_SIGN_UP"
    const val TO_KAKAO_LOGIN = "TO_KAKAO_LOGIN"



    const val TO_PERMISSION = "TO_PERMISSION"
    const val TO_INTRO = "TO_INTRO"


    const val REGISTER_TAB = 0
    const val INQUIRY_TAB = 1
    const val MY_MENU_TAB = 2

    const val TO_NOT_DISTURB = "TO_NOT_DISTURB"
    const val TO_SET_NOTIFY_OPTION = "TO_SET_NOTIFY_OPTION"
    const val TO_UPDATE_APP_PASSWORD = "TO_UPDATE_APP_PASSWORD"

    const val TO_LOGOUT = "TO_LOGOUT"

    const val TO_DELETE = "TO_DELETE"

    const val MAIN_BRIDGE_REGISTER = "MAIN_BRIDGE_REGISTER"

    const val REGISTER_INPUT_INFO = "REGISTER_INPUT_INFO"
    const val REGISTER_SELECT_CARRIER = "REGISTER_SELECT_CARRIER"
    const val REGISTER_CONFIRM_PARCEL = "REGISTER_CONFIRM_PARCEL"

    const val REGISTER_REVISE = "REGISTER_REVISE"
    const val REGISTER_INITIALIZE = "REGISTER_INITIALIZE"
    const val REGISTER_SUCCESS: String = "REGISTER_SUCCESS"

}