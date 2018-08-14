package com.adamwberck.android.makeareminder.Activity;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Fragment.OverviewFragment;

import java.util.UUID;

public class OverviewActivity extends SingleFragmentActivity implements OverviewFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return OverviewFragment.newInstance();
    }


    @Override
    public void onGroupSelected(UUID uuid) {
        //TODO show tasklist
    }

    @Override
    public void onGroupEdit(UUID uuid) {
        Intent intent = GroupActivity.newIntent(this,uuid);
        startActivity(intent);
    }
}
