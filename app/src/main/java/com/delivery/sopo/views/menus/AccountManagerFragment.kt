package com.delivery.sopo.views.menus

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import kotlinx.android.synthetic.main.fragment_account_manager.*


class AccountManagerFragment: Fragment()
{
    lateinit var binding: FragmentAccountManagerBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {

        val view = inflater.inflate(R.layout.fragment_account_manager, container, false)

        layout_sign_out.setOnClickListener {

        }

        return view
    }

    companion object
    {

    }
}