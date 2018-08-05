package com.adamwberck.android.makeareminder.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.R;

public class TaskActivity extends SingleFragmentActivity {


    private static final String EXTRA_TASK = "com.adamwberck.android.makeareminder.task";
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, Task task) {
        Intent intent = new Intent(packageContext,TaskActivity.class);
        intent.putExtra(EXTRA_TASK,task);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        Task task = (Task) getIntent().getExtras().getSerializable(EXTRA_TASK);
        return TaskFragment.newInstance(task);
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