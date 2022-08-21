package com.delivery.sopo.presentation.views.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.databinding.ConfirmDeleteDialogBinding
import com.delivery.sopo.enums.DialogType
import com.delivery.sopo.util.SizeUtil

class CommonDialog(private val dialogType: DialogType = DialogType.FocusLeftButton("네", "아니요"),
                   private val title: CharSequence,
                   private val content: String? = null,
                   private val onLeftClickListener: (DialogFragment)->Unit,
                   private val onRightClickListener: ((DialogFragment)-> Unit)?=null
):
        DialogFragment()
{
    lateinit var binding: ConfirmDeleteDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = ConfirmDeleteDialogBinding.inflate(LayoutInflater.from(context))
        binding.lifecycleOwner = activity

        setWindowSetting()

        return binding.root
    }

    fun setTitle(title: String):CommonDialog {
        binding.title = title
        return this
    }

    fun setContent(content: String):CommonDialog {
        binding.content = content
        return this
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogType = dialogType

        binding.title = title
        binding.content = content

        when(dialogType)
        {
            is DialogType.FocusLeftButton ->
            {
                binding.leftBtnText = dialogType.leftButton
                binding.rightBtnText = dialogType.rightButton
            }
            is DialogType.FocusRightButton ->
            {
                binding.leftBtnText = dialogType.leftButton
                binding.rightBtnText = dialogType.rightButton
            }
            is DialogType.FocusOneButton ->
            {
                binding.leftBtnText = dialogType.button
            }
        }

        binding.leftBtnClickListener = View.OnClickListener { onLeftClickListener(this) }
        onRightClickListener?.let {
            binding.rightBtnClickListener = View.OnClickListener { it(this) }
        }

    }

    private fun setWindowSetting()
    {
        isCancelable = true
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }

    override fun onResume()
    {
        super.onResume()

        activity?.let {

            val size = SizeUtil.getDeviceSize(it)

            val params: ViewGroup.LayoutParams? = dialog?.window?.attributes

            val deviceWidth = size.x
//            params?.width = (deviceWidth * 0.8).toInt() - SizeUtil.changePxToDp(it.applicationContext, 36.0f)
            params?.width = (deviceWidth.toFloat() - SizeUtil.changeDpToPx(it.applicationContext, 36.0f)).toInt()
            dialog?.window?.attributes = params as WindowManager.LayoutParams
        }
    }
}
