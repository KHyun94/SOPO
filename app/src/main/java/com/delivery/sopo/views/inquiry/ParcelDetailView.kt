package com.delivery.sopo.views.inquiry

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.BR
import com.delivery.sopo.R
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.databinding.ParcelDetailViewBinding
import com.delivery.sopo.databinding.StatusDisplayBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.android.synthetic.main.parcel_full_detail_item.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class ParcelDetailView: Fragment()
{
    private lateinit var parentView: MainView
    lateinit var binding: ParcelDetailViewBinding
    private val vm: ParcelDetailViewModel by viewModel()

    private var slideViewStatus = 0

    private var progressBar: CustomProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        parentView = activity as MainView
        receiveBundleData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        binding = bindView(inflater = inflater, container = container, layoutRes = R.layout.parcel_detail_view)
        setObserve()
        setViewSetting()
        setListener()

        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        val vg = binding.root.parent as ViewGroup
        vg.removeView(binding.root)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        var _slideOffset: Float = 0.0f

        binding.layoutMain.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float)
            {
                _slideOffset = slideOffset
                CoroutineScope(Dispatchers.Main).launch {
                    when
                    {
                        _slideOffset < 0.1 ->
                        {
                            // 테두리
                            binding.layoutDrawer.setBackgroundResource(R.drawable.border_rounded_30dp)
                            binding.includeSemi.root.visibility = View.VISIBLE
                            binding.includeFull.root.visibility = View.GONE
                            slideViewStatus = 0
                        }
                        _slideOffset < 0.7 ->
                        {
                            binding.layoutDrawer.setBackgroundResource(R.drawable.border_rounded_15dp)
                            binding.includeFull.root.layout_hedaer.visibility = View.INVISIBLE
                            binding.includeSemi.root.visibility = View.GONE
                            binding.includeFull.root.visibility = View.VISIBLE
                            slideViewStatus = 1
                        }
                        else ->
                        {
                            // 테두리
                            binding.layoutDrawer.setBackgroundResource(R.color.MAIN_WHITE)
                            binding.includeFull.root.layout_hedaer.visibility = View.VISIBLE
                            binding.includeSemi.root.visibility = View.GONE
                            binding.includeFull.root.visibility = View.VISIBLE
                            slideViewStatus = 1
                        }
                     }
                 }

             }

            override fun onPanelStateChanged(panel: View?, previousState: PanelState?, newState: PanelState)
            {
                CoroutineScope(Dispatchers.Main).launch {
                    if(_slideOffset < 0.1 && previousState == SlidingUpPanelLayout.PanelState.DRAGGING) binding.layoutMain.panelState = PanelState.COLLAPSED
                    else if(_slideOffset == 1.0f && previousState == PanelState.DRAGGING) binding.layoutMain.panelState = PanelState.EXPANDED

                }

            }
        })
    }

    override fun onDetach()
    {
        super.onDetach()
        callback!!.remove()
    }

    private fun receiveBundleData()
    {
        arguments?.let { bundle ->
            val parcelId = bundle.getInt(PARCEL_ID)
            SopoLog.d("[$parcelId]의 상세 페이지")
            vm.parcelId.value = parcelId
        }
    }

    private fun pasteWaybillNumIntoClipboard(tv: TextView)
    {
        val copyText = tv.text.toString()
        ClipboardUtil.copyTextToClipboard(activity!!, copyText)
        Toast.makeText(activity!!, "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT).show()
    }

    private fun setViewSetting(){
        binding.ivStatus.bringToFront()
        binding.tvSubtext.bringToFront()
    }

    private fun setListener(){
        binding.includeSemi.ivCopy.setOnClickListener {
            pasteWaybillNumIntoClipboard(binding.includeSemi.tvWaybillNum)
        }

        binding.includeFull.ivCopy.setOnClickListener {
            pasteWaybillNumIntoClipboard(binding.includeFull.tvWaybillNum)
        }
    }

    private fun <T: ViewDataBinding> bindView(inflater: LayoutInflater, @LayoutRes layoutRes: Int, container: ViewGroup?): T
    {
        val binding = DataBindingUtil.inflate<T>(inflater, layoutRes, container, false)
        binding.setVariable(BR.vm, vm)
        binding.lifecycleOwner = this
        return binding
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer {
            if(it != null && it == TabCode.secondTab)
            {
                callback = object: OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d(msg = "ParcelDetailView:: BackPressListener")

                        if(slideViewStatus == 0)
                        {
                            requireActivity().supportFragmentManager.popBackStack()
                        }
                        else
                        {
                            binding.layoutMain.panelState = PanelState.COLLAPSED
                        }
                    }
                }
                requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
            }
        })

        vm.parcelId.observe(this, Observer {parcelId ->
            CoroutineScope(Dispatchers.Main).launch {
                vm.updateUnidentifiedStatusToZero(parcelId = parcelId)
                vm.requestParcelDetailData(parcelId = parcelId)
            }
        })

        vm.result.observe(this, Observer { res ->
            if(res.result) return@Observer

//            when(res.code){
//                GeneralDialog
//            }

        })

        vm.statusList.observe(this, Observer { list ->
            if(list == null) return@Observer

            setIndicatorView(topView = binding.includeSemi.layoutAddView, bottomView = null,
                             baseLayout = binding.includeSemi.layoutDetailContent, list = list)

            updateDrawerLayoutSize(binding.includeSemi.root)

            setIndicatorView(topView = binding.includeFull.tvTitle,
                             bottomView = binding.includeFull.vEmpty,
                             baseLayout = binding.includeFull.layoutDetailContent, list = list)
        })

        vm.isProgress.observe(this, Observer { isProgress ->
            if(isProgress == null) return@Observer

            if(progressBar == null)
            {
                progressBar = CustomProgressBar(parentView)
            }

            progressBar?.onStartProgress(isProgress) { isDismiss ->
                if(isDismiss) progressBar = null
            }

        })

        vm.updateType.observe(this, Observer { type ->

            val (message, clickMessage, clickListener) = when(type)
            {
                StatusConst.SUCCESS ->
                {
                    Triple("업데이트 사항이 있습니다.", "업데이트", View.OnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val parcelId = vm.parcelId.value ?: throw Exception("Parcel id가 존재하지 않습니다.")
                            vm.getRemoteParcel(parcelId)
                            parentView.getAlertMessageBar().onDismiss()
                        }
                    })
                }
                StatusConst.FAILURE ->
                {
                    Triple("업데이트 도중 에러가 발생했습니다.", "재시도", View.OnClickListener {
                        CoroutineScope(Dispatchers.IO).launch {
                            val parcelId =
                                vm.parcelId.value ?: throw Exception("Parcel id가 존재하지 않습니다.")
                            vm.requestParcelForRefresh(parcelId)
                            parentView.getAlertMessageBar().onDismiss()
                        }
                    })
                }
                else -> return@Observer SopoLog.e("올바른 업데이트 형식이 아닙니다. - type[${type}]")
            }

            parentView.getAlertMessageBar().run {
                setText(message)
                setOnCancelClicked(clickMessage, null, clickListener)
                onStart()
            }

        })

        vm.isBack.observe(this, Observer {

            if(it == null) return@Observer

            if(it)
            {
                FragmentManager.remove(activity!!)
                vm.isBack.call()
            }
        })

        vm.isDragOut.observe(this, Observer {
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
    private fun setIndicatorView(topView: View?, bottomView: View?, baseLayout: LinearLayout, list: List<SelectItem<String>>)
    {
        val inflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val linearParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                                     LinearLayout.LayoutParams.WRAP_CONTENT)

        linearParams.leftMargin = SizeUtil.changeDpToPx(activity!!, 12.0f)
        linearParams.rightMargin = SizeUtil.changeDpToPx(activity!!, 12.0f)

        // 기존에 있는 자식 뷰를 초기화
        if(baseLayout.childCount > 0) baseLayout.removeAllViews()

        for(item in list)
        {
            // 해당 xml binding
            val itemBinding = StatusDisplayBinding.inflate(inflater, baseLayout, false)
            itemBinding.lifecycleOwner = this
            itemBinding.item = item
            itemBinding.selectRes = R.drawable.ic_status_indicator
            itemBinding.unselectRes = R.drawable.ic_status_oval

            itemBinding.layoutMain.layoutParams = linearParams

            // 배송 상태 현재 step의 image view의 세팅을 변경
            if(item.isSelect)
            {
                val ivParam = LinearLayout.LayoutParams(SizeUtil.changeDpToPx(activity!!, 30.0f),
                                                        SizeUtil.changeDpToPx(activity!!, 30.0f))
                ivParam.bottomMargin = SizeUtil.changeDpToPx(activity!!, 3.0f)
                itemBinding.ivIndicator.layoutParams = ivParam

                val typeface =
                    ResourcesCompat.getFont(requireContext(), R.font.spoqa_han_sans_neo_bold)

                itemBinding.tvStatus.typeface = typeface
            }

            baseLayout.addView(itemBinding.root)
        }

        // todo 추후 param 받아서 처리하거나 callback 처리를 해서 빼도록 처리할 예정
        if(topView != null)
        {
            if(topView is LinearLayout)
            {
                val constraintParams =
                    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                                  ConstraintLayout.LayoutParams.WRAP_CONTENT)

                constraintParams.topToBottom = topView.id
                constraintParams.leftToRight = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.topMargin = SizeUtil.changeDpToPx(activity!!, 34.0f)

                baseLayout.layoutParams = constraintParams
            }
            else
            {
                val constraintParams =
                    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                                  ConstraintLayout.LayoutParams.WRAP_CONTENT)

                constraintParams.topToBottom = topView.id
                constraintParams.leftToRight = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.topMargin = SizeUtil.changeDpToPx(activity!!, 27.0f)

                baseLayout.layoutParams = constraintParams

                val constraintParams2 =
                    ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                                                  SizeUtil.changeDpToPx(activity!!, 8.0f))

                constraintParams2.topToBottom = baseLayout.id
                constraintParams2.topMargin = SizeUtil.changeDpToPx(activity!!, 40.0f)

                bottomView!!.layoutParams = constraintParams2
            }
        }
    }

    //    var bar = updateDrawerLayoutSize(layout)

    // 하단 드로우 레이아웃 사이즈 변경
    private fun updateDrawerLayoutSize(view: View)
    {
        val globalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val height = view.height
            binding.layoutMain.panelHeight = height
        }

        view.viewTreeObserver.run {
            addOnGlobalLayoutListener(globalListener)

            Handler().postDelayed(Runnable {
                removeOnGlobalLayoutListener(globalListener)
            }, 1000)

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
                if(slideViewStatus == 0)
                {
                    requireActivity().supportFragmentManager.popBackStack()
                }
                else
                {
                    binding.layoutMain.panelState = PanelState.COLLAPSED
                }
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback!!)
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