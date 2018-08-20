package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;

/**
 * Created by Adam on 8/21/2017.
 */

public class TimePickerDialog extends DismissDialog {

    public static final String EXTRA_TIME =
            "com.bignerdranch.android.criminalintent.date";

    private static final String ARG_TIME = "time";

    private TimePicker mTimePicker;

    public static TimePickerDialog newInstance(DateTime date) {
        Bundle args = new Bundle();
        if(date!=null) {
            args.putSerializable(ARG_TIME, date);
        }
        else {
            args.putSerializable(ARG_TIME, new DateTime());
        }

        TimePickerDialog fragment = new TimePickerDialog();
        fragment.setArguments(args);


        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DateTime date = (DateTime) getArguments().getSerializable(ARG_TIME);
        final int hour = date.getHourOfDay();
        final int minute = date.getMinuteOfHour();
        //TODO make it set to current time if button time is in the past

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time,null);

        mTimePicker = v.findViewById(R.id.dialog_time_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        }
        else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int year = date.getYear();
                        int month = date.getMonthOfYear();
                        int day = date.getDayOfMonth();

                        int hour = 0;
                        int minute = 0;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            hour = mTimePicker.getHour();
                            minute = mTimePicker.getMinute();
                        }else{
                            hour = mTimePicker.getCurrentHour();
                            minute = mTimePicker.getCurrentMinute();
                        }

                        sendResult(Activity.RESULT_OK,new DateTime(year,month,day,hour,minute));
                    }
                })
                .setNegativeButton(R.string.clear_time, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK,null);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, DateTime date) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
