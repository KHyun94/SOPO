package com.delivery.sopo.presentation.views.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.delivery.sopo.R;
import com.delivery.sopo.interfaces.listener.OnIntroClickListener;

public class IntroPageAdapter extends PagerAdapter {

    // LayoutInflater 서비스 사용을 위한 Context 참조 저장.
    private Context mContext = null ;
    private OnIntroClickListener introClickListener;

    public IntroPageAdapter()
    {
    }

    // Context를 전달받아 mContext에 저장하는 생성자 추가.
    public IntroPageAdapter(Context context, OnIntroClickListener listener) {
        mContext = context;
        introClickListener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = null ;

        if (mContext != null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (position)
            {
                case 0:
                    view = inflater.inflate(R.layout.intro_view_1, container, false);
                    break;
                case 1:
                    view = inflater.inflate(R.layout.intro_view_2, container, false);
                    break;
                case 2:
                    view = inflater.inflate(R.layout.intro_view_3, container, false);

                    TextView tvSettingLater = view.findViewById(R.id.tv_setting_later);
                    TextView tvSettingNow = view.findViewById(R.id.tv_setting_now);

                    tvSettingLater.setOnClickListener((View.OnClickListener) v -> introClickListener.onIntroSettingClicked(false));
                    tvSettingNow.setOnClickListener((View.OnClickListener) v -> introClickListener.onIntroSettingClicked(true));

                    break;
            }
        }

        // 뷰페이저에 추가.
        container.addView(view) ;

        return view ;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // 뷰페이저에서 삭제.
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return (view == (View)object);
    }
}