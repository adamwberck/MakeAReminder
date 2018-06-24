package com.adamwberck.android.makeareminder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        Task task = (Task) getArguments().getSerializable(ARG_TASK);
        String name = getArguments().getString(ARG_NAME);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(name)
                .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                })
                .setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                });
        return builder.create();
    }

    private void cancel() {
        this.getDialog().cancel();
    }
}

