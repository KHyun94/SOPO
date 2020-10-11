package com.delivery.sopo.views.registers

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterStep2Binding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.interfaces.listener.OnMainBackPressListener
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.views.adapter.GridRvAdapter
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterStep2 : Fragment()
{
    val TAG = "LOG.SOPO"

    private lateinit var parentView : MainView

    private lateinit var binding: RegisterStep2Binding
    private val registerStep2Vm: RegisterStep2ViewModel by viewModel()

    private var waybilNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if (arguments != null)
        {
            waybilNum = arguments!!.getString("waybilNum") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.register_step2, container, false)
        binding.vm = registerStep2Vm
        binding.lifecycleOwner = this

        binding.vm!!.initAdapter(_waybilNum = waybilNum ?: "")

        setObserve()

        parentView = activity as MainView

        parentView.setOnBackPressListener(object : OnMainBackPressListener
        {
            override fun onBackPressed()
            {
                Log.d("LOG.SOPO", "OnBackPressed task 2")
                FragmentManager.remove(activity!!)
            }
        })

        return binding.root
    }

    //(activity as RegisterMainFrame).childFragmentManager
    fun setObserve()
    {
        binding.vm?.moveFragment?.observe(this, Observer {

            when (it)
            {
                FragmentType.REGISTER_STEP3.NAME ->
                {
                    val mHandler = Handler()
                    mHandler.postDelayed(Runnable {

                        FragmentType.REGISTER_STEP3.FRAGMENT =
                            RegisterStep3.newInstance(waybilNum, binding.vm!!.selectedItem.value!!.item)

                        FragmentManager.move(
                            activity!!,
                            FragmentType.REGISTER_STEP3,
                            RegisterMainFrame.viewId
                        )
                        binding.vm?.moveFragment?.value = ""

                    }, 300) // 0.5초후
                }

                FragmentType.REGISTER_STEP2.NAME ->
                {
                    FragmentManager.remove(activity = activity!!)
                    binding.vm?.moveFragment?.value = ""
                }
            }
        })

        binding.vm!!.adapter.observe(this, Observer {
            it?.setOnItemClickListener(object :
                GridRvAdapter.OnItemClickListener<List<SelectItem<CourierItem>>>
            {
                override fun onItemClicked(v: View, pos: Int, items: List<SelectItem<CourierItem>>)
                {
                    val item = items[pos]

                    if(item.isSelect)
                    {
                        binding.vm!!.selectedItem.value = item
                        binding.vm!!.moveFragment.value = FragmentType.REGISTER_STEP3.NAME
                    }
                }

            })
        })
    }

    companion object
    {
        fun newInstance(waybilNum: String?, courier: CourierItem?): RegisterStep2
        {
            val registerStep2 = RegisterStep2()

            val args = Bundle()

            args.putString("waybilNum", waybilNum)
            args.putSerializable("courier", courier)

            registerStep2.arguments = args
            return registerStep2
        }
    }

}