package com.adamwberck.android.makeareminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;

import com.adamwberck.android.makeareminder.Elements.Task;

public class AlarmAlertFragment extends DialogFragment {
    private static final String ARG_TASK = "task";
    private static final String ARG_NAME = "name";
    public static AlarmAlertFragment newInstance(Task task,String name){
        Bundle args = new Bundle();
        AlarmAlertFragment fragment = new AlarmAlertFragment();
        args.putSerializable(ARG_TASK,task);
        args.putString(ARG_NAME,name);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setStyle(R.style.AppTheme,DialogFragment.STYLE_NORMAL);
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alarm,null);
        String name = getArguments().getString(ARG_NAME);
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(name)
                .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                })
                .setNegativeButton("Complete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                })
                .setNeutralButton("Skip",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                })
                .create();
    }

    private void cancel() {
        this.getDialog().cancel();

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
    }
}

