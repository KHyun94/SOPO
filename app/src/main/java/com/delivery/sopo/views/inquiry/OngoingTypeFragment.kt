package com.delivery.sopo.views.inquiry

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.databinding.FragmentOngoingTypeBinding
import com.delivery.sopo.databinding.PopupMenuViewBinding
import com.delivery.sopo.enums.*
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressListener
import com.delivery.sopo.interfaces.listener.ParcelEventListener
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomSnackBar
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.inquiry.OngoingTypeViewModel
import com.delivery.sopo.views.adapter.InquiryListAdapter
import com.delivery.sopo.views.adapter.PopupMenuListAdapter
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.delivery.sopo.views.dialog.OptionalClickListener
import com.delivery.sopo.views.dialog.OptionalDialog
import com.delivery.sopo.views.main.MainView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.system.exitProcess

class OngoingTypeFragment: BaseFragment<FragmentOngoingTypeBinding, OngoingTypeViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_ongoing_type
    override val vm: OngoingTypeViewModel by viewModel()
    override val mainLayout: View by lazy { binding.swipeLayoutMainOngoing }

    private lateinit var soonArrivalParcelAdapter: InquiryListAdapter
    private lateinit var registeredParcelAdapter: InquiryListAdapter

    private var refreshDelay: Boolean = false

    private val parentView: MainView by lazy { activity as MainView }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        onSOPOBackPressedListener = object: OnSOPOBackPressListener
        {
            override fun onBackPressedInTime()
            {

                Snackbar.make(parentView.binding.layoutMain, "한번 더 누르시면 앱이 종료됩니다. 온고잉", 2000)
                    .apply { animationMode = Snackbar.ANIMATION_MODE_SLIDE }
                    .show()
            }

            override fun onBackPressedOutTime()
            {
                ActivityCompat.finishAffinity(parentView)
                exitProcess(0)
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        setAdapters()
        setListener()
    }

    private fun getAdapter(inquiryItemTypeEnum: InquiryItemTypeEnum): InquiryListAdapter
    {
        return InquiryListAdapter(parcelType = inquiryItemTypeEnum).apply {
            this.setOnParcelClickListener(getParcelClicked())
        }
    }

    private fun setAdapterAnimation(recyclerView: RecyclerView){
        val animator = recyclerView.itemAnimator as SimpleItemAnimator
        animator.supportsChangeAnimations = false
    }

    private fun setAdapters()
    {
        getAdapter(InquiryItemTypeEnum.Soon).let { adapter ->
            soonArrivalParcelAdapter = adapter
            binding.recyclerviewSoonArrival.adapter = soonArrivalParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }

        getAdapter(InquiryItemTypeEnum.Registered).let { adapter ->
            registeredParcelAdapter = adapter
            binding.recyclerviewRegisteredParcel.adapter = registeredParcelAdapter
            val animator = binding.recyclerviewSoonArrival.itemAnimator as SimpleItemAnimator
            animator.supportsChangeAnimations = false
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        if(activity == null) return
        parentView.currentPage.observe(requireActivity()) {
            if(it != null && it == TabCode.secondTab)
            {
                parentView.onBackPressedDispatcher.addCallback(parentView, onBackPressedCallback)
            }
        }

        vm.ongoingList.observe(requireActivity()) { list ->

            if(list.size == 0) binding.linearNoItem.visibility = View.VISIBLE
            else binding.linearNoItem.visibility = View.GONE

            soonArrivalParcelAdapter.separateDeliveryListByStatus(list)
            registeredParcelAdapter.separateDeliveryListByStatus(list)

            viewSettingForSoonArrivalList(soonArrivalParcelAdapter.getListSize())
            viewSettingForRegisteredList(registeredParcelAdapter.getListSize())
        }
    }

    override fun onResume()
    {
        super.onResume()
        SopoLog.d("!!!!!!!!!!!!!!!!!")
        parentView.onBackPressedDispatcher.addCallback(parentView, onBackPressedCallback)
    }

    private fun getParcelClicked(): ParcelEventListener
    {
        return object: ParcelEventListener()
        {
            override fun onMaintainParcelClicked(view: View, pos: Int, parcelId: Int)
            {
                super.onMaintainParcelClicked(view, pos, parcelId)

                OptionalDialog(optionalType = OptionalTypeEnum.LEFT, titleIcon = 0, title = "이 아이템을 제거할까요?", subTitle = "고객의 정보가 삭제되며 복구가 불가능합니다.", content = """
                    배송 상태가 2주간 확인되지 않고 있어요.
                    등록된 송장번호가 유효하지 않을지도 몰라요.
                                """.trimIndent(), leftHandler = Pair("지울게요", second = object: OptionalClickListener
                {
                    override fun invoke(dialog: OptionalDialog)
                    {

                        vm.onDeleteParcel(parcelId = parcelId)
                        dialog.dismiss()
                    }
                }), rightHandler = Pair(first = "유지할게요", second = object: OptionalClickListener
                {
                    override fun invoke(dialog: OptionalDialog)
                    {
                        CoroutineScope(Dispatchers.Main).launch {

                            vm.onRefreshParcel(parcelId)

//                            withContext(Dispatchers.Default) {
//                                registeredParcelAdapter.getList()[pos].apply {
//                                    this.parcelResponse = parcelRepo.getLocalParcelById(parcelId)
//                                        ?: return@withContext
//                                }
//                            }

                            registeredParcelAdapter.notifyItemChanged(pos)
                        }



                        dialog.dismiss()
                    }
                })).show(requireActivity().supportFragmentManager, "")
            }

            override fun onEnterParcelDetailClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onEnterParcelDetailClicked(view, type, parcelId)

                TabCode.INQUIRY_DETAIL.FRAGMENT = ParcelDetailView.newInstance(parcelId)
                FragmentManager.add(requireActivity(), TabCode.INQUIRY_DETAIL, InquiryMainFragment.viewId)
            }

            override fun onUpdateParcelAliasClicked(view: View, type: InquiryStatusEnum, parcelId: Int)
            {
                super.onUpdateParcelAliasClicked(view, type, parcelId)

                val edit = MutableLiveData<String>()

                AlertUtil.updateValueDialog(requireContext(), "물품명을 입력해주세요.", Pair("확인", View.OnClickListener {
                    edit.observe(requireActivity()) { parcelAlias ->
                        vm.onUpdateParcelAlias(parcelId, parcelAlias)
                        AlertUtil.onDismiss()
                    }
                }), Pair("취소", null)) {
                    edit.value = it
                }
            }

        }
    }

    private fun setListener()
    { // 당겨서 새로고침 !
        binding.swipeLayoutMainOngoing.setOnRefreshListener {

            if(!refreshDelay)
            {
                refreshDelay = true

                vm.syncParcelsByOngoing()

                //5초후에 실행
                Timer().schedule(object: TimerTask()
                                 {
                                     override fun run()
                                     {
                                         CoroutineScope(Dispatchers.Main).launch {
                                             refreshDelay = false
                                         }
                                     }
                                 }, 5000)

                binding.swipeLayoutMainOngoing.isRefreshing = false

                return@setOnRefreshListener
            }

            Toast.makeText(requireContext(), "5초 후에 다시 새로고침을 시도해주세요.", Toast.LENGTH_LONG).show()

            binding.swipeLayoutMainOngoing.isRefreshing = false
        }
    }

    // '곧 도착' 리스트의 아이템의 개수에 따른 화면세팅
    private fun viewSettingForSoonArrivalList(listSize: Int)
    {
        if(listSize > 0)
        {
            binding.constraintSoonArrival.visibility = View.VISIBLE
            return
        }

        binding.constraintSoonArrival.visibility = View.GONE
    }

    // TODO : 데이터 바인딩으로 처리할 수 있으면 수정
    private fun viewSettingForRegisteredList(listSize: Int)
    {
        when(listSize)
        {
            0 ->
            {
                binding.constraintRegisteredArrival.visibility = View.GONE
            }
            else ->
            {
                binding.constraintRegisteredArrival.visibility = View.VISIBLE
            }
        }
    }

}