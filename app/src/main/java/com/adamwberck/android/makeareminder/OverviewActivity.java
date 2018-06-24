package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.UUID;

public class OverviewActivity extends SingleFragmentActivity implements OverviewFragment.Callbacks,
        TaskFragment.Callbacks, OverviewFragment.OnDeleteTaskListener,
        TaskFragment.OnDeleteTaskListener {

    private static final String EXTRA_TASK_ID = "com.adamwberck.android.makeareminder.task_id";
    private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";
    private static final String EXTRA_NAME = "com.adamwberck.android.makeareminder.name";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        TaskLab.get(this).removeUnnamed();
    }

    public void onTaskUpdated(Task task) {
        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        overviewFragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        try {
            int id = getIntent().getExtras().getInt(EXTRA_TASK_ID);
            boolean isAlarmOn = getIntent().getExtras().getBoolean(EXTRA_ALARM);
            String name = getIntent().getExtras().getString(EXTRA_NAME);
            return OverviewFragment.newInstance(id,isAlarmOn,name);
        } catch (NullPointerException e) {
            return OverviewFragment.newInstance( false);
        }


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    @Override
    public void onTaskSelected(Task task) {
        if (isSingleFragment()) {
            Intent intent = TaskActivity.newIntent(this, task.getID());
            startActivity(intent);
        } else {
            TaskFragment tf = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);

            if (tf != null) {
                String name = tf.getTask().getName();
                if (!name.isEmpty()) {
                    Fragment newDetail = TaskFragment.newInstance(task.getID());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.detail_fragment_container, newDetail).commit();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getString(R.string.name_task_warning),Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else {
                Fragment newDetail = TaskFragment.newInstance(task.getID());
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
        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (overviewFragment != null) {
            overviewFragment.deleteTask(taskId);
            overviewFragment.updateUI();
        }
        if (taskFragment != null) {
            Task viewTask = taskFragment.getTask();
            if (viewTask.getID()==taskId) {
                overviewFragment.getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(taskFragment).commit();
            }
        }
    }

    public static Intent newIntent(Context packageContext, int id, boolean isAlarmOn,
                                   String name) {
        Intent intent = new Intent(packageContext, OverviewActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        intent.putExtra(EXTRA_ALARM, isAlarmOn);
        intent.putExtra(EXTRA_NAME, name);
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
        if(!isSingleFragment()){

        }
        super.onBackPressed();
    }
}
