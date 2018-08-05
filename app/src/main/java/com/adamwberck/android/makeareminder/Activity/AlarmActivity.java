package com.adamwberck.android.makeareminder.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.adamwberck.android.makeareminder.Fragment.AlarmFullFragment;

public class AlarmActivity extends SingleFragmentActivity {
    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";
    private static final String EXTRA_TITLE = "com.adamwberck.android.makeareminder.title";

    @Override
    protected Fragment createFragment() {
        int id = getIntent().getExtras().getInt(EXTRA_TASK_ID);
        boolean isAlarmOn = getIntent().getExtras().getBoolean(EXTRA_ALARM);
        String name = getIntent().getExtras().getString(EXTRA_NAME);
        String title = getIntent().getExtras().getString(EXTRA_TITLE);
        return AlarmFullFragment.newInstance(id,isAlarmOn,name,title);
    }

    public static Intent newIntent(Context packageContext, int id, boolean isAlarmOn,
                                   String name, String title) {
        Intent intent = new Intent(packageContext, AlarmActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        intent.putExtra(EXTRA_ALARM, isAlarmOn);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }
}
