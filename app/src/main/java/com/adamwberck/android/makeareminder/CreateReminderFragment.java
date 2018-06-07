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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.adamwberck.android.makeareminder.Elements.SpanOfTime;

import java.time.Period;

public class CreateReminderFragment extends DialogFragment {
    private static final String ARG_SPAN = "span";
    public static final String EXTRA_SPAN = "com.adamwberck.android.makeareminder.span";
    private long mDuration;
    private int mTimeTypeInt;


    public static CreateReminderFragment newInstance() {
        Bundle args = new Bundle();
        //args.putSerializable(ARG_SPAN, span);

        CreateReminderFragment fragment = new CreateReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_reminder,null);
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
        Spinner spinner = view.findViewById(R.id.type_time_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type,android.R.layout.simple_spinner_item);
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

        builder.setTitle(R.string.reminder_dialog)
                .setView(view)
                .setPositiveButton(R.string.create_reminder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createReminder();
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

    private void cancel() {
        CreateReminderFragment.this.getDialog().cancel();
    }

    private void createReminder() {
        SpanOfTime span;
        if(mTimeTypeInt==0) {
            span = SpanOfTime.ofMinutes(mDuration);
        }
        else if(mTimeTypeInt==1){
            span = SpanOfTime.ofHours(mDuration);
        }
        else if(mTimeTypeInt==2){
            span = SpanOfTime.ofDays(mDuration);
        }
        else {
            span = SpanOfTime.ofWeeks(mDuration);
        }
        sendResult(Activity.RESULT_OK,span);
    }

    private void sendResult(int resultCode, SpanOfTime span) {
        if (getTargetFragment() == null) {
            return;
        }
        int d = getTargetRequestCode();
        int f = resultCode;

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SPAN, span);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
