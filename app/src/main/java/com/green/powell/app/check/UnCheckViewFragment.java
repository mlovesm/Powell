package com.green.powell.app.check;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.green.powell.app.R;
import com.green.powell.app.equipment.EquipmentTab1Fragment;
import com.green.powell.app.util.UtilClass;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UnCheckViewFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    public static int int_items = 2 ;

    private String idx="";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_layout,null);
        ButterKnife.bind(this, view);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        /**
         *Set an Apater for the View Pager
         */
        viewPager.setAdapter(new MyAdapter(getChildFragmentManager()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                UtilClass.logD(TAG, tab+"");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        idx= getArguments().getString("equip_no");

        return view;
    }//onCreateView

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position)
        {
            switch (position){
                case 0 : return new UnCheckTab1Fragment(idx);
                case 1 : return new EquipmentTab1Fragment(idx);
            }
            return null;
        }

        @Override
        public int getCount() {

            return int_items;

        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position){
                case 0 :
                    return "기본정보";
                case 1 :
                    return "장치정보";
                case 2 :
                    return "위치정보";
            }
            return null;
        }
    }

}
