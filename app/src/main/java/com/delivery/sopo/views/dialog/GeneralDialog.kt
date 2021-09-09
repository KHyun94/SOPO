package com.delivery.sopo.views.dialog

import android.app.ActionBar
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.GeneralDialogBinding
import com.delivery.sopo.util.SizeUtil

typealias OnAgreeClickListener = (agree: GeneralDialog) -> Unit

class GeneralDialog : DialogFragment
{
    private var parentActivity: Activity

    private var title: String? = null
    private var msg: String? = null
    private var detailMsg: String? = null

    private var onRightClickListener: Pair<String, OnAgreeClickListener?>? = null
    private var onLeftClickListener: Pair<String, OnAgreeClickListener?>? = null

    private lateinit var binding: GeneralDialogBinding

    constructor(
        act: Activity,
        title: String,
        msg: String,
        detailMsg: String?,
        rHandler: Pair<String, OnAgreeClickListener?>
    ) : super()
    {
        this.parentActivity = act
        this.title = title
        this.msg = msg
        this.detailMsg = detailMsg
        this.onRightClickListener = rHandler
    }

    constructor(
        act: Activity,
        title: String,
        msg: String,
        detailMsg: String?,
        rHandler: Pair<String, OnAgreeClickListener?>,
        lHandler: Pair<String, OnAgreeClickListener?>
    ) : super()
    {
        this.parentActivity = act
        this.title = title
        this.msg = msg
        this.detailMsg = detailMsg
        this.onRightClickListener = rHandler
        this.onLeftClickListener = lHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        binding = GeneralDialogBinding.inflate(inflater, container, false)

        setSetting()
        setUI()
        setClickEvent()

        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        dialog?.window?.setLayout(
            (SizeUtil.getDeviceSize(parentActivity).x * 4 / 5),
            ActionBar.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setUI()
    {
        binding.tvTitle.text = title
        binding.tvSimpleMsg.text = msg

        if (detailMsg != null)
        {
            binding.tvDetailMsg.text = detailMsg
        }
        else
        {
            binding.tvExpandLayout.visibility = View.GONE
            binding.ivArrow.visibility = View.GONE
        }

        if (onRightClickListener != null)
            binding.btnRight.text = onRightClickListener?.first
        else
            binding.btnRight.text = "네"

        if (onLeftClickListener != null)
            binding.btnLeft.text = onLeftClickListener?.first
        else
        {
//            binding.btnLeft.text = "아니오"
            binding.btnLeft.visibility = View.GONE
        }

    }

    private fun setClickEvent()
    {

        // 자세히 보기
        binding.tvExpandLayout.setOnClickListener {

            if (binding.layoutDetail.visibility == View.VISIBLE)
            {
                binding.layoutDetail.visibility = View.GONE
                binding.ivArrow.setBackgroundResource(R.drawable.ic_down_arrow)
            }
            else
            {
                binding.layoutDetail.visibility = View.VISIBLE
                binding.ivArrow.setBackgroundResource(R.drawable.ic_up_arrow)
            }
        }

        binding.btnRight.setOnClickListener {
            if (onRightClickListener?.second == null)
            {
                dismiss()
            }
            else
            {
                onRightClickListener?.second?.invoke(this)
            }
        }

        binding.btnLeft.setOnClickListener {
            if (onLeftClickListener?.second == null)
            {
                dismiss()
            }
            else
            {
                onLeftClickListener?.second?.invoke(this)
            }

        }

    }

    private fun setSetting()
    {
        isCancelable = false
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }
    }
}
