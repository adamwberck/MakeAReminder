package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class TaskActivity extends SingleFragmentActivity{


    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, UUID reminderID) {
        Intent intent = new Intent(packageContext,TaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID,reminderID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID reminderID = (UUID) getIntent().getSerializableExtra(EXTRA_TASK_ID);
        return TaskFragment.newInstance(reminderID);
    }


}
