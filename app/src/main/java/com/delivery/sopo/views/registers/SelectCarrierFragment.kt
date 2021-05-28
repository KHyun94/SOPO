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
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectCarrierFragment: Fragment()
{
    private lateinit var parentView: MainView

    private lateinit var binding: FragmentSelectCarrierBinding
    private val vm: RegisterStep2ViewModel by viewModel()

    private var waybillNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        parentView = activity as MainView

        waybillNum = arguments?.getString("waybillNum") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        bindView(inflater, container)
        setObserve()

        return binding.root
    }

    fun bindView(inflater: LayoutInflater, container: ViewGroup?){
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_carrier, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    //(activity as RegisterMainFrame).childFragmentManager
    private fun setObserve()
    {
        binding.vm!!.setAdapter(_waybillNum = waybillNum ?: "")

        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 0)
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

        binding.vm?.moveFragment?.observe(this, Observer {

            SopoLog.d("moveFragment >>> ${it}")

            when (it)
            {
                TabCode.REGISTER_STEP3.NAME ->
                {
                    val mHandler = Handler()
                    mHandler.postDelayed(Runnable {

                        if(waybillNum == null || waybillNum == "")
                        {
                            TabCode.REGISTER_STEP1.FRAGMENT =
                                InputParcelFragment.newInstance(waybillNum, binding.vm!!.selectedItem.value!!.item, 0)

                            FragmentManager.move(requireActivity(), TabCode.REGISTER_STEP1, RegisterMainFragment.layoutId)
                        }
                        else
                        {
                            TabCode.REGISTER_STEP3.FRAGMENT =
                                ConfirmParcelFragment.newInstance(waybillNum, binding.vm!!.selectedItem.value!!.item)

                            FragmentManager.move(requireActivity(), TabCode.REGISTER_STEP3, RegisterMainFragment.layoutId)
                        }



                        binding.vm?.moveFragment?.value = ""

                    }, 300) // 0.5초후
                }

                TabCode.REGISTER_STEP2.NAME ->
                {
                    FragmentManager.remove(activity = activity!!)
                    binding.vm?.moveFragment?.value = ""

                    TabCode.REGISTER_STEP1.FRAGMENT =
                        InputParcelFragment.newInstance(waybillNum, binding.vm?.selectedItem?.value?.item, 0)

                    FragmentManager.move(requireActivity(), TabCode.REGISTER_STEP1, RegisterMainFragment.layoutId)
                }
            }
        })

        binding.vm!!.adapter.observe(this, Observer {
            it?.setOnItemClickListener(object: GridTypedRecyclerViewAdapter.OnItemClickListener<List<SelectItem<CarrierDTO?>>>
            {
                override fun onItemClicked(v: View, pos: Int, items: List<SelectItem<CarrierDTO?>>)
                {
                    val item = items[pos]

                    if (item.isSelect)
                    {
                        binding.vm!!.selectedItem.value = item
                        binding.vm!!.moveFragment.value = TabCode.REGISTER_STEP3.NAME
                    }
                }

            })
        })
    }

    companion object
    {
        fun newInstance(waybillNum: String?, carrierDTO: CarrierDTO?): SelectCarrierFragment
        {
            val registerStep2 = SelectCarrierFragment()

            val args = Bundle()

            args.putString("waybillNum", waybillNum)
            args.putSerializable("carrier", carrierDTO)

            registerStep2.arguments = args
            return registerStep2
        }
    }

    var callback: OnBackPressedCallback? = null

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

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
    }

    override fun onDetach()
    {
        super.onDetach()
        callback!!.remove()
    }
}