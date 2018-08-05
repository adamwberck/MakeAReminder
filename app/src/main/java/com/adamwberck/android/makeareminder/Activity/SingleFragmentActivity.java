package com.adamwberck.android.makeareminder.Activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.adamwberck.android.makeareminder.R;

import net.danlew.android.joda.JodaTimeAndroid;

/* Created by Adam on 8/18/2017.*/

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private static final String TAG = "Fragment";

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment== null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onDestroy(){
        Log.i(TAG,this.toString() + " destroyed");
        super.onDestroy();
    }
}
