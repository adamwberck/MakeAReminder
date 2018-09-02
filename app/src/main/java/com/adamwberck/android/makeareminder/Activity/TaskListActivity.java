package com.adamwberck.android.makeareminder.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.Fragment.TaskListFragment;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;

import java.util.UUID;

public class TaskListActivity extends SingleFragmentActivity implements TaskListFragment.Callbacks,
        TaskFragment.Callbacks, TaskListFragment.OnDeleteTaskListener {

    private static final String EXTRA_TASK = "com.adamwberck.android.makeareminder.task";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";
    private static final String EXTRA_GROUP_ID = "com.adamwberck.android.makeareminder.group_id";

    public static Intent newIntent(Context packageContext, Task task){
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        return intent;
    }


    public static Intent newIntent(Context packageContext, UUID uuid){
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, uuid);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //TaskLab.get(this).removeUnnamed();
    }

    public void onTaskUpdated(int taskID) {
        TaskListFragment taskListFragment = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        taskListFragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        UUID id = (UUID) getIntent().getExtras().getSerializable(EXTRA_GROUP_ID);
        return TaskListFragment.newInstance(id);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    @Override
    public void onTaskSelected(Task task,boolean isNew) {
        if (isSingleFragment()) {
            Intent intent = TaskActivity.newIntent(this, task,isNew);
            startActivity(intent);
        }
        else {
            TaskFragment tf = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);

            if (tf != null) {
                String name = tf.getTask().getName();
                if (!name.isEmpty()) {
                    Fragment newDetail = TaskFragment.newInstance(task);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_fragment_container, newDetail).commit();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.name_task_warning),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else {
                Fragment newDetail = TaskFragment.newInstance(task);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container, newDetail).commit();
            }
        }
    }

    private boolean isSingleFragment() {
        return findViewById(R.id.detail_fragment_container) == null;
    }





    public Task getTask() {
        if (isSingleFragment()) {
            return null;
        } else {
            TaskFragment tf = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);
            return tf==null? null: tf.getTask();
        }
    }

    @Override
    public void onBackPressed(){
        //TODO handle back pressing with dual view
        if(!isSingleFragment()){
        }
        super.onBackPressed();
    }

    @Override
    public void onTaskIdDeleted(Task task) {
        Group group = task.getGroup();
        if(group!=null){
            group.removeTask(task);
        }
    }
}
