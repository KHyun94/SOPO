package com.delivery.sopo.presentation.views.inquiry

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.DateSelector
import com.delivery.sopo.R
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.databinding.FragmentOngoingTypeBinding
import com.delivery.sopo.databinding.ItemSelectorDateBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.interfaces.OnPageSelectListener
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.presentation.consts.IntentConst
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.viewmodels.inquiry.OngoingTypeViewModel
import com.delivery.sopo.presentation.views.adapter.DateStickyHeaderItemDecoration
import com.delivery.sopo.presentation.views.adapter.InquiryListAdapter
import com.delivery.sopo.presentation.views.adapter.OngoingTypeAdapter
import com.delivery.sopo.presentation.views.dialog.CommonDialog
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*

@AndroidEntryPoint
class OngoingTypeFragment : BaseFragment<FragmentOngoingTypeBinding, OngoingTypeViewModel>() {
    private lateinit var onPageSelectListener: OnPageSelectListener

    override val layoutRes: Int = R.layout.fragment_ongoing_type
    override val vm: OngoingTypeViewModel by viewModels()
    override val mainLayout: View by lazy { binding.linearMainOngoing }

    private lateinit var registeredParcelAdapter: OngoingTypeAdapter

    private var refreshDelay: Boolean = false

    private val motherActivity: MainActivity by lazy { activity as MainActivity }

    private var scrollStatus: ScrollStatusEnum = ScrollStatusEnum.TOP

    val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            SopoLog.d("Registered Action ${intent.action}")

