package com.delivery.sopo.util.ui_util

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.AlertUpdateDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateValueDialog(private val callback: ((String)-> Unit)): DialogFragment()
{
    private lateinit var binding: AlertUpdateDialogBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = AlertUpdateDialogBinding.inflate(layoutInflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Dispatchers.Main).launch {

            binding.tvOkBtn.setOnClickListener{
                callback.invoke(binding.etInputText.text.toString())
                dialog?.dismiss()
            }

            binding.tvCancelBtn.setOnClickListener{
                dialog?.dismiss()
            }

            binding.tvOkBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))
            binding.etInputText.addTextChangedListener {
                if (it.toString().length > 1)
                {
                    binding.tvOkBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_MAIN_700))
                }
            }

            binding.tvOkBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))
            binding.etInputText.addTextChangedListener {
                if (it.toString().isNotEmpty())
                {
                    binding.tvOkBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_MAIN_700))
                    binding.vFocusStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.COLOR_MAIN_700))
                }
                else
                {
                    binding.tvOkBtn.setTextColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))
                    binding.vFocusStatus.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_200))
                }
            }
        }
    }

    override fun onResume()
    {
        super.onResume()

        setDialogSize()
    }

    private fun setDialogSize() = with(binding)
    {
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val deviceWidth = getScreenWidth(requireContext())
        params?.width = (deviceWidth * 0.8).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    fun getScreenWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    fun getScreenHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = wm.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            val displayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.heightPixels
        }
    }
}