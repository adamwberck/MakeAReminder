package com.adamwberck.android.makeareminder.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;


public class TaskActivity extends SingleFragmentActivity {


    private static final String EXTRA_TASK = "com.adamwberck.android.makeareminder.task";
    private static final String EXTRA_IS_NEW = "com.adamwberck.android.makeareminder.is_new";
    private static final String TAG = "TaskActivity";
    private boolean mIsNew;
    private Task mOldTask;
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, Task task,boolean isNew) {
        Intent intent = new Intent(packageContext,TaskActivity.class);
        intent.putExtra(EXTRA_IS_NEW,isNew);
        intent.putExtra(EXTRA_TASK,task);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        mOldTask = (Task) getIntent().getExtras().getSerializable(EXTRA_TASK);
        mIsNew = getIntent().getExtras().getBoolean(EXTRA_IS_NEW);
        return TaskFragment.newInstance(mOldTask);
    }


    @Override
    public void onBackPressed(){
        final Task task = ((TaskFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container)).getTask();
        String name = task.getName();
        Log.i(TAG,"TaskA2 "+task.toString());
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setNegativeButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!mIsNew) {
                            task.getGroup().addTask(mOldTask);
                        }
                        task.getGroup().removeTask(task);
                        TaskActivity.this.finish();
                    }
                })
                .setCancelable(true);

        if(!name.isEmpty()) {
            b.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GroupLab.saveLab();
                    TaskActivity.this.finish();
                }
            });
        }
        else {
            b.setPositiveButton(R.string.cancel, null);
        }
        b.show();
    }
    @Override
    public void finish(){
        super.finish();
    }

}
