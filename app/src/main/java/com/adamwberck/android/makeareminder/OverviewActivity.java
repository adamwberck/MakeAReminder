package com.adamwberck.android.makeareminder;

import android.support.v4.app.Fragment;

import com.adamwberck.android.makeareminder.Elements.Group;

public class OverviewActivity extends SingleFragmentActivity implements OverviewFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return OverviewFragment.newInstance();
    }


    @Override
    public void onGroupSelected(Group group) {
        //TODO show tasklist
    }
}
