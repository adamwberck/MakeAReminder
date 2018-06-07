package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

public class TaskActivity extends SingleFragmentActivity{


    private static final String EXTRA_REMINDER_ID =
            "com.adamwberck.android.makeareminder.reminder_id";

    public static Intent newIntent(Context pakageContext, UUID reminderID) {
        Intent intent = new Intent(pakageContext,TaskActivity.class);
        intent.putExtra(EXTRA_REMINDER_ID,reminderID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID reminderID = (UUID) getIntent()
                .getSerializableExtra(EXTRA_REMINDER_ID);
        return TaskFragment.newInstance(reminderID);
    }
}
