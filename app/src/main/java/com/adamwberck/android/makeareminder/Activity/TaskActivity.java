package com.adamwberck.android.makeareminder.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;

public class TaskActivity extends SingleFragmentActivity {


    private static final String EXTRA_TASK = "com.adamwberck.android.makeareminder.task";
    private Task mOldTask;
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, int taskID) {
        Intent intent = new Intent(packageContext,TaskActivity.class);
        intent.putExtra(EXTRA_TASK,taskID);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        int id = getIntent().getExtras().getInt(EXTRA_TASK);
        try {
            mOldTask = GroupLab.get(getBaseContext()).getTask(id).clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }

        if(mOldTask.getName().isEmpty()){
            mOldTask=null;
        }
        return TaskFragment.newInstance(id);
    }


    @Override
    public void onBackPressed(){
        //Todo check changed
        final Task task = ((TaskFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container)).getTask();
        if(task.equals(mOldTask)){
            super.onBackPressed();
            return;
        }
        String name = task.getName();
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle("Discard Changes?")
                .setNegativeButton(R.string.discard_changes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Todo discard
                        task.getGroup().removeTask(task);
                        if(mOldTask!=null){
                            task.getGroup().addTask(mOldTask);
                        }
                        TaskActivity.this.finish();
                    }
                })
                .setCancelable(true);
        if(name.isEmpty()){
            b.setPositiveButton(R.string.cancel, null);
        }
        else {
            b.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    GroupLab.saveLab();
                    TaskActivity.this.finish();
                }
            });
        }
        b.show();
    }

}
