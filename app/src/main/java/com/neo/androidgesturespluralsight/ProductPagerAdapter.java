package com.neo.androidgesturespluralsight;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by User on 3/3/2018.
 */


/**
 * Adapter for managing list of fragments in the ViewPager
 */
public class ProductPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Fragment> mFragments = new ArrayList<>();

    public ProductPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);       // gets fragment at specified pos
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}













