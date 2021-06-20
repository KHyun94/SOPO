package com.delivery.sopo.enums

enum class MenuEnum(val title: String)
{
  NOTICE(title = "공지사항"),
  SETTING(title = "설정"),
  FAQ(title = "FAQ"),
  USE_TERMS(title = "이용약관"),
  APP_INFO(title = "앱 정보"),
  NOT_DISTURB(title = "방해금지 시간대 설정"),
  ACCOUNT_MANAGER(title = "계정 관리"),
  UPDATE_NICKNAME(title = "닉네임 변경"),
  SIGN_OUT(title = "계정 탈퇴")
}