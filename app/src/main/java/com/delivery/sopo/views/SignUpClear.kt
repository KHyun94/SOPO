package com.delivery.sopo.views

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delivery.sopo.R
import kotlinx.android.synthetic.main.sign_up_clear.*

class SignUpClear : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up_clear)

        btn_move_login.setOnClickListener {
            startActivity(Intent(this, LoginView::class.java))
        }
    }
}