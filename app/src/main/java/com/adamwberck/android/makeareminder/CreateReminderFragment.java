package com.adamwberck.android.makeareminder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
//TODO make dialogs dismiss when clicked off of
public class CreateReminderFragment extends DismissDialogFragment {
    private static final String ARG_REMINDER = "reminder";
    public static final String EXTRA_NEW_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    public static final String EXTRA_DELETE_REMINDER
            = "com.adamwberck.android.makeareminder.deletereminder";
    public static final String EXTRA_IS_ALARM = "com.adamwberck.android.makeareminder.isalarm";
    private long mDuration;
    private int mTimeTypeInt;
    private boolean mWarningTypeIsAlarm;


    public static CreateReminderFragment newInstance() {
        Bundle args = new Bundle();
        CreateReminderFragment fragment = new CreateReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateReminderFragment newInstance(Reminder reminder) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder);

        CreateReminderFragment fragment = new CreateReminderFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_reminder,null);
        EditText timeText = view.findViewById(R.id.reminder_time_text);
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
                R.array.time_type,R.layout.spinner_item);
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
        final Switch sw = view.findViewById(R.id.reminder_warning_type_switch);
        final TextView warningText = view.findViewById(R.id.reminder_warning_type_text);
        updateSwitchLabel(sw, warningText);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateSwitchLabel(sw, warningText);
            }
        });

        final Reminder reminder = (Reminder) getArguments().getSerializable(ARG_REMINDER);
        String posText = reminder==null ? getString(R.string.create_reminder) :
                getString(R.string.edit_reminder);
        String title = reminder==null ? getString(R.string.reminder_add_dialog) :
                getString(R.string.reminder_edit_dialog);
        builder.setTitle(title)
                .setView(view)
                .setPositiveButton(posText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mWarningTypeIsAlarm = sw.isChecked();
                        createReminder(reminder);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancel();
                    }
                });

        if(reminder!=null) {
            SpanOfTime.Type type = reminder.getTimeBefore().getTimeType();
            long time = reminder.getTimeBefore().getTime(type);
            timeText.setText(""+time);
            int spinnerNumber = setSpinnerNumber(type);
            spinner.setSelection(spinnerNumber);
            sw.setChecked(reminder.isAlarm());

        }

        return builder.create();
    }

    private void updateSwitchLabel(Switch sw, TextView warningText) {
        String strWarningText = sw.isChecked()
                ? getString(R.string.alarm) : getString(R.string.notification);
        warningText.setText(strWarningText);
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
        CreateReminderFragment.this.getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED,null,false,null);
    }

    private void createReminder(Reminder editReminder) {
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
        sendResult(Activity.RESULT_OK,span,mWarningTypeIsAlarm, editReminder);
    }

    private void sendResult(int resultCode, SpanOfTime span, boolean isAlarm, Reminder editReminder) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NEW_REMINDER, span);
        intent.putExtra(EXTRA_IS_ALARM, isAlarm);
        if(editReminder==null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        }
        else {
            editReminder.setWarningType(mWarningTypeIsAlarm);
            intent.putExtra(EXTRA_DELETE_REMINDER,editReminder);
            getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
        }
    }
}
