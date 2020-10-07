package com.delivery.sopo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.views.menus.MenuFragment
import kotlinx.android.synthetic.main.fragment_base.*

class MenuView : AppCompatActivity()
{
    private var appBarTitle: String? = null
    private lateinit var menuView: MenuView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        menuView = this

        loadData()
        setView()
        gotoView()
    }

    private fun gotoView() {
            move(menuView, MenuFragment(), 0)
    }

    private fun loadData() {
        appBarTitle = "방해금지 시간대 설정"
    }

    private fun setView() {
        title_tv.text = appBarTitle
    }

    private fun move(activity: AppCompatActivity, fragment: Fragment, animation: Int) {
        val transaction = activity.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_menu, fragment).commitAllowingStateLoss()
    }

}