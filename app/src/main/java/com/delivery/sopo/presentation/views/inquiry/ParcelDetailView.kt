package com.delivery.sopo.presentation.views.inquiry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.ParcelDetailViewBinding
import com.delivery.sopo.databinding.StatusDisplayBinding
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.*
import com.delivery.sopo.presentation.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.presentation.views.adapter.TimeLineRecyclerViewAdapter
import com.delivery.sopo.presentation.views.main.MainView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.properties.Delegates


class ParcelDetailView: BaseFragment<ParcelDetailViewBinding, ParcelDetailViewModel>()
{
    private val parentView: MainView by lazy { activity as MainView }

    override val layoutRes: Int = R.layout.parcel_detail_view
    override val mainLayout: View by lazy { binding.relativeMainInquiryDetail }
    override val vm: ParcelDetailViewModel by viewModel()

    var parcelId by Delegates.notNull<Int>()

    override fun receiveData(bundle: Bundle)
    {
        super.receiveData(bundle)
        parcelId = bundle.getInt(PARCEL_ID)
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

                if(binding.layoutMain.panelState == PanelState.COLLAPSED)
                {
                    requireActivity().supportFragmentManager.popBackStack()
                }
                else
                {
                    binding.layoutMain.panelState = PanelState.COLLAPSED
                }
            }
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        vm.requestParcelDetail(parcelId = parcelId)

        setListener()

        var _slideOffset: Float = 0.0f

        val elevation = binding.includeSemi.constraintSubCardview.elevation

        val onPanelSlideListener = object: SlidingUpPanelLayout.PanelSlideListener
        {
            override fun onPanelSlide(panel: View?, slideOffset: Float)
            {
                _slideOffset = slideOffset

                SopoLog.d("elevation => $elevation  | slideOffset $_slideOffset")

                CoroutineScope(Dispatchers.Main).launch {

                    if(slideOffset == 0.0f)
                    {
                        binding.includeSemi.constraintSubCardview.elevation = elevation
                    }
                    else
                    {
                        binding.includeSemi.constraintSubCardview.elevation = elevation - ((elevation / 3) * slideOffset * 10)
                    }


                }

                when
                {
                    slideOffset < 0.3 ->
                    { // 테두리
                        binding.layoutDrawer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_50))

                        binding.includeSemi.root.makeVisible()
                        binding.includeFull.root.makeGone()
                    }
                    slideOffset < 0.7 ->
                    {
                        binding.layoutDrawer.setBackgroundResource(R.color.MAIN_WHITE)
                        binding.includeSemi.root.makeGone()
                        binding.includeFull.layoutHedaer.makeVisible()
                        binding.includeFull.root.makeVisible()
                    }
                }

