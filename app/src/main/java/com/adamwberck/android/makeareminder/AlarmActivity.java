package com.adamwberck.android.makeareminder;

import android.support.v4.app.Fragment;

public class AlarmActivity extends SingleFragmentActivity {
    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";

    @Override
    protected Fragment createFragment() {
        int id = getIntent().getExtras().getInt(EXTRA_TASK_ID);
        boolean isAlarmOn = getIntent().getExtras().getBoolean(EXTRA_ALARM);
        String name = getIntent().getExtras().getString(EXTRA_NAME);
        return OverviewFragment.newInstance(id,isAlarmOn,name);
    }
}
