package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.Intent;
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

    public void onTaskUpdated(Task task) {
        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        overviewFragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        try {
            UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_TASK_ID);
            boolean isAlarmOn = (boolean) getIntent().getSerializableExtra(EXTRA_ALARM);
            return OverviewFragment.newInstance(id, isAlarmOn);
        } catch (NullPointerException e) {
            return OverviewFragment.newInstance(null, false);
        }


    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    @Override
    public void onTaskSelected(Task task) {
        if (findViewById(R.id.detail_fragment_container) == null) {
            Intent intent = TaskActivity.newIntent(this, task.getID());
            startActivity(intent);
        } else {
            TaskFragment tf = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);
            String name = "no fragment";
            if (tf != null) {
                name = tf.getTask().getName();
            }
            if (name != null && !name.isEmpty()) {
                Fragment newDetail = TaskFragment.newInstance(task.getID());
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.detail_fragment_container,newDetail).commit();
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(), "Please Name Task",
                        Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    @Override
    public void onTaskIdSelected(UUID taskId) {
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
            if (viewTask.getID().equals(taskId)) {
                overviewFragment.getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(taskFragment).commit();
            }
        }
    }

    public static Intent newIntent(Context packageContext, UUID id, boolean isAlarmOn) {
        Intent intent = new Intent(packageContext, OverviewActivity.class);
        intent.putExtra(EXTRA_TASK_ID, id);
        intent.putExtra(EXTRA_ALARM, isAlarmOn);
        return intent;
    }

    public Task getTask() {
        if (findViewById(R.id.detail_fragment_container) == null) {
            return null;
        } else {
            TaskFragment tf = (TaskFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);
            return tf==null? null: tf.getTask();
        }
    }
}
