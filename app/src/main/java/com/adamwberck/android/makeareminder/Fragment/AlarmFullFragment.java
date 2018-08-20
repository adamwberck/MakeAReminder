package com.adamwberck.android.makeareminder.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamwberck.android.makeareminder.Dialog.AlarmAlertDialog;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;
import com.adamwberck.android.makeareminder.Service.ReminderService;

import org.joda.time.DateTime;

public class AlarmFullFragment extends VisibleFragment {
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_ALARM = "alarm";
    private static final String ARG_NAME = "name";
    private static final String ARG_TITLE = "title";

    private static final int REQUEST_SNOOZE = 0;
    private static final String DIALOG_ALARM = "Dialog_ALARM";
    private int mTaskID;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alarm_full, container,false);
        super.setupUI(v);
        return v;
    }

    public static Fragment newInstance(int id, boolean isAlarmOn, String name,String title) {
        Bundle args =  new Bundle();
        args.putInt(ARG_TASK_ID, id);
        args.putBoolean(ARG_ALARM,isAlarmOn);
        args.putString(ARG_NAME,name);
        args.putString(ARG_TITLE,title);

        AlarmFullFragment fragment = new AlarmFullFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        boolean isAlarmOn = getArguments().getBoolean(ARG_ALARM);
        mTaskID = getArguments().getInt(ARG_TASK_ID);
        Task task = GroupLab.get(getActivity()).getTask(mTaskID);
        if (isAlarmOn) {
            startAlarmDialog(task);
        }
    }

    private void startAlarmDialog(Task task) {
        String name = getArguments().getString(ARG_NAME);
        String title = getArguments().getString(ARG_TITLE);
        FragmentManager manager = getFragmentManager();
        AlarmAlertDialog dialog = AlarmAlertDialog.newInstance(task,title);
        dialog.setTargetFragment(AlarmFullFragment.this, REQUEST_SNOOZE);
        dialog.show(manager, DIALOG_ALARM);
        ReminderService.setServiceAlarm(getActivity(),task.getID(),name,
                true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode!= Activity.RESULT_OK) {
            getActivity().finish();
            return;
        }

        if(requestCode==REQUEST_SNOOZE){
            DateTime snoozeTime = (DateTime) data.getExtras()
                    .getSerializable(AlarmAlertDialog.EXTRA_INTERVAL);
            Task task = GroupLab.get(getContext()).getTask(mTaskID);

            if (snoozeTime!=null && snoozeTime.isAfterNow()) {
                task.setSnoozeTime(snoozeTime);
            }
            else {
                task.setSnoozeTime(null);
                if(snoozeTime==null){
                    task.setComplete(true);
                }
            }
            ReminderService.setServiceAlarm(getActivity().getApplicationContext(), mTaskID
                            , task.getName(), true);

            getActivity().finish();
        }

    }
}
