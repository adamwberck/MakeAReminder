package com.adamwberck.android.makeareminder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.adamwberck.android.makeareminder.TaskFragment.hideSoftKeyboard;

public class AlarmAlertFragment extends DialogFragment {
    private static final String ARG_TASK = "task";
    private static final String ARG_NAME = "name";
    private static final String TAG = "AlarmAlert";
    private int mSnoozeSelected = -1;
    private List<String> mSnoozeList = new ArrayList<>(12);
    private int mButtonNumber = 0;

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

    public void setupButtons(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof Button) {
            Button b = (Button) view;
            b.setId(mButtonNumber);
            String text = mSnoozeList.get(mButtonNumber++);
            b.setText(text);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick(v);
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupButtons(innerView);
            }
        }
    }

    private void buttonClick(View v) {
        int id = v.getId();
        Log.i(TAG,id+"button pressed.");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alarm,null);
        initSnoozeButtons();

        //TODO switch to page viewer
        setupButtons(v.findViewById(R.id.snooze_buttons));
        String name = getArguments().getString(ARG_NAME);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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
                });
        return builder.create();
    }

    private void initSnoozeButtons() {
        int num = 1;
        String letter,sNum,label;
        for(int i=1;i<=6;i++){
            letter = getString(R.string.minute_letter);
            sNum = num+"";
            label = getString(R.string.snooze_text,sNum,letter);
            num = num==1?5:num*2;
            mSnoozeList.add(label);
        }
        for(int i=2;i<=8;i*=2){
            letter = getString(R.string.hour_letter);
            sNum = i+"";
            label = getString(R.string.snooze_text,sNum,letter);
            mSnoozeList.add(label);
        }
        String[] letters = {getString(R.string.day_letter),getString(R.string.week_letter)};
        for(String l : letters) {
            sNum = 1 + "";
            label = getString(R.string.snooze_text, sNum, l);
            mSnoozeList.add(label);
        }
        letter = letters[0];
        sNum = 30+"";
        label = getString(R.string.snooze_text,sNum,letter);
        mSnoozeList.add(label);
    }

    private void cancel() {
        this.getDialog().cancel();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
    }

}