                when(slideOffset < 1.0f)
                {
                    true ->
                    {
                        WindowUtil.setWindowStatusBarColor(requireActivity(), R.color.COLOR_GRAY_50)
                    }
                    false ->
                    {
                        WindowUtil.setWindowStatusBarColor(requireActivity(), R.color.MAIN_WHITE)
                    }
                }

            }

            override fun onPanelStateChanged(panel: View?, previousState: PanelState?, newState: PanelState)
            {
                if(_slideOffset < 0.1 && previousState == PanelState.DRAGGING)
                {
                    binding.layoutMain.panelState = PanelState.COLLAPSED
                }
                else if(_slideOffset == 1.0f && previousState == PanelState.DRAGGING)
                {
                    binding.layoutMain.panelState = PanelState.EXPANDED
                }
            }
        }

        binding.layoutMain.addPanelSlideListener(onPanelSlideListener)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        val vg = binding.root.parent as ViewGroup?
        vg?.removeView(binding.root)
    }

    private fun pasteWaybillNumIntoClipboard(tv: TextView)
    {
        val copyText = tv.text.toString()
        ClipboardUtil.copyTextToClipboard(requireContext(), copyText)
        Toast.makeText(requireContext(), "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT)
            .show()
    }

    private fun setListener()
    {
        binding.includeSemi.ivCopy.setOnClickListener {
            pasteWaybillNumIntoClipboard(binding.includeSemi.tvWaybillNum)
        }

        binding.includeFull.ivCopy.setOnClickListener {
            pasteWaybillNumIntoClipboard(binding.includeFull.tvWaybillNum)
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.getCurrentPage().observe(this) {
            if(it != 1) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->
            when(nav)
            {
                NavigatorConst.TO_BACK_SCREEN ->
                {
                    if(binding.layoutMain.panelState == PanelState.COLLAPSED)
                    {
                        FragmentManager.remove(requireActivity())
                    }
                    else
                    {
                        binding.layoutMain.panelState = PanelState.COLLAPSED
                    }
                }
            }

        }

        vm.parcelDetail.observe(this) { parcelDetail ->

            val adapter = TimeLineRecyclerViewAdapter().apply { setItemList(parcelDetail.timeLineProgresses) }

            binding.setVariable(BR.timeLineAdapter, adapter)

            val indicators = vm.getDeliveryStatusIndicator(parcelDetail.deliverStatus)

            setIndicatorView(baseLayout = binding.includeSemi.layoutDetailContent, topView = binding.includeSemi.vGuideline, bottomView = null, list = indicators)
            setIndicatorView(baseLayout = binding.includeFull.layoutDetailContent, topView = binding.includeFull.tvTitle, bottomView = binding.includeFull.vEmpty, list = indicators)

            updateDrawerLayoutSize(binding.includeSemi.root).start()
        }
    }

    // 동적으로 indicator view 생성
    private fun setIndicatorView(baseLayout: LinearLayout, list: List<SelectItem<String>>, topView: View?, bottomView: View?)
    {
        val inflater = requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val linearParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        linearParams.leftMargin = SizeUtil.changeDpToPx(requireActivity(), 12.0f)
        linearParams.rightMargin = SizeUtil.changeDpToPx(requireActivity(), 12.0f)

        // 기존에 있는 자식 뷰를 초기화
        if(baseLayout.childCount > 0) baseLayout.removeAllViews()

        for(item in list)
        { // 해당 xml binding

            if(item.item == DeliveryStatusEnum.NOT_REGISTERED.TITLE ||
                item.item == DeliveryStatusEnum.ORPHANED.TITLE ||
                item.item == DeliveryStatusEnum.ERROR.TITLE ||
                item.item == DeliveryStatusEnum.INFORMATION_RECEIVED.TITLE)
            {
                continue
            }

            val itemBinding = StatusDisplayBinding.inflate(inflater, baseLayout, false)
            itemBinding.lifecycleOwner = this
            itemBinding.item = item
            itemBinding.selectRes = R.drawable.ic_status_indicator
            itemBinding.unselectRes = R.drawable.ic_status_oval

            itemBinding.layoutMain.layoutParams = linearParams

            // 배송 상태 현재 step의 image view의 세팅을 변경
            if(item.isSelect)
            {
                val ivParam =
                    LinearLayout.LayoutParams(SizeUtil.changeDpToPx(requireActivity(), 30.0f), SizeUtil.changeDpToPx(requireActivity(), 30.0f))
                ivParam.bottomMargin = SizeUtil.changeDpToPx(requireActivity(), 3.0f)
                itemBinding.ivIndicator.layoutParams = ivParam

                val typeface = ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)

                itemBinding.tvStatus.typeface = typeface
            }

            baseLayout.addView(itemBinding.root)
        }

        val constraintParams =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)

        if(topView == null) constraintParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        else constraintParams.topToBottom = topView.id

        constraintParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        constraintParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        constraintParams.topMargin = SizeUtil.changeDpToPx(requireActivity(), 27.0f)

        if(bottomView == null)
        {
            constraintParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID

            constraintParams.bottomMargin = SizeUtil.changeDpToPx(requireActivity(), 30.0f)
            baseLayout.layoutParams = constraintParams
            return
        }

        val constraintParams2 =
            ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, SizeUtil.changeDpToPx(requireActivity(), 8.0f))

        constraintParams2.topToBottom = baseLayout.id
        constraintParams2.topMargin = SizeUtil.changeDpToPx(requireActivity(), 40.0f)

        bottomView.layoutParams = constraintParams2
    }

    // 하단 드로우 레이아웃 사이즈 변경
    private fun updateDrawerLayoutSize(view: View) = CoroutineScope(Dispatchers.Main).launch {
        val height = view.height
        binding.layoutMain.panelHeight = height
    }

    companion object
    {
        private val PARCEL_ID = "PARCEL_ID"
        private val IS_BE_UPDATED = "IS_BE_UPDATED"

        // 해당 프래그먼트를 인스턴스화 할 때 무조건 newInstance로 호출해야한다.
        fun newInstance(parcelId: Int, isBeUpdated: Boolean = false): ParcelDetailView
        {
            val fragment = ParcelDetailView()

            val args = Bundle()

            // parcel의 uid, regDt를 parameter로 가진다.
            args.run {
                putInt(PARCEL_ID, parcelId)
                putBoolean(IS_BE_UPDATED, isBeUpdated)
            }

            fragment.arguments = args

            return fragment
        }

    }

}