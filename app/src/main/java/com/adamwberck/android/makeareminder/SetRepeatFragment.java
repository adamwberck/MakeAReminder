package com.adamwberck.android.makeareminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Repeat;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;

public class SetRepeatFragment extends DialogFragment {
    public static final String ARG_REPEAT = "repeat";
    public static final String EXTRA_REPEAT = "com.adamwberck.android.makeareminder.extrarepeat";
    private long mDuration;
    private int mTimeTypeInt;

    //TODO add exclusion days for hourly repeats

    public static CreateReminderFragment newInstance() {
        Bundle args = new Bundle();
        CreateReminderFragment fragment = new CreateReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateReminderFragment newInstance(SpanOfTime currentRepeat) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_REPEAT, currentRepeat);

        CreateReminderFragment fragment = new CreateReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_repeat,null);
        EditText timeText = view.findViewById(R.id.amount_time_text);
        timeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //pass
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //pass
            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                try {
                    mDuration = Long.parseLong(s1);
                }catch(NumberFormatException ignored){

                }
            }
        });
        Spinner spinner = view.findViewById(R.id.repeat_time_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type_repeat,android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).toString().equals("")) {
                    mTimeTypeInt = position;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        builder.setTitle(R.string.repeat_title)
                .setView(view)
                .setPositiveButton(R.string.repeat_postive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRepeat();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                });


        return builder.create();
    }

    private static int setSpinnerNumber(SpanOfTime.Type type) {
        if(type==SpanOfTime.Type.MINUTE) {
            return 0;
        }
        else if(type== SpanOfTime.Type.HOUR){
            return 1;
        }
        else if(type==SpanOfTime.Type.DAY){
            return 2;
        }
        else {
            return 3;
        }
    }

    private void cancel() {
        SetRepeatFragment.this.getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED,null);
    }

    private void setRepeat() {
        SpanOfTime span;
        if(mTimeTypeInt==0){
            span = SpanOfTime.ofDays(mDuration);
        }
        else if (mTimeTypeInt==1){
            span = SpanOfTime.ofWeeks(mDuration);
        }
        else if(mTimeTypeInt==2){
            //TODO add month support
            span = SpanOfTime.ofWeeks(mDuration);
        }
        else {
            //TODO add year support
            span = SpanOfTime.ofWeeks(mDuration);
        }
        Repeat repeat = new Repeat(mDuration,span);
        sendResult(Activity.RESULT_OK,repeat);
    }

    private void sendResult(int resultCode, Repeat repeat) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_REPEAT, repeat);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

}
