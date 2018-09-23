package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;

/**
 * Created by Adam on 8/21/2017.
 */

public class DatePickerDialog extends DismissDialog {

    public static final String EXTRA_DATE =
            "com.bignerdranch.android.criminalintent.date";

    private static final String ARG_DATE = "date";

    private DatePicker mDatePicker;

    public static DatePickerDialog newInstance(DateTime date) {
        Bundle args = new Bundle();
        if(date!=null) {
            args.putSerializable(ARG_DATE, date);
        }
        else {

            args.putSerializable(ARG_DATE, SpanOfTime.roundTime(new DateTime(),15));
        }

        DatePickerDialog fragment = new DatePickerDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final DateTime dateTime = (DateTime) getArguments().getSerializable(ARG_DATE);
        int year = dateTime.getYear();
        int month = dateTime.getMonthOfYear();
        int day = dateTime.getDayOfMonth();
        //TODO make it set to current date if button date is in the past


        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_date,null);

        mDatePicker = v.findViewById(R.id.dialog_date_picker);
        mDatePicker.init(year, month-1,day,null);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();


                        int hour = dateTime.getHourOfDay();
                        int minute = dateTime.getHourOfDay();

                        DateTime date = new DateTime(year, month+1, day,hour,minute);
                        sendResult(Activity.RESULT_OK,date);
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
        intent.putExtra(EXTRA_DATE, date);

        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
