package com.delivery.sopo.views.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.databinding.ConfirmDeleteDialogBinding
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.util.SizeUtil

typealias OnOptionalClickListener = (dialog: DialogFragment) -> Unit

class OptionalDialog(private val optionalType: OptionalTypeEnum = OptionalTypeEnum.TWO_WAY_LEFT,
                     private val title: CharSequence,
                     private val subtitle: CharSequence? = null, private val content: String? = null,
                     private val leftHandler: Pair<String, OnOptionalClickListener>,
                     private val rightHandler: Pair<String, OnOptionalClickListener>? = null):
        DialogFragment()
{
    lateinit var binding: ConfirmDeleteDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = ConfirmDeleteDialogBinding.inflate(LayoutInflater.from(context))
        binding.lifecycleOwner = activity
        binding.optionalType = optionalType
        setWindowSetting()

        binding.title = title
        binding.subtitle = subtitle
        binding.content = content

        binding.leftBtnText = leftHandler.first
        binding.leftBtnClickListener = View.OnClickListener { leftHandler.second.invoke(this) }

        binding.rightBtnText = rightHandler?.first
        binding.rightBtnClickListener = View.OnClickListener { rightHandler?.second?.invoke(this) }

        return binding.root
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
            params?.width = (deviceWidth * 0.8).toInt()
            dialog?.window?.attributes = params as WindowManager.LayoutParams
        }


    }
}
