package com.adamwberck.android.makeareminder.Activity;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Fragment.OverviewFragment;

import java.util.UUID;

public class OverviewActivity extends SingleFragmentActivity implements OverviewFragment.Callbacks{

    @Override
    protected Fragment createFragment() {
        return OverviewFragment.newInstance();
    }


    @Override
    public void onGroupSelected(UUID uuid) {
        Intent intent = TaskListActivity.newIntent(this,uuid);
        startActivity(intent);
    }

    @Override
    public void onGroupEdit(UUID uuid) {

        Intent intent = GroupActivity.newIntent(this,uuid);
        startActivity(intent);
    }

    @Override
    public void onTaskAdded(Group group){
        //TODO set default task elements
        Task task = new Task(this,group);
        group.addTask(task);
        Intent intent = TaskActivity.newIntent(this, task.getID());
        startActivity(intent);
        task.test();
    }
}
