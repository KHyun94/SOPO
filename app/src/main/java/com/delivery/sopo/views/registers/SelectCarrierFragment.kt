package com.delivery.sopo.views.registers

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentSelectCarrierBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.registesrs.SelectCarrierViewModel
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectCarrierFragment: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: FragmentSelectCarrierBinding
    private val vm: SelectCarrierViewModel by viewModel()

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                SopoLog.d(msg = "Register Step::2 BackPressListener")
                requireActivity().supportFragmentManager.popBackStack()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentView = activity as MainView
        receiveBundleData()
    }

    private fun receiveBundleData()
    {
        arguments?.let { bundle ->
            val waybillNum = bundle.getString(RegisterMainFrame.WAYBILL_NO)
            vm.waybillNum.value = waybillNum
            SopoLog.d("receiveBundleData >>> $waybillNum ${vm.waybillNum.value}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        bindView(inflater, container)
        setObserve()

        return binding.root
    }

    fun bindView(inflater: LayoutInflater, container: ViewGroup?)
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_carrier, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer {
            if(it != null && it == 0)
            {
                callback = object: OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d(msg = "Register Step::2 BackPressListener")
                        requireActivity().supportFragmentManager.popBackStack()
                    }

                }

                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        SopoLog.d("setObserve >>> ${vm.waybillNum.value}")

        vm.setCarrierAdapter(_waybillNum = vm.waybillNum.value ?: return)

        vm.moveFragment.observe(this, Observer {

            SopoLog.d("moveFragment >>> ${it}")

            val registerDTO =
                ParcelRegister(vm.waybillNum.value, vm.selectedItem.value?.item?.carrier, null)

            when(it)
            {
                TabCode.REGISTER_CONFIRM.NAME ->
                {
                    val mHandler = Handler()
                    mHandler.postDelayed(Runnable {

                        if(vm.waybillNum.value == null || vm.waybillNum.value == "")
                        {
                            TabCode.REGISTER_INPUT.FRAGMENT =
                                InputParcelFragment.newInstance(register = registerDTO,
                                                                returnType = 0)

                            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT,
                                                 RegisterMainFrame.viewId)
                        }
                        else
                        {
                            TabCode.REGISTER_CONFIRM.FRAGMENT =
                                ConfirmParcelFragment.newInstance(register = registerDTO)

                            FragmentManager.move(requireActivity(), TabCode.REGISTER_CONFIRM,
                                                 RegisterMainFrame.viewId)
                        }



                        binding.vm?.moveFragment?.value = ""

                    }, 300) // 0.5초후
                }

                TabCode.REGISTER_SELECT.NAME ->
                {
                    FragmentManager.remove(activity = requireActivity())
                    binding.vm?.moveFragment?.value = ""

                    TabCode.REGISTER_INPUT.FRAGMENT =
                        InputParcelFragment.newInstance(register = registerDTO, returnType = 0)

                    FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT,
                                         RegisterMainFrame.viewId)
                }
            }
        })

        binding.vm!!.adapter.observe(this, Observer {
            it?.setOnItemClickListener(object: GridTypedRecyclerViewAdapter.OnItemClickListener<List<SelectItem<CarrierDTO?>>>
                                       {
                                           override fun onItemClicked(v: View, pos: Int, items: List<SelectItem<CarrierDTO?>>)
                                           {
                                               val item = items[pos]

                                               if(item.isSelect)
                                               {
                                                   vm.selectedItem.value = item
                                                   vm.moveFragment.value = TabCode.REGISTER_CONFIRM.NAME
                                               }
                                           }

                                       })
        })
    }

    companion object
    {
        fun newInstance(waybillNum: String): SelectCarrierFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFrame.WAYBILL_NO, waybillNum)
            }

            return SelectCarrierFragment().apply {
                arguments = args
            }
        }
    }

    override fun onDetach()
    {
        super.onDetach()
        callback!!.remove()
    }
}