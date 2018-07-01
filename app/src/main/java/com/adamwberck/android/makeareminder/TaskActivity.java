package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.UUID;

public class TaskActivity extends SingleFragmentActivity{


    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, int taskID) {
        Intent intent = new Intent(packageContext,TaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID,taskID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        int taskID = getIntent().getExtras().getInt(EXTRA_TASK_ID);
        return TaskFragment.newInstance(taskID);
    }


    @Override
    public void onBackPressed(){
        String name = ((TaskFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container)).getTask().getName();
        if(name.isEmpty()){
            Toast toast = Toast.makeText(getApplicationContext(), R.string.name_task_warning,
                    Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            super.onBackPressed();
        }
    }

}
