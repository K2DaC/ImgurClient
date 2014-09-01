package net.podkowik.curseradapterwithservice;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import net.podkowik.curseradapterwithservice.fragments.ImgurPostsContentFragment;


public class HomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, ImgurPostsContentFragment.getInstance())
                    .commit();
        }
    }
}
