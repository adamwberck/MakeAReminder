package com.adamwberck.android.makeareminder.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class ChooseSnoozeDialog extends DismissDialog {
    private static final DateTime START_TIME = new DateTime();
    private static final String ARG_TASK = "task";
    private static final String ARG_NAME = "name";
    private static final String TAG = "AlarmAlert";
    public static final String EXTRA_INTERVAL =
            "com.adamwberck.android.makeareminder.extrainterval";
    private boolean mNewAlarm = true;
    private Stack<DateTime> mUndoStack = new Stack<>();
    private TextView mSnoozeText;
    private DateTime mSnoozeTime = new DateTime(START_TIME);
    private int mSnoozeSelected = -1;
    private List<String> mSnoozeList = new ArrayList<>(12);
    private int mButtonNumber = 0;
    private TextView mSnoozeText2;

    public static ChooseSnoozeDialog newInstance(Task task, String name){
        Bundle args = new Bundle();
        ChooseSnoozeDialog fragment = new ChooseSnoozeDialog();
        args.putSerializable(ARG_TASK,task);
        args.putString(ARG_NAME,name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume(){
        super.onResume();
        int height = 600;
        int width = 410;
        getDialog().getWindow().setLayout(width, height);
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
                    updateSnoozeText();
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

    private void updateSnoozeText() {
        Resources r = getResources();
        int color = mNewAlarm ? r.getColor(R.color.blue):r.getColor(R.color.black);
        mSnoozeText.setTextColor(color);
        mSnoozeText2.setTextColor(color);
        Interval interval = new Interval(START_TIME,mSnoozeTime);
        PeriodFormatter pfb  = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSeparator(":")
                .appendMinutes()
                .toFormatter();
        mSnoozeText.setText(pfb.print(interval.toPeriod()));
        if(interval.toDuration().isLongerThan(new Duration(TimeUnit.HOURS.toMillis(24)))){
            long millis = interval.toDurationMillis();
            long day = TimeUnit.MILLISECONDS.toDays(millis);
            long weeks = day/7;
            day%=7;
            String snoozeDay = getResources().getQuantityString(R.plurals.day,
                    (int) day,(int)day);
            String snoozeWeek = getResources().getQuantityString(R.plurals.weeks,
                    (int) weeks,(int)weeks);
            mSnoozeText2.setVisibility(View.VISIBLE);
            if(day!=0&&weeks!=0){
                mSnoozeText2.setText(getString(R.string.snooze_text_2,snoozeWeek,
                    snoozeDay));
            }
            else {
                String text = weeks==0?snoozeDay:snoozeWeek;
                mSnoozeText2.setText(text);
            }

        }
        else {
            mSnoozeText2.setText("");
            mSnoozeText2.setVisibility(View.GONE);
        }
    }

    private void buttonClick(View v) {
        Button b = (Button) v;
        String s = (String) b.getText();
        Log.i(TAG,s+" button pressed.");
        char c =  s.charAt(s.length()-1);
        int time = Integer.parseInt(s.substring(0,s.length()-1));
        mUndoStack.push(mSnoozeTime);
        if(mNewAlarm){
            mSnoozeTime = START_TIME;
            mNewAlarm = false;
        }
        switch (c){
            case 'm':
                mSnoozeTime = mSnoozeTime.plusMinutes(time);
                break;
            case 'h':
                mSnoozeTime = mSnoozeTime.plusHours(time);
                break;
            case 'd':
                mSnoozeTime = mSnoozeTime.plusDays(time);
                break;
            case 'w':
                mSnoozeTime = mSnoozeTime.plusWeeks(time);
                break;
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_alarm,null);

        Task task = (Task) getArguments().getSerializable(ARG_TASK);
        if(task!=null) {
            DateTime snoozeTime = task.getSnoozeTime();
            if (snoozeTime != null && snoozeTime.isAfterNow()) {
                Interval interval = new Interval(DateTime.now(), snoozeTime);
                mSnoozeTime = START_TIME.plus(interval.toDurationMillis());
            }
        }
        ImageButton resetButton = v.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mSnoozeTime.equals(START_TIME)) {
                    mUndoStack.push(mSnoozeTime);
                    mSnoozeTime = START_TIME;
                    updateSnoozeText();
                }
            }
        });

        ImageButton undoButton = v.findViewById(R.id.undo_button);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUndoStack.size()>0) {
                    mSnoozeTime = mUndoStack.pop();
                }
                if(mUndoStack.size()==0){
                    mNewAlarm = true;
                }
                updateSnoozeText();
            }
        });

        mSnoozeText = v.findViewById(R.id.snooze_time_text);
        mSnoozeText2 = v.findViewById(R.id.snooze_time_text_2);
        updateSnoozeText();
        mSnoozeList = Arrays.asList(getResources().getStringArray(R.array.snooze_labels));
        final HorizontalScrollView horizontalScrollView =  v.findViewById(R.id.snooze_scrollview);
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                int scrollX = view.getScrollX();
                int scrollY = view.getScrollY();
                Log.i("Scrolling", "X is ["+scrollX+"] Y is ["+scrollY+"]");
                int max =273;// horizontalScrollView.get()/2;
                int half = max/2;
                if(event.getAction()==MotionEvent.ACTION_UP){
                    int goal = scrollX<half?0:max;
                    horizontalScrollView.smoothScrollTo(goal,scrollY);
                    //horizontalScrollView.invalidate();
                    return true;
                }
                return false;
            }

        });
        setupButtons(v.findViewById(R.id.color_buttons));
        String name = getArguments().getString(ARG_NAME);

        //End Buttons
        Button snoozeButton = v.findViewById(R.id.alarm_snooze_button);
        snoozeButton.setText(R.string.clear_snooze);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK,new Interval(START_TIME,mSnoozeTime)
                        .toDurationMillis());
            }
        });
        Button dismissButton = v.findViewById(R.id.alarm_dismiss_button);
        dismissButton.setText(R.string.set_snooze);
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK,0);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(name).create();
    }

    private void sendResult(int resultCode, long interval ){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_INTERVAL, interval);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
        getDialog().dismiss();
    }
    private void cancel() {
        this.getDialog().cancel();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
    }
}

