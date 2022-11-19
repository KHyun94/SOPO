package com.delivery.sopo.presentation.register.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.databinding.FragmentSelectCarrierBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.models.enums.RegisterNavigation
import com.delivery.sopo.presentation.register.viewmodel.SelectCarrierViewModel
import com.delivery.sopo.presentation.views.adapter.CarrierRecyclerViewAdapter
import com.delivery.sopo.presentation.views.adapter.OnItemClickListener
import com.delivery.sopo.presentation.views.adapter.SelectCarrier
import com.delivery.sopo.presentation.views.adapter.SelectType
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.GridSpacingItemDecoration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SelectCarrierFragment : BaseFragment<FragmentSelectCarrierBinding, SelectCarrierViewModel>() {
    override val layoutRes: Int = R.layout.fragment_select_carrier
    override val vm: SelectCarrierViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainSelectCarrier }

    private val parentActivity: MainActivity by lazy { activity as MainActivity }

    lateinit var adapter: CarrierRecyclerViewAdapter
    lateinit var waybillNum: String
    var carrier: Carrier.Info? = null

    private lateinit var registerNavigation: RegisterNavigation

    override fun receiveData(bundle: Bundle) {
        super.receiveData(bundle)

        registerNavigation = bundle.getSerializable(RegisterParcelFragment.RETURN_TYPE) as RegisterNavigation
    }

    override fun setBeforeBinding() {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object : OnSOPOBackPressEvent(isUseCommon = true) {
            override fun onBackPressed() {
                super.onBackPressed()

                val parcel = Parcel.Register(
                    waybillNum = vm.waybillNum.value ?: "",
                    carrier = vm.carrier.value,
                    alias = null
                )
                val fm = parentFragmentManager.beginTransaction()
                fm.replace(
                    RegisterParcelFragment.viewId,
                    InputParcelFragment.newInstance(registerNavigation = RegisterNavigation.Init),
                    "inputParcel"
                )
                fm.commit()
            }
        }
    }

    override fun setAfterBinding() {
        super.setAfterBinding()
//        hideKeyboard()

        when (registerNavigation) {
            is RegisterNavigation.Init -> {
            }
            is RegisterNavigation.Next -> {
                (registerNavigation as RegisterNavigation.Next).parcel.apply {
                    vm.waybillNum.postValue(waybillNum)
                    vm.carrier.postValue(carrier)
                }
            }
            is RegisterNavigation.Complete -> {
//                notifyRegisteredParcel((registerNavigation as RegisterNavigation.Complete).parcel)
            }
        }

        setRecyclerViewAdapter()
        setRecyclerViewItem()
    }

    override fun setObserve() {
        super.setObserve()

        activity ?: return
        parentActivity.getCurrentPage().observe(this) {
            if (it != TabCode.REGISTER_TAB) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->

            val parcelRegister = Parcel.Register(vm.waybillNum.value ?: "", carrier, null)

            SopoLog.d("Nav [data:${parcelRegister.toString()}] [nav:$nav]")

            when (nav) {
                NavigatorConst.REGISTER_INPUT_INFO -> {
//                    val parcelRegister = Parcel.Register(waybillNum = waybillNum, carrier = carrier, alias = null)
//                    TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = parcelRegister, registerNavigation = RegisterNavigation.Next)
//                    FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterParcelFragment.viewId)
                }
                NavigatorConst.REGISTER_CONFIRM_PARCEL -> {
                    /*Handler(Looper.getMainLooper()).postDelayed(Runnable {

                        if(waybillNum == "")
                        {
                            TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = parcelRegister, registerNavigation = RegisterNavigation.Next)
                            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterParcelFragment.viewId)
                            return@Runnable
                        }

                        TabCode.REGISTER_CONFIRM.FRAGMENT = ConfirmParcelFragment.newInstance(register = parcelRegister,beforeStep = NavigatorConst.REGISTER_SELECT_CARRIER)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_CONFIRM, RegisterParcelFragment.viewId)

                    }, 300) // 0.5초후*/
                }
            }
        }
    }

    private fun setRecyclerViewAdapter() {
        adapter = CarrierRecyclerViewAdapter()

        adapter.setOnItemClickListener(object : OnItemClickListener<Carrier.Info> {
            override fun onSelectedItemClicked(data: Carrier.Info) {
                val fm = parentFragmentManager.beginTransaction()
                val parcel = Parcel.Register(vm.waybillNum.value ?: return, data, "")
                fm.replace(
                    RegisterParcelFragment.viewId, ConfirmParcelFragment.newInstance(
                        registerNavigation = RegisterNavigation.Next(
                            nextStep = "CONFIRM_PARCEL",
                            parcel = parcel
                        )
                    ), "inputParcel"
                )
                fm.commit()
            }

            override fun onNotSelectableItemClicked() {
                Toast.makeText(context, "일시적으로 조회가 불가능합니다.", Toast.LENGTH_SHORT).show()
            }

        })

        val decoration = GridSpacingItemDecoration(3, 16, true)

        binding.recyclerCarriers.layoutManager = GridLayoutManager(this.context, 3)
        binding.recyclerCarriers.adapter = adapter
        binding.recyclerCarriers.addItemDecoration(decoration)
    }

    private fun setRecyclerViewItem() = lifecycleScope.launch(Dispatchers.Default) {

        vm.getCarriers().collect {
            SopoLog.d("GET Carrier ${it.toString()}")
            when (it) {
                is Result.Success -> {
                    withContext(Dispatchers.Main) {
                        adapter.setItems(it.data.map { carrierInfo ->
                            if (!carrierInfo.isAvailable) SelectCarrier(
                                SelectType.NotSelectable,
                                carrierInfo
                            )
                            else SelectCarrier(SelectType.Unselect, carrierInfo)
                        })
                    }
                }
                is Result.Loading -> {

                }
                is Result.Error -> {
                    it.exception.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "택배사 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                }
            }
        }


//        val listener = object: CarrierRecyclerViewAdapter.OnItemClickListener<SelectItem<CarrierEntity>>
//        {
//            override fun onItemClicked(v: View, item: SelectItem<Carrier?>)
//            {
//                if(item.isSelect)
//                {
//                    item.item?.let { carrier ->
//                        this@SelectCarrierFragment.carrier = carrier.carrier
//                    }
//
//                    vm.postNavigator(NavigatorConst.REGISTER_CONFIRM_PARCEL)
//                }
//            }
//
//        }
//
//        adapter.setOnItemClickListener(listener)
    }

    companion object {
        fun newInstance(registerNavigation: RegisterNavigation): SelectCarrierFragment {
            val args = Bundle().apply {
                putSerializable(RegisterParcelFragment.RETURN_TYPE, registerNavigation)
            }

            return SelectCarrierFragment().apply {
                arguments = args
            }
        }
    }
}