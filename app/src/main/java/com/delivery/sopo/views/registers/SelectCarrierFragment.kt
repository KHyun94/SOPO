package com.delivery.sopo.views.registers

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentSelectCarrierBinding
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.ParcelRegister
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.GridSpacingItemDecoration
import com.delivery.sopo.viewmodels.registesrs.SelectCarrierViewModel
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.views.main.MainView
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
    lateinit var waybillNum: String
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

                TabCode.REGISTER_INPUT.FRAGMENT = InputParcelFragment.newInstance(parcelRegister = null, returnType = 0)

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
        parentView.currentPage.observe(this) {
            if(it != 0) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->

            vm.setNavigator(null)

            val parcelRegister = ParcelRegister(waybillNum, carrier, null)

            SopoLog.d("Nav [data:${parcelRegister.toString()}] [nav:$nav]")

            when(nav)
            {
                NavigatorEnum.REGISTER_CONFIRM ->
                {
                    Handler(Looper.getMainLooper()).postDelayed(Runnable {

                        if(waybillNum == "")
                        {
                            TabCode.REGISTER_INPUT.FRAGMENT =
                                InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)

                            FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
                            return@Runnable
                        }

                        TabCode.REGISTER_CONFIRM.FRAGMENT =
                            ConfirmParcelFragment.newInstance(register = parcelRegister,beforeStep = 1)

                        FragmentManager.move(requireActivity(), TabCode.REGISTER_CONFIRM, RegisterMainFragment.viewId)

                    }, 500) // 0.5초후
                }

                NavigatorEnum.REGISTER_INPUT ->
                {

//                    requireActivity().supportFragmentManager.popBackStack()
                    FragmentManager.remove(activity = requireActivity())

                    TabCode.REGISTER_INPUT.FRAGMENT =
                        InputParcelFragment.newInstance(parcelRegister = parcelRegister, returnType = 0)

                    FragmentManager.move(requireActivity(), TabCode.REGISTER_INPUT, RegisterMainFragment.viewId)
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
        adapter.setItems(vm.getCarriers(waybillNum = waybillNum))

        val listener =
            object: GridTypedRecyclerViewAdapter.OnItemClickListener<List<SelectItem<Carrier?>>>
            {
                override fun onItemClicked(v: View, pos: Int, items: List<SelectItem<Carrier?>>)
                {
                    val item = items[pos]

                    if(item.isSelect)
                    {
                        item.item?.let { carrier ->
                            this@SelectCarrierFragment.carrier = carrier.carrier
                        }

                        vm.setNavigator(NavigatorEnum.REGISTER_CONFIRM)

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