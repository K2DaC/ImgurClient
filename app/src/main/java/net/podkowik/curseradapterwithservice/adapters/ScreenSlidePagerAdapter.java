package net.podkowik.curseradapterwithservice.adapters;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import net.podkowik.curseradapterwithservice.fragments.ImageViewerFragment;

/**
 * Created by christoph.podkowik on 29/08/14.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

    private int mSize;
    public ScreenSlidePagerAdapter(int size, FragmentManager fm) {
        super(fm);
        mSize = size;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageViewerFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return mSize;
    }


}