            when (intent.action) {
                IntentConst.Action.REGISTERED_ONGOING_PARCEL -> {

                }
                else -> {
                    SopoLog.d("NO ACTION")
                    return
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter().apply {
            addAction(IntentConst.Action.REGISTERED_ONGOING_PARCEL)
        }

        motherActivity.registerReceiver(broadcastReceiver, filter)

        if (scrollStatus != ScrollStatusEnum.TOP) {
            onPageSelectListener.onChangeTab(TabCode.INQUIRY_ONGOING)
        } else {
            onPageSelectListener.onChangeTab(null)
        }

        motherActivity.onReselectedTapClickListener = object : () -> Unit {
            override fun invoke() {
                if (scrollStatus == ScrollStatusEnum.TOP) return
                binding.recyclerviewRegisteredParcel.scrollTo(0, 0)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        motherActivity.unregisterReceiver(broadcastReceiver)
    }

    private fun setOnMainBridgeListener(context: Context) {
        onPageSelectListener = requireActivity() as OnPageSelectListener
    }

    override fun setBeforeBinding() {
        super.setBeforeBinding()

        onSOPOBackPressedListener = object : OnSOPOBackPressEvent() {
            override fun onBackPressedInTime() {
                Snackbar.make(mainLayout, "진행 한번 더 누르시면 앱이 종료됩니다.", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime() {
                exit()
            }
        }

        setOnMainBridgeListener(context = requireContext())
    }

    override fun setAfterBinding() {
        super.setAfterBinding()

        setAdapters()
        setListener()
    }

    private fun setAdapters() {
        registeredParcelAdapter = OngoingTypeAdapter().apply {
            this.setOnParcelClickListener(setOnParcelClickListener())
        }

        binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
    }

    override fun setObserve() {
        super.setObserve()

        activity ?: return

        motherActivity.getCurrentPage().observe(this) {
            if (it != 1) return@observe
            vm.getOngoingParcels()
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.parcels.asLiveData(Dispatchers.Default).observe(this) {

            when (it) {
                is Result.Success<List<InquiryListItem>> -> {
                    binding.linearNoItem.makeGone()

                    val sortList = vm.sortByDeliveryStatus(it.data).toMutableList()

                    registeredParcelAdapter.separateDeliveryListByStatus(sortList.toMutableList())

                    /*viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
                    viewSettingForRegisteredList(registeredParcelAdapter.getListSize())*/
                }
                is Result.Error -> {
                    binding.linearNoItem.makeVisible()
                }
                is Result.Loading, Result.Uninitialized -> {
                    binding.linearNoItem.makeGone()
                }
                else -> {
                    binding.linearNoItem.makeVisible()
                }
            }
        }

        vm.navigator.observe(this) { navigator ->
            when (navigator) {
                NavigatorConst.MAIN_BRIDGE_REGISTER -> {
                    onPageSelectListener.onSetCurrentPage(0)
                }
            }
        }
    }

    private fun setOnParcelClickListener(): ParcelEventListener {
        return object : ParcelEventListener() {
            override fun onMaintainParcelClicked(view: View, pos: Int, parcelId: Int) {
                super.onMaintainParcelClicked(view, pos, parcelId)

                val optionalDialog =
                    CommonDialog(dialogType = DialogType.FocusRightButton("지울게요", "유지할게요"),
                        title = "이 아이템을 제거할까요?",
                        content = """
                    배송 상태가 2주간 확인되지 않고 있어요.
                    등록된 송장번호가 유효하지 않을지도 몰라요.
                                """.trimIndent(),
                        onLeftClickListener = { dialog: DialogFragment ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                vm.deleteParcel(parcelId)
                                delay(1000)
                                vm.getOngoingParcels()
                                dialog.dismiss()
                            }
                        },
                        onRightClickListener = { dialog: DialogFragment ->
                            lifecycleScope.launch(Dispatchers.Main) {
                                withContext(Dispatchers.IO) { vm.refreshParcel(parcelId) }

                                delay(1000)

                                vm.getOngoingParcels()
                            }

                            dialog.dismiss()
                        })

                optionalDialog.show(requireActivity().supportFragmentManager, "")
            }

            override fun onEnterParcelDetailClicked(
                view: View,
                type: InquiryStatusEnum,
                parcelId: Int
            ) {
                super.onEnterParcelDetailClicked(view, type, parcelId)

                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.add(
                    requireActivity(),
                    TabCode.INQUIRY_DETAIL,
                    InquiryMainFragment.viewId
                )

                onPageSelectListener.onChangeTab(null)
            }

            override fun onUpdateParcelAliasClicked(
                view: View,
                type: InquiryStatusEnum,
                parcelId: Int
            ) {
                super.onUpdateParcelAliasClicked(view, type, parcelId)

                showKeyboard(binding.includeBottomInputLayout.etInputText)
                binding.includeBottomInputLayout.root.makeVisible()
                binding.includeBottomInputLayout.onClickListener = View.OnClickListener {
                    val alias: String = binding.includeBottomInputLayout.etInputText.text.toString()
                    vm.updateParcelAlias(parcelId, alias)
                }
            }
        }
    }

    override fun onShowKeyboard() {
        super.onShowKeyboard()

    }

    override fun onHideKeyboard() {
        super.onHideKeyboard()
        vm.syncOngoingParcels()
        binding.includeBottomInputLayout.root.makeGone()
    }

    private fun setListener() { // 당겨서 새로고침 !
//        binding.swipeLayoutMainOngoing.setOnRefreshListener {
//
//            if (!refreshDelay) {
//                refreshDelay = true
//
//                vm.syncOngoingParcels()
//
//                //5초후에 실행
//                Timer().schedule(object : TimerTask() {
//                    override fun run() {
//                        lifecycleScope.launch(Dispatchers.Main) {
//                            refreshDelay = false
//                        }
//                    }
//                }, 5000)
//
//                binding.swipeLayoutMainOngoing.isRefreshing = false
//
//                return@setOnRefreshListener
//            }
//
//            Toast.makeText(requireContext(), "5초 후에 다시 새로고침을 시도해주세요.", Toast.LENGTH_LONG).show()
//
//            binding.swipeLayoutMainOngoing.isRefreshing = false
//        }

        /* binding.nestedSvMainOngoingInquiry.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

             if(scrollY > 0)
             {
                 scrollStatus = ScrollStatusEnum.MIDDLE
                 onPageSelectListener.onChangeTab(TabCode.INQUIRY_ONGOING)
             }
             else
             {
                 scrollStatus = ScrollStatusEnum.TOP
                 onPageSelectListener.onChangeTab(null)
             }
         }*/
    }

/*   // '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
   private fun viewSettingForSoonArrivalList(listSize: Int)
   {
       if(listSize > 0)
       {
           binding.constraintSoonArrival.makeVisible()
           return
       }

       binding.constraintSoonArrival.makeGone()
   }

   // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
   private fun viewSettingForRegisteredList(listSize: Int)
   {
       when(listSize)
       {
           0 ->
           {
               binding.constraintRegisteredArrival.makeGone()
           }
           else ->
           {
               binding.constraintRegisteredArrival.makeVisible()
           }
       }
   }*/
}