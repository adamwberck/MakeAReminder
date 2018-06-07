package com.adamwberck.android.makeareminder;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.UUID;

public class OverviewActivity extends SingleFragmentActivity implements OverviewFragment.Callbacks,
        TaskFragment.Callbacks, OverviewFragment.OnDeleteTaskListener,
        TaskFragment.OnDeleteTaskListener{

    public void onTaskUpdated(Task task){
        OverviewFragment overviewFragment = (OverviewFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        overviewFragment.updateUI();
    }

    @Override
    protected Fragment createFragment() {
        return new OverviewFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_masterdetail;
    }


    @Override
    public void onTaskSelected(Task task){
        if(findViewById(R.id.detail_fragment_container)==null){
            Intent intent = TaskActivity.newIntent(this, task.getID());
            startActivity(intent);
        }else {
            Fragment newDetail = TaskFragment.newInstance(task.getID());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container,newDetail).commit();
        }
    }

    @Override
    public void onTaskIdSelected(UUID taskId) {
        TaskFragment taskFragment = (TaskFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        OverviewFragment overviewFragment = (OverviewFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if(overviewFragment!=null) {
            overviewFragment.deleteTask(taskId);
            overviewFragment.updateUI();
        }
        if(taskFragment !=null){
            Task viewTask = taskFragment.getTask();
            if(viewTask.getID().equals(taskId)) {
                overviewFragment.getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(taskFragment).commit();
            }
        }
    }
}
