package com.delivery.sopo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.delivery.sopo.R
import com.delivery.sopo.views.inquiry.ParcelDetailView

class TestActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val fm = supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.run {
            replace(R.id.layout_test_fragment, ParcelDetailView(), "Tag")
            addToBackStack(null)
            commit()
        }
    }
}