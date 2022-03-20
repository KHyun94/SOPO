package com.delivery.sopo.views.inquiry

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
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ParcelDetailViewBinding
import com.delivery.sopo.databinding.StatusDisplayBinding
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.*
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.views.main.MainView
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

        CoroutineScope(Dispatchers.Main).launch {
            vm.updateUnidentifiedStatusToZero(parcelId = parcelId)
            vm.requestParcelDetailData(parcelId = parcelId)
        }

        setListener()

        var _slideOffset: Float = 0.0f

        binding.layoutMain.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener
                                                 {
                                                     override fun onPanelSlide(panel: View?, slideOffset: Float)
                                                     { //                                                         panel?.alpha = 0xffffff.toFloat()
                                                         _slideOffset = slideOffset
                                                         CoroutineScope(Dispatchers.Main).launch {
                                                             when
                                                             {
                                                                 _slideOffset < 0.3 ->
                                                                 { // 테두리
                                                                     binding.layoutDrawer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.COLOR_GRAY_50))

                                                                     binding.includeSemi.root.makeVisible()
                                                                     binding.includeFull.root.makeGone()
                                                                 }
                                                                 _slideOffset < 0.7 ->
                                                                 {
                                                                     binding.layoutDrawer.setBackgroundResource(R.color.MAIN_WHITE)
                                                                     binding.includeSemi.root.makeGone()
                                                                     binding.includeFull.layoutHedaer.makeVisible()
                                                                     binding.includeFull.root.makeVisible()
                                                                 }

                                                             }
                                                         }

                                                     }

                                                     override fun onPanelStateChanged(panel: View?, previousState: PanelState?, newState: PanelState)
                                                     { //                                                         CoroutineScope(Dispatchers.Main).launch {
                                                         if(_slideOffset < 0.1 && previousState == SlidingUpPanelLayout.PanelState.DRAGGING)
                                                         {
                                                             binding.layoutMain.panelState =
                                                                 PanelState.COLLAPSED
                                                         }
                                                         else if(_slideOffset == 1.0f && previousState == PanelState.DRAGGING)
                                                         {
                                                             binding.layoutMain.panelState =
                                                                 PanelState.EXPANDED
                                                         }

                                                         //                                                         }

                                                     }
                                                 })
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
        parentView.currentPage.observe(this) {
            if(it != 1) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.statusList.observe(requireActivity(), Observer { list ->
            if(list == null) return@Observer

            setIndicatorView(baseLayout = binding.includeSemi.layoutDetailContent, topView = binding.includeSemi.vGuideline, bottomView = null, list = list)
            setIndicatorView(baseLayout = binding.includeFull.layoutDetailContent, topView = binding.includeFull.tvTitle, bottomView = binding.includeFull.vEmpty, list = list)

            updateDrawerLayoutSize(binding.includeSemi.root).start()
        })

        vm.isBack.observe(requireActivity(), Observer {

            if(it == null) return@Observer

            if(it)
            {
                FragmentManager.remove(requireActivity())
                vm.isBack.call()
            }
        })

        vm.isDragOut.observe(requireActivity(), Observer {
            if(it != null)
            {
                if(it)
                {
                    binding.layoutMain.panelState = PanelState.COLLAPSED
                    vm.isDragOut.call()
                }
            }
        })
    }

    // 동적으로 indicator view 생성
    private fun setIndicatorView(baseLayout: LinearLayout, list: List<SelectItem<String>>, topView: View?, bottomView: View?)
    {
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val linearParams =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        linearParams.leftMargin = SizeUtil.changeDpToPx(requireActivity(), 12.0f)
        linearParams.rightMargin = SizeUtil.changeDpToPx(requireActivity(), 12.0f)

        // 기존에 있는 자식 뷰를 초기화
        if(baseLayout.childCount > 0) baseLayout.removeAllViews()

        for(item in list)
        { // 해당 xml binding
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