package net.podkowik.curseradapterwithservice;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import net.podkowik.curseradapterwithservice.adapters.ScreenSlidePagerAdapter;
import net.podkowik.curseradapterwithservice.fragments.ImgurPostsContentFragment;


public class ImageViewerActivity extends FragmentActivity {

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        int currentView = getIntent().getIntExtra(ImgurPostsContentFragment.POSITION, 0);
        int size = getIntent().getIntExtra(ImgurPostsContentFragment.SIZE, 0);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(size, getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(currentView);
    }
}
