package com.delivery.sopo.presentation.views.registers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentSelectCarrierBinding
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.models.enums.ReturnType
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.GridSpacingItemDecoration
import com.delivery.sopo.presentation.viewmodels.registesrs.SelectCarrierViewModel
import com.delivery.sopo.presentation.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.presentation.views.main.MainView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SelectCarrierFragment: BaseFragment<FragmentSelectCarrierBinding, SelectCarrierViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_select_carrier
    override val vm: SelectCarrierViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainSelectCarrier }

    private val parentView: MainView by lazy { activity as MainView }

    lateinit var adapter: GridTypedRecyclerViewAdapter
    var waybillNum: String? = null
    var carrier: CarrierEnum? = null

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)

        waybillNum = bundle.getString(RegisterMainFragment.WAYBILL_NO) ?: ""
        SopoLog.d("운송장 번호 $waybillNum")
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(isUseCommon = true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()

                val parcelRegister = Parcel.Register(waybillNum = waybillNum, carrier = carrier, alias = null)

                TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = parcelRegister, returnType = ReturnType.REVISE_PARCEL)
                FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()
        hideKeyboard()
        setRecyclerViewAdapter()
        setRecyclerViewItem()
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.getCurrentPage().observe(this) {
            if(it != TabCode.firstTab) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->

            val parcelRegister = Parcel.Register(waybillNum, carrier, null)

            SopoLog.d("Nav [data:${parcelRegister.toString()}] [nav:$nav]")

            when(nav)
            {
                NavigatorConst.REGISTER_INPUT_INFO ->
                {
                    val parcelRegister = Parcel.Register(waybillNum = waybillNum, carrier = carrier, alias = null)
                    TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = parcelRegister, returnType = ReturnType.REVISE_PARCEL)
                    FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                }
                NavigatorConst.REGISTER_CONFIRM_PARCEL ->
                {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {

                        if(waybillNum == "")
                        {
                            TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(register = parcelRegister, returnType = ReturnType.REVISE_PARCEL)
                            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                            return@Runnable
                        }

                        TabCode.REGISTER_CONFIRM.FRAGMENT = ConfirmParcelFragment.newInstance(register = parcelRegister,beforeStep = NavigatorConst.REGISTER_SELECT_CARRIER)
                        FragmentManager.move(requireActivity(), TabCode.REGISTER_CONFIRM, RegisterMainFragment.viewId)

                    }, 300) // 0.5초후
                }
            }
        }
    }

    private fun setRecyclerViewAdapter()
    {
        adapter = GridTypedRecyclerViewAdapter(emptyList())

        val decoration = GridSpacingItemDecoration(3, 32, true)

        binding.recyclerCarriers.layoutManager = GridLayoutManager(this.context, 3)
        binding.recyclerCarriers.adapter = adapter
        binding.recyclerCarriers.addItemDecoration(decoration)
    }

    private fun setRecyclerViewItem() = CoroutineScope(Dispatchers.Main).launch {
        adapter.setItems(vm.getCarriers(waybillNum = waybillNum?:""))

        val listener = object: GridTypedRecyclerViewAdapter.OnItemClickListener<SelectItem<Carrier?>>
        {
            override fun onItemClicked(v: View, item: SelectItem<Carrier?>)
            {
                if(item.isSelect)
                {
                    item.item?.let { carrier ->
                        this@SelectCarrierFragment.carrier = carrier.carrier
                    }

                    vm.postNavigator(NavigatorConst.REGISTER_CONFIRM_PARCEL)

                }
            }

        }

        adapter.setOnItemClickListener(listener)
    }

    companion object
    {
        fun newInstance(waybillNum: String): SelectCarrierFragment
        {
            val args = Bundle().apply {
                putSerializable(RegisterMainFragment.WAYBILL_NO, waybillNum)
            }

            return SelectCarrierFragment().apply {
                arguments = args
            }
        }
    }
}