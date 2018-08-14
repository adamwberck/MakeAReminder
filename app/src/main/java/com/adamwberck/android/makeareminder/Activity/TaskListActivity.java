package com.adamwberck.android.makeareminder.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.Fragment.TaskListFragment;
import com.adamwberck.android.makeareminder.R;

public class TaskListActivity extends SingleFragmentActivity implements TaskListFragment.Callbacks,
        TaskFragment.Callbacks, TaskListFragment.OnDeleteTaskListener,
        TaskFragment.OnDeleteTaskListener {

    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //TaskLab.get(this).removeUnnamed();
    }

    public void onTaskUpdated(Task task) {
        TaskListFragment taskListFragment = (TaskListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        taskListFragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        return TaskListFragment.newInstance();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    @Override
    public void onTaskSelected(Task task) {
        if (isSingleFragment()) {
            Intent intent = TaskActivity.newIntent(this, task);
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

    @Override
    public void onTaskIdSelected(int taskId) {
        TaskFragment taskFragment = (TaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        TaskListFragment taskListFragment = (TaskListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (taskListFragment != null) {
            taskListFragment.deleteTask(taskId);
            taskListFragment.updateUI();
        }
        if (taskFragment != null) {
            Task viewTask = taskFragment.getTask();
            if (viewTask.getID()==taskId) {
                taskListFragment.getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(taskFragment).commit();
            }
        }
    }

    public static Intent newIntent(Context packageContext, int id) {
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        return intent;
    }
    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, TaskListActivity.class);
        return intent;
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
}
