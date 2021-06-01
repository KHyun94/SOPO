package com.delivery.sopo.abstracts

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import kotlin.system.exitProcess

abstract class BasicFragmentView<T : ViewDataBinding> : Fragment()
{
    lateinit var parentView: Activity
    lateinit var binding: T

    abstract var layoutId: Int

    var callback: OnBackPressedCallback? = null

    inline fun<reified P: FragmentActivity?> setParentView(): P
    {
        return activity as P?:throw Exception("Parent Activity is null")
    }

    init
    {

    }

    // true: back press key를 2초 내로 2번 눌렀을 때, fa
    fun setOnBackPressedCallback(mainView: View, handler: (Boolean) -> Unit){
        var pressedTime: Long = 0

        object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (System.currentTimeMillis() - pressedTime > 2000)
                {
                    pressedTime = System.currentTimeMillis()

                    Snackbar.make(mainView, "한번 더 누르시면 앱이 종료됩니다.", 2000).apply {
                        animationMode = Snackbar.ANIMATION_MODE_SLIDE
                    }.show()

                    return handler.invoke(false)
                }

                handler.invoke(true)

                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
        binding.lifecycleOwner = this
        setObserver()
        return binding.root
    }

    abstract fun setObserver()
}