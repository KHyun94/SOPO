package com.delivery.sopo.views.inquiry

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
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
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.ClipboardUtil
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SizeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.views.main.MainView
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState
import kotlinx.android.synthetic.main.main_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class ParcelDetailView : Fragment()
{
    val TAG = "LOG.SOPO"

    private lateinit var parentView: MainView

    private val userRepoImpl: UserRepoImpl by inject()
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    lateinit var binding: ParcelDetailViewBinding
    private val vm: ParcelDetailViewModel by viewModel()

    private var parcelUId: String? = null
    private var regDt: String? = null

    private var slideViewStatus = 0;

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        NetworkManager.initPrivateApi(userRepoImpl.getEmail(), userRepoImpl.getApiPwd())

        if (arguments != null)
        {
            parcelUId = arguments?.getString(PARCEL_UID)
            regDt = arguments?.getString(REQ_DT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        parentView = activity as MainView

        bindViewSetting(inflater = inflater, container = container)
        setObserve()

        // 택배 info LiveData 데이터 입력
        binding.vm!!.parcelId.value = ParcelId(regDt!!, parcelUId!!)

        parentView = activity as MainView

        binding.includeSemi.ivCopy.setOnClickListener {
            val copyText = binding.includeSemi.tvWaybilNum.text.toString()
            ClipboardUtil.copyTextToClipboard(activity!!, copyText)

            Toast.makeText(activity!!, "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT).show()
        }

        binding.includeFull.ivCopy.setOnClickListener {
            val copyText = binding.includeFull.tvWaybilNum.text.toString()
            ClipboardUtil.copyTextToClipboard(activity!!, copyText)

            Toast.makeText(activity!!, "운송장 번호 [$copyText]가 복사되었습니다!!!", Toast.LENGTH_SHORT).show()
        }

        parentView.alert_message_bar.setText("업데이트 사항이 있습니다.")
        parentView.alert_message_bar.setOnCancelClicked(
            "업데이트",
            R.color.MAIN_WHITE,
            View.OnClickListener {
                binding.vm!!.updateParcelItem(binding.vm!!.parcelEntity.value!!)
                parentView.alert_message_bar.onDismiss()
            })
        return binding.root
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        binding.root.parent?.also {
            Log.d("!!!!!!", "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ParcelDetailView DELETE")
            (it as ViewGroup).removeView(binding.root)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        var slideoffsetC: Float = 0.0f

        binding.layoutMain.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener
        {
            override fun onPanelSlide(panel: View?, slideOffset: Float)
            {
//                Log.i(TAG, "onPanelSlide, offset $slideOffset")
                slideoffsetC = slideOffset
                CoroutineScope(Dispatchers.Main).launch {
                    if (slideoffsetC < 0.1)
                    {
                        // 테두리
                        binding.layoutDrawer.setBackgroundResource(R.drawable.border_drawer)

                        binding.includeSemi.root.visibility = View.VISIBLE
                        binding.includeFull.root.visibility = View.GONE

                        slideViewStatus = 0
                    }
                    else
                    {
                        // 테두리
                        binding.layoutDrawer.setBackgroundResource(R.color.MAIN_WHITE)

                        binding.includeSemi.root.visibility = View.GONE
                        binding.includeFull.root.visibility = View.VISIBLE

                        slideViewStatus = 1
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
                    if (slideoffsetC < 0.1 && previousState == SlidingUpPanelLayout.PanelState.DRAGGING)
                    {
                        SopoLog.d("닫힘 -> pre {$previousState } cur {$newState }", null)
                        binding.layoutMain.panelState = PanelState.COLLAPSED
                    }
                    else if (slideoffsetC == 1.0f && previousState == PanelState.DRAGGING)
                    {
                        SopoLog.d("열림 -> pre {$previousState } cur {$newState }", null)
                        binding.layoutMain.panelState = PanelState.EXPANDED
                    }
                }

            }
        })

    }

    // binding setting
    private fun bindViewSetting(inflater: LayoutInflater, container: ViewGroup?)
    {
        binding = ParcelDetailViewBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.ivStatus.bringToFront()
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
                        Log.d(TAG, "ParcelDetailView:: BackPressListener")

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
                binding.vm!!.requestParcelDetailData(it)
            }
        })

        binding.vm!!.parcelEntity.observe(this, Observer {
            if (it != null)
            {
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

        binding.vm!!.isBeUpdated.observe(this, Observer {
            if (it != null && it == true)
            {
                SopoLog.d("상세 페이지 업데이트 여부 체크 ${it ?: "NULL"}")

                parentView.alert_message_bar.onStart(null)
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
                    binding.layoutMain.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
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
//            binding.layoutTail.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, height)

//            Log.d(TAG, "바텀 리니어 높이 => ${binding.layoutTail.height}")
//            Log.d(TAG, "드로어 높이 => ${binding.layoutMain.panelHeight}")
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