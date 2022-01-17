package com.delivery.sopo.views.inquiry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentOngoingTypeBinding
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class OngoingTypeFragment: BaseFragment<FragmentOngoingTypeBinding, InquiryViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_ongoing_type
    override val vm: InquiryViewModel by viewModel()
    override val mainLayout: View by lazy { binding.swipeLayoutMainOngoing }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()
    }

    override fun setObserve()
    {
        super.setObserve()
    }

}