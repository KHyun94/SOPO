package com.delivery.sopo.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.R
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.OnAgreeClickListener
import com.delivery.sopo.views.login.LoginSelectView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.function.Function

typealias OnTextClickListener = Pair<String?, View.OnClickListener?>

object AlertUtil: KoinComponent
{
    val userSITORY: UserLocalRepository by inject()
    val O_AUTH_REPO_IMPL: OAuthLocalRepository by inject()
    val appDataBase: AppDatabase by inject()

    var alert: AlertDialog? = null

    fun updateValueDialog(context: Context, title: String, okListener: OnTextClickListener?, cancelListener: OnTextClickListener?, callback: Function<String, Unit>)
    {
        val constraintLayout =
            View.inflate(context, R.layout.alert_update_dialog, null) as ConstraintLayout

        val layoutMain: ConstraintLayout = constraintLayout.findViewById(R.id.layout_main)
        val etInputText: EditText = constraintLayout.findViewById(R.id.et_input_text)
        val vFocusStatus: View = constraintLayout.findViewById(R.id.v_focus_status)
        val tvTitle: TextView = constraintLayout.findViewById(R.id.tv_title)
        val tvCancelBtn: TextView = constraintLayout.findViewById(R.id.tv_cancel_btn)
        val tvOkBtn: TextView = constraintLayout.findViewById(R.id.tv_ok_btn)

        alert = AlertDialog.Builder(context).setView(constraintLayout).setCancelable(false).create()

        layoutMain.layoutParams = ConstraintLayout.LayoutParams((SizeUtil.changePxToDp(context, 288.0f).toInt()), SizeUtil.changePxToDp(context, 288.0f).toInt())

        // 테두리 라운딩 처리
        alert?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)
        }

        val param = alert?.window?.attributes
        param?.let { param ->
            param.width = SizeUtil.changePxToDp(context, 288.0f).toInt()
            param.height = SizeUtil.changePxToDp(context, 138.0f).toInt()
            alert?.window?.attributes = param
        }

        CoroutineScope(Dispatchers.Main).launch {

            tvTitle.text = title

            tvOkBtn.text = okListener?.first ?: "확인"
            if (okListener?.first != null) tvOkBtn.setOnClickListener(okListener.second)
            else
            {
                tvCancelBtn.visibility = View.GONE
                tvOkBtn.setOnClickListener { alert!!.dismiss() }
            }

            if (cancelListener?.first != null)
            {
                tvCancelBtn.text = cancelListener.first

                if (cancelListener.second != null) tvCancelBtn.setOnClickListener(cancelListener.second)
                else tvCancelBtn.setOnClickListener { alert!!.dismiss() }
            }

            tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            etInputText.addTextChangedListener {
                if (it.toString().length > 1)
                {
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                }
            }

            tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
            etInputText.addTextChangedListener {
                if (it.toString().isNotEmpty())
                {
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                    vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_MAIN_700))
                    callback.apply(it.toString())
                }
                else
                {
                    tvOkBtn.setTextColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                    vFocusStatus.setBackgroundColor(context.resources.getColor(R.color.COLOR_GRAY_200))
                }
            }
        }

        alert!!.show()
    }

    fun onDismiss()
    {
        if (alert != null)
        {
            alert?.dismiss()
            alert = null
        }
    }

    // refreshToken이 비정상으로 만료되었을 때 호출되는 AlertMessage 모든 내부 DB 내용 초기화 및 앱 종
    suspend fun alertExpiredToken(activity: FragmentActivity, message: String) = withContext(Dispatchers.Default) {
        GeneralDialog(act = activity, title = "종료", msg = message, detailMsg = null, rHandler = Pair("확인", object: OnAgreeClickListener
        {
            override fun invoke(agree: GeneralDialog)
            {
                userSITORY.removeUserRepo()

                CoroutineScope(Dispatchers.Default).launch { appDataBase.clearAllTables() }

                Intent(activity, LoginSelectView::class.java).let {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    activity.startActivity(it)
                    activity.finish()
                }
            }
        })).show(activity.supportFragmentManager, "FINISH")
    }
}