package com.delivery.sopo.enums

sealed class DialogType
{
    data class FocusLeftButton(val leftButton: String, val rightButton: String): DialogType()
    data class FocusRightButton(val leftButton: String, val rightButton: String): DialogType()
    data class FocusOneButton(val button: String): DialogType()
}