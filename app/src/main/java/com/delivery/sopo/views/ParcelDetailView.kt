package com.delivery.sopo.views

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.sopo.R
import com.delivery.sopo.databinding.ParcelDetailViewBinding
import com.delivery.sopo.databinding.StatusDisplayBinding
import com.delivery.sopo.models.StatusItem
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.util.fun_util.SizeUtil
import com.delivery.sopo.viewmodels.ParcelDetailViewModel
import com.delivery.sopo.views.adapter.TimeLineRvAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class ParcelDetailView : Fragment()
{
    val TAG = "LOG.SOPO"

    lateinit var binding: ParcelDetailViewBinding
    private val vm: ParcelDetailViewModel by viewModel()



    private var parcelUId : String? = null
    private var regDt : String? = null

    var statusList = mutableListOf<StatusItem>(
        StatusItem("상품인수", false),
        StatusItem("배송중", true),
        StatusItem("동네도착", false),
        StatusItem("배송완료", false)
    )

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        if(arguments != null)
        {
            parcelUId = arguments?.getString(PARCEL_UID)
            regDt = arguments?.getString(REQ_DT)
        }

        NetworkManager.initPrivateApi("gnltlgnlrl94@naver.com", "A9E5E837F775F17A6C3B16F810FD74EFA6CFF5064D95977497E5091DC177BDC2")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        bindView(inflater = inflater, container = container)
        parcelUId = "54f61e1a-0439-41bf-b07d-cf51835c0dc3"
        regDt = "2020-09-19"
        if(parcelUId != null && regDt != null)
        {
            Log.d(TAG, "실행 전")
            binding.vm!!.requestFetchParcel(parcelUId = parcelUId!!, regDt = regDt!!)


        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        setIndicatorView(
            binding.includeSemi.layoutAddView,
            null,
            binding.includeSemi.layoutDetailContent,
            statusList
        )
        setIndicatorView(
            binding.includeFull.tvTitle,
            binding.includeFull.vEmpty,
            binding.includeFull.layoutDetailContent,
            statusList
        )

        updateDrawerLayoutSize(binding.includeSemi.root)

        Log.d(TAG, "Rv 호출 전")

        binding.includeFull.rvTimeLine.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = TimeLineRvAdapter()
        }


        var location: IntArray = IntArray(2)
        binding.layoutDrawer.setOnTouchListener { view: View, event: MotionEvent ->
            when (event.action)
            {
                MotionEvent.ACTION_MOVE ->
                {
                    view.getLocationOnScreen(location)

                    if (location[1] > 72)
                    {
                        activity!!.runOnUiThread {
//                            layout_drawer.setBackgroundResource(R.color.MAIN_WHITE)
                            binding.includeSemi.root.visibility = View.GONE
                            binding.includeFull.root.visibility = View.VISIBLE
                            binding.layoutMain.isEnabled = true

                        }
                    }
                    else
                    {
                        activity!!.runOnUiThread {
//                            layout_drawer.setBackgroundResource(R.drawable.border_drawer)

                            binding.includeSemi.root.visibility = View.VISIBLE
                            binding.includeFull.root.visibility = View.GONE
                            binding.layoutMain.isEnabled = false
                        }
                    }

                    Log.d("LOG.SOPO", "Event X => ${event.x}")
                    Log.d("LOG.SOPO", "Event Y => ${event.y}")
                }

            }
            //리턴값은 return 없이 아래와 같이
            true // or false
        }

    }

    // binding setting
    private fun bindView(inflater: LayoutInflater, container: ViewGroup?)
    {
        binding = ParcelDetailViewBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
    }

    // 동적으로 indicator view 생성
    private fun setIndicatorView(
        topView: View?,
        bottomView: View?,
        baseLayout: LinearLayout,
        list: List<StatusItem>
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
            if (item.isCurrent)
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

    // 하단 드로우 레이아웃 사이즈 변경
    private fun updateDrawerLayoutSize(view: View)
    {
        val globalListener = ViewTreeObserver.OnGlobalLayoutListener {
            val height = view.height
            binding.layoutMain.panelHeight = height
        }

        view.viewTreeObserver.run {
            addOnGlobalLayoutListener(globalListener)
        }

    }

    companion object{

        private val PARCEL_UID = "PARCEL_UID"
        private val REQ_DT = "REQ_DT"

        // 해당 프래그먼트를 인스턴스화 할 때 무조건 newInstance로 호출해야한다.
        fun newInstance(parcelUId:String, regDt:String):ParcelDetailView
        {
            val fragment = ParcelDetailView()

            val args = Bundle()

            // parcel의 uid, regDt를 parameter로 가진다.
            args.run {
                putString(PARCEL_UID, parcelUId)
                putString(REQ_DT, regDt)
            }

            fragment.arguments = args

            return fragment
        }

    }

}