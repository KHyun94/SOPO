package com.delivery.sopo.views.inquiry

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ParcelDetailViewBinding
import com.delivery.sopo.databinding.StatusDisplayBinding
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.views.main.MainView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ParcelDetailView : Fragment()
{
    private lateinit var parentView: MainView
    lateinit var binding: ParcelDetailViewBinding

    private val vm: ParcelDetailViewModel by viewModel()

    private lateinit var parcelId : ParcelId
    private var parcelUId: String = ""
    private var regDt: String = ""

    private var slideViewStatus = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        parentView = activity as MainView

        arguments?.run {
            parcelId = ParcelId(parcelUid = getString(PARCEL_UID)?:"", regDt= getString(REQ_DT)?:"")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        bindView(inflater = inflater, container = container)
        setObserve()

        // TODO 상세 내역 상태 텍스트, 이미지를 맨 앞으로 순서 변경
        binding.ivStatus.bringToFront()
        binding.tvSubtext.bringToFront()

        // 택배 info LiveData 데이터 입력
        binding.vm!!.parcelId.value = parcelId

        // TODO include view를 사용했을 때 parameter로 clickListener 셋 할 필요 있음
        binding.includeSemi.ivCopy.setOnClickListener {
            val copyText = binding.includeSemi.tvWayBilNum.text.toString()
            ClipboardUtil.copyTextToClipboard(activity!!, copyText)
            Toast.makeText(activity!!, "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT).show()
        }

        binding.includeFull.ivCopy.setOnClickListener {
            val copyText = binding.includeFull.tvWayBilNum.text.toString()
            ClipboardUtil.copyTextToClipboard(activity!!, copyText)
            Toast.makeText(activity!!, "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        binding.root.parent?.also {
            (it as ViewGroup).removeView(binding.root)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        var _slideOffset: Float = 0.0f

        binding.layoutMain.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener
        {
            override fun onPanelSlide(panel: View?, slideOffset: Float)
            {
                _slideOffset = slideOffset

                CoroutineScope(Dispatchers.Main).launch {

                    when
                    {
                        _slideOffset < 0.1 ->
                        {
                            // 테두리
                            binding.layoutDrawer.setBackgroundResource(R.drawable.border_drawer)

                            binding.includeSemi.root.visibility = View.VISIBLE
                            binding.includeFull.root.visibility = View.GONE

                            slideViewStatus = 0
                        }
                        else ->
                        {
                            // 테두리
                            binding.layoutDrawer.setBackgroundResource(R.color.MAIN_WHITE)

                            binding.includeSemi.root.visibility = View.GONE
                            binding.includeFull.root.visibility = View.VISIBLE

                            slideViewStatus = 1
                        }
                    }
                }

            }

            override fun onPanelStateChanged(
                panel: View?,
                previousState: PanelState?,
                newState: PanelState
            )
            {
                CoroutineScope(Dispatchers.Main).launch {
                    if (_slideOffset < 0.1 && previousState == SlidingUpPanelLayout.PanelState.DRAGGING)
                    {
                        SopoLog.d("닫힘 -> pre {$previousState } cur {$newState }")
                        binding.layoutMain.panelState = PanelState.COLLAPSED
                    }
                    else if (_slideOffset == 1.0f && previousState == PanelState.DRAGGING)
                    {
                        SopoLog.d( "열림 -> pre {$previousState } cur {$newState }")
                        binding.layoutMain.panelState = PanelState.EXPANDED
                    }
                }

            }
        })

    }

    // binding setting
    private fun bindView(inflater: LayoutInflater, container: ViewGroup?)
    {
        binding = ParcelDetailViewBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    private fun setObserve()
    {
        parentView.currentPage.observe(this, Observer {
            if (it != null && it == 1)
            {
                callback = object : OnBackPressedCallback(true)
                {
                    override fun handleOnBackPressed()
                    {
                        SopoLog.d( msg = "ParcelDetailView:: BackPressListener")

                        if (slideViewStatus == 0)
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

        binding.vm!!.parcelId.observe(this, Observer {
            if (it != null)
            {
                binding.vm!!.updateIsUnidentifiedToZero(it)
                binding.vm!!.requestRemoteParcel(parcelId = it)
            }
        })

        binding.vm!!.parcelEntity.observe(this, Observer {
            if (it != null)
            {
                //todo Error
                binding.vm!!.updateParcelItem(it)
            }
        })

        binding.vm!!.statusList.observe(this, Observer {
            if (it != null)
            {
                setIndicatorView(
                    topView = binding.includeSemi.layoutAddView,
                    bottomView = null,
                    baseLayout = binding.includeSemi.layoutDetailContent,
                    list = it
                )

                updateDrawerLayoutSize(binding.includeSemi.root)

                setIndicatorView(
                    topView = binding.includeFull.tvTitle,
                    bottomView = binding.includeFull.vEmpty,
                    baseLayout = binding.includeFull.layoutDetailContent,
                    list = it
                )
            }
        })

        val progress = CustomProgressBar(this@ParcelDetailView.parentView)

        binding.vm!!.isProgress.observe(this, Observer { isProgress ->

            progress.autoProgressbar(isProgress)
        })

        binding.vm!!.isUpdate.observe(this, Observer {

            when (it)
            {
                true ->
                {
                    parentView.getAlertMessageBar().run {
                        setText("업데이트 사항이 있습니다.")
                        setOnCancelClicked("업데이트", null, View.OnClickListener {
                            binding.vm!!.getRemoteParcel(ParcelId(regDt, parcelUId))
                        })
                        onStart(null)
                    }
                }
                false ->
                {
                    parentView.getAlertMessageBar().run {
                        setText("업데이트 도중 에러가 발생했습니다.")
                        setOnCancelClicked("재시도", null, View.OnClickListener {
                            binding.vm!!.requestRemoteParcel(ParcelId(regDt, parcelUId))
                        })
                        onStart(null)
                    }
                }
                null ->
                {

                }
            }
        })

        binding.vm!!.isBack.observe(this, Observer {

            if (it != null)
            {
                if (it)
                {
                    FragmentManager.remove(activity!!, this@ParcelDetailView)
                    binding.vm!!.isBack.call()
                }
            }

        })

        binding.vm!!.isDown.observe(this, Observer {
            if (it != null)
            {
                if (it)
                {
                    binding.layoutMain.panelState = PanelState.COLLAPSED
                    binding.vm!!.isDown.call()
                }
            }
        })
    }

    // 동적으로 indicator view 생성
    private fun setIndicatorView(
        topView: View?,
        bottomView: View?,
        baseLayout: LinearLayout,
        list: List<SelectItem<String>>
    )
    {
        val inflater =
            activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val linearParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        linearParams.leftMargin = SizeUtil.changeDpToPx(activity!!, 9.0f)
        linearParams.rightMargin = SizeUtil.changeDpToPx(activity!!, 9.0f)

        // 기존에 있는 자식 뷰를 초기화
        if (baseLayout.childCount > 0) baseLayout.removeAllViews()

        for (item in list)
        {
            // 해당 xml binding
            val itemBinding = StatusDisplayBinding.inflate(inflater, baseLayout, false)
            itemBinding.lifecycleOwner = this
            itemBinding.item = item
            itemBinding.selectRes = R.drawable.ic_status_indicator
            itemBinding.unselectRes = R.drawable.ic_status_oval

            itemBinding.layoutMain.layoutParams = linearParams

            // 배송 상태 현재 step의 image view의 세팅을 변경
            if (item.isSelect)
            {
                val ivParam = LinearLayout.LayoutParams(
                    SizeUtil.changeDpToPx(activity!!, 30.0f),
                    SizeUtil.changeDpToPx(activity!!, 30.0f)
                )
                ivParam.bottomMargin = SizeUtil.changeDpToPx(activity!!, 3.0f)
                itemBinding.ivIndicator.layoutParams = ivParam
            }

            baseLayout.addView(itemBinding.root)
        }

        // todo 추후 param 받아서 처리하거나 callback 처리를 해서 빼도록 처리할 예정
        if (topView != null)
        {
            if (topView is LinearLayout)
            {
                val constraintParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                constraintParams.topToBottom = topView.id
                constraintParams.leftToRight = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.topMargin = SizeUtil.changeDpToPx(activity!!, 34.0f)

                baseLayout.layoutParams = constraintParams
            }
            else
            {
                val constraintParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
                )

                constraintParams.topToBottom = topView.id
                constraintParams.leftToRight = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.rightToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                constraintParams.topMargin = SizeUtil.changeDpToPx(activity!!, 27.0f)

                baseLayout.layoutParams = constraintParams

                val constraintParams2 = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    SizeUtil.changeDpToPx(activity!!, 8.0f)
                )

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

        callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                if (slideViewStatus == 0)
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

    override fun onDetach()
    {
        super.onDetach()
        callback!!.remove()
    }

    companion object
    {
        private val PARCEL_UID = "PARCEL_UID"
        private val REQ_DT = "REQ_DT"
        private val IS_BE_UPDATED = "IS_BE_UPDATED"

        // 해당 프래그먼트를 인스턴스화 할 때 무조건 newInstance로 호출해야한다.
        fun newInstance(
            parcelUId: String,
            regDt: String,
            isBeUpdated: Boolean = false
        ): ParcelDetailView
        {
            val fragment = ParcelDetailView()

            val args = Bundle()

            // parcel의 uid, regDt를 parameter로 가진다.
            args.run {
                putString(PARCEL_UID, parcelUId)
                putString(REQ_DT, regDt)
                putBoolean(IS_BE_UPDATED, isBeUpdated)
            }

            fragment.arguments = args

            return fragment
        }

    }

}