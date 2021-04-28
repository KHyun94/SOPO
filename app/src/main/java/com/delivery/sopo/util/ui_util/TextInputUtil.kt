package com.delivery.sopo.util.ui_util

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import com.delivery.sopo.R
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

object TextInputUtil
{
    var errorMessage = ""

    fun changeFocus(context: Context, focus: Triple<View, Boolean, InfoEnum>)
    {
        if (!focus.second)
        {
            focusOut(context, focus)
            return
        }

        focusIn(context, focus)
    }

    private fun focusIn(context: Context, focus: Triple<View, Boolean, InfoEnum>)
    {
        SopoLog.d("${focus.third.NAME}::focus in")

        val textInputEditText = (focus.first as TextInputEditText)
        val textInputLayout = textInputEditText.parent.parent as TextInputLayout

        val hasFocus = focus.second
        val infoEnum = focus.third

        textInputLayout.run {
            // 힌트 홣성화
            isHintEnabled = true
            hint = focus.third.NAME
            // 내부 이너 박스 컬러 >>> GRAY_50
            boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)
            // endIcon >>> Visible, clear img

            error = null
            errorIconDrawable = null

            isEndIconVisible = true
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_clear)

            setEndIconOnClickListener {
                editText?.setText("")
            }
        }
    }

    private fun focusOut(context: Context, focus: Triple<View, Boolean, InfoEnum>)
    {
        SopoLog.d("${focus.third.NAME}::focus out")

        val textInputEditText = (focus.first as TextInputEditText)
        val textInputLayout = textInputEditText.parent.parent as TextInputLayout

        val hasFocus = focus.second
        val infoEnum = focus.third

        textInputLayout.setEndIconOnClickListener(null)

        var isValidate = false

        when (infoEnum)
        {
            InfoEnum.EMAIL ->
            {
                isValidate = ValidateUtil.isValidateEmail(textInputEditText.text.toString())

                if(!isValidate)
                {
                    errorMessage = "이메일 양식을 확인해주세요."
                }
            }
            InfoEnum.PASSWORD ->
            {
                isValidate = ValidateUtil.isValidatePassword(textInputEditText.text.toString())

                if(!isValidate)
                {
                    errorMessage = "비밀번호 형식을 확인해주세요."
                }
            }
            InfoEnum.NICKNAME ->
            {
                isValidate = ValidateUtil.isValidateNickname(textInputEditText.text.toString())

                if(!isValidate)
                {
                    errorMessage = "닉네임 형식을 확인해주세요."
                }
            }
            else ->
            {
                isValidate = false
            }
        }

        if(textInputEditText.text.toString().isEmpty())
        {
            textInputLayout.run {
                isHintEnabled = true
                hint = infoEnum.NAME
                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_100)

                boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)

                error = null

                endIconDrawable = null
                errorIconDrawable = ContextCompat.getDrawable(context, R.color.COLOR_GRAY_100)
            }
            return
        }

        if (!isValidate)
        {
            SopoLog.d("${infoEnum.NAME}'s validation is failed >>>${textInputEditText.text.toString()}")

            textInputLayout.run {
                isHintEnabled = true
                hint = infoEnum.NAME
                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)

                boxStrokeWidth = SizeUtil.changeDpToPx(context, 2.0f)
                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)

                isEndIconVisible = false
                endIconDrawable = null

                error = errorMessage
                errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_fail)
            }
            return
        }

        SopoLog.d("${focus.third.NAME}'s validation is success >>>${textInputEditText.text.toString()}")

        textInputLayout.run {
            isHintEnabled = false
            hint = null
            boxBackgroundColor = resources.getColor(R.color.COLOR_MAIN_BLUE_50)

            boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
            boxStrokeErrorColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))

            isEndIconVisible = true
            endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_black_arrow_right)

            error = ""
            isErrorEnabled = true
            errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_success)
        }
    }


    fun setMessage(errorMessage: String)
    {
        this.errorMessage = errorMessage
    }
}