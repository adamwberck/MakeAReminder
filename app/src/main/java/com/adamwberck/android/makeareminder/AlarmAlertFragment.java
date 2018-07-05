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
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static com.adamwberck.android.makeareminder.TaskFragment.hideSoftKeyboard;

public class AlarmAlertFragment extends DialogFragment {
    private static final DateTime START_TIME = new DateTime();
    private static final String ARG_TASK = "task";
    private static final String ARG_NAME = "name";
    private static final String TAG = "AlarmAlert";
    private Stack<DateTime> mUndoStack = new Stack<>();
    private TextView mSnoozeText;
    private DateTime mSnoozeTime = new DateTime(START_TIME);
    private int mSnoozeSelected = -1;
    private List<String> mSnoozeList = new ArrayList<>(12);
    private int mButtonNumber = 0;
    private TextView mSnoozeText2;

    public static AlarmAlertFragment newInstance(Task task,String name){
        Bundle args = new Bundle();
        AlarmAlertFragment fragment = new AlarmAlertFragment();
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
            if(day!=0&&weeks!=0){
            mSnoozeText2.setText(getString(R.string.snooze_text_2,snoozeWeek,
                    snoozeDay));
            }else {
                String text = weeks==0?snoozeDay:snoozeWeek;
                mSnoozeText2.setText(text);
            }

        }
        else {
            mSnoozeText2.setText("");
        }
    }

    private void buttonClick(View v) {
        Button b = (Button) v;
        String s = (String) b.getText();
        Log.i(TAG,s+" button pressed.");
        char c =  s.charAt(s.length()-1);
        int time = Integer.parseInt(s.substring(0,s.length()-1));
        mUndoStack.push(mSnoozeTime);
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

        ImageButton resetButton = v.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnoozeTime = new DateTime(START_TIME);
                updateSnoozeText();
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
                    mSnoozeTime = new DateTime(START_TIME);
                }
                updateSnoozeText();
            }
        });


        mSnoozeText = v.findViewById(R.id.snooze_time_text);
        mSnoozeText2 = v.findViewById(R.id.snooze_time_text_2);
        mSnoozeList = Arrays.asList(getResources().getStringArray(R.array.snooze_labels));
        final HorizontalScrollView horizontalScrollView =  v.findViewById(R.id.snooze_scrollview);
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                // TODO Auto-generated method stub

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

    private void cancel() {
        this.getDialog().cancel();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
    }
}

