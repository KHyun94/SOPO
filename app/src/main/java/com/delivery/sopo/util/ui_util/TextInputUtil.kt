package com.delivery.sopo.util.ui_util

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
    var value: String = ""

    fun changeFocus(context: Context, focus: Triple<View, Boolean, InfoEnum>): Pair<InfoEnum, Boolean>
    {
        if (!focus.second)
        {
            return focusOut(context, focus)
        }

        return focusIn(context, focus)
    }

    private fun setTextInputLayoutTextWatcher(textInputEditText: TextInputEditText, infoEnum: InfoEnum): TextWatcher
    {
        val textInputLayout = textInputEditText.parent.parent as TextInputLayout

        return object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?)
            {
                textInputLayout.helperText = when (infoEnum)
                {
                    InfoEnum.EMAIL ->
                    {
                        if(ValidateUtil.isValidateEmail(p0.toString())) ""
                        else "이메일 양식을 확인해주세요."
                    }
                    InfoEnum.PASSWORD ->
                    {
                        if(ValidateUtil.isValidatePassword(p0.toString())) ""
                        else "영문, 숫자 조합 8자리 이상 설정해주세요."
                    }
                    InfoEnum.RE_PASSWORD ->
                    {
                        if(ValidateUtil.isValidatePassword(p0.toString())) ""
                        else "영문, 숫자 조합 8자리 이상 설정해주세요."
                    }
                    InfoEnum.NICKNAME ->
                    {
                        if(ValidateUtil.isValidateNickname(p0.toString())) ""
                        else "닉네임 형식을 확인해주세요."
                    }
                    else -> ""
                }
            }

        }
    }

    private fun focusIn(context: Context, focus: Triple<View, Boolean, InfoEnum>): Pair<InfoEnum, Boolean>
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
            boxBackgroundColor = ContextCompat.getColor(context, R.color.COLOR_GRAY_50)
            // endIcon >>> Visible, clear img

            error = null
            errorIconDrawable = null

            val inputText: String = textInputEditText.text.toString()

            helperText = when (infoEnum)
            {
                InfoEnum.EMAIL ->
                {
                    if(ValidateUtil.isValidateEmail(inputText)) ""
                    else "이메일 양식을 확인해주세요."
                }
                InfoEnum.PASSWORD ->
                {
                    if(ValidateUtil.isValidatePassword(inputText)) ""
                    else "영문, 숫자 조합 8자리 이상 설정해주세요."
                }
                InfoEnum.RE_PASSWORD ->
                {
                    if(ValidateUtil.isValidatePassword(inputText)) ""
                    else "영문, 숫자 조합 8자리 이상 설정해주세요."
                }
                InfoEnum.NICKNAME ->
                {
                    if(ValidateUtil.isValidateNickname(inputText)) ""
                    else "닉네임 형식을 확인해주세요."
                }
                else -> ""
            }

            textInputEditText.addTextChangedListener(setTextInputLayoutTextWatcher(textInputEditText, infoEnum))

//            setHelperTextColor(ContextCompat.getColorStateList(context, R.color.COLOR_MAIN_700))

            isEndIconVisible = true
            endIconMode = TextInputLayout.END_ICON_CUSTOM
            endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_clear)

            setEndIconOnClickListener {
                editText?.setText("")
            }
        }

        return Pair(infoEnum, false)
    }

    private fun focusOut(context: Context, focus: Triple<View, Boolean, InfoEnum>): Pair<InfoEnum, Boolean>
    {
        SopoLog.d("${focus.third.NAME}::focus out")

        val textInputEditText = (focus.first as TextInputEditText)
        val textInputLayout = textInputEditText.parent.parent as TextInputLayout

        val hasFocus = focus.second
        val infoEnum = focus.third

        textInputLayout.setEndIconOnClickListener(null)

        textInputEditText.removeTextChangedListener(setTextInputLayoutTextWatcher(textInputEditText, infoEnum))

        textInputLayout.helperText = null

        var isValidate: Boolean

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

                value = textInputEditText.text.toString()

                if(!isValidate)
                {
                    errorMessage = "영문, 숫자 조합 8자리 이상 설정해주세요."
                }
            }
            InfoEnum.RE_PASSWORD ->
            {
                isValidate = ValidateUtil.isValidatePassword(textInputEditText.text.toString())

                if(!isValidate)
                {
                    errorMessage = "영문, 숫자 조합 8자리 이상 설정해주세요."
                }

                if(textInputEditText.text.toString() != value)
                {
                    isValidate = false
                    errorMessage = "비밀번호가 일치하지 않습니다."
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
            InfoEnum.WAYBILL_NUMBER ->
            {
                isValidate = textInputEditText.text.toString().isNotEmpty()

                if(!isValidate)
                {
                    errorMessage = "운송장 번호를 확인해주세요."
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
                if(infoEnum == InfoEnum.WAYBILL_NUMBER)
                {
                    hint = "${focus.third.NAME}를 여기에 입력하세요."
                }
                if(infoEnum != InfoEnum.WAYBILL_NUMBER) hint = infoEnum.NAME
                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_100)

                boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)

                error = null

                endIconDrawable = null
                errorIconDrawable = ContextCompat.getDrawable(context, R.color.COLOR_GRAY_100)
            }
            return Pair(infoEnum, false)
        }

        if (!isValidate)
        {
            SopoLog.d("${infoEnum.NAME}'s validation is failed >>>${textInputEditText.text.toString()}")

            textInputLayout.run {
                isHintEnabled = true
                hint = infoEnum.NAME

                if(infoEnum == InfoEnum.WAYBILL_NUMBER)
                {
                    hint = "${focus.third.NAME}를 여기에 입력하세요."
                }

                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_50)

                boxStrokeWidth = SizeUtil.changeDpToPx(context, 2.0f)
                boxStrokeColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_700)

                isEndIconVisible = false
                endIconDrawable = null

                error = errorMessage
                errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_fail)
            }
            return Pair(infoEnum, false)
        }

        SopoLog.d("${focus.third.NAME}'s validation is success >>>${textInputEditText.text.toString()}")

        textInputLayout.run {
            isHintEnabled = false
            hint = null
            boxBackgroundColor = ContextCompat.getColor(context, R.color.COLOR_MAIN_BLUE_50)

            boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)
            boxStrokeErrorColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.COLOR_MAIN_700))

            if(infoEnum == InfoEnum.WAYBILL_NUMBER)
            {
                boxBackgroundColor = resources.getColor(R.color.COLOR_GRAY_100)

                boxStrokeWidth = SizeUtil.changeDpToPx(context, 0.0f)

                isEndIconVisible = true
                endIconDrawable = null

                error = ""
                isErrorEnabled = true
                errorIconDrawable = null
                return Pair(infoEnum, true)
            }

            isEndIconVisible = true
            endIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_success)

            error = ""
            isErrorEnabled = true
            errorIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_textinput_status_success)

        }

        return Pair(infoEnum, true)
    }


    fun setMessage(errorMessage: String)
    {
        this.errorMessage = errorMessage
    }
}