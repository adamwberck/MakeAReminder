package com.adamwberck.android.makeareminder.Activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Fragment.GroupFragment;
import com.adamwberck.android.makeareminder.Fragment.TaskFragment;
import com.adamwberck.android.makeareminder.R;

import java.util.UUID;

public class GroupActivity extends SingleFragmentActivity {


    private static final String EXTRA_GROUP = "com.adamwberck.android.makeareminder.group";
    //private static final String EXTRA_ALARM = "com.adamwberck.android.makeareminder.alarm";

    public static Intent newIntent(Context packageContext, UUID uuid) {
        Intent intent = new Intent(packageContext,GroupActivity.class);
        intent.putExtra(EXTRA_GROUP,uuid);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID id =  (UUID) getIntent().getExtras().getSerializable(EXTRA_GROUP);
        return GroupFragment.newInstance(id);
    }


    @Override
    public void onBackPressed(){
        String name = ((GroupFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container)).getGroup().getName();
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
