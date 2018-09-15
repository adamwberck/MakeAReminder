package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CreateReminderDialog extends DismissDialog {
    private static final String ARG_REMINDER = "reminder";
    public static final String EXTRA_NEW_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    public static final String EXTRA_OLD_REMINDER
            = "com.adamwberck.android.makeareminder.oldreminder";
    private static final String TAG = "ReminderDialog";
    private int mTimeTypePos;
    private ImageView mAlertTypeIcon;
    private Spinner mAlertTypeSpinner;
    private ImageButton mVibrateButton;
    private Spinner mSoundAlertSpinner;
    private ArrayAdapter<Integer> mTimeValueArray;
    private Map<SpanOfTime.Type,List<Integer>> mTimeValueMap = new ArrayMap<>(4);
    private int mTimeValuePos;
    private boolean mDoesVibrate;
    private boolean mIsAlarm;
    private Button mCustomizeAlertButton;
    private boolean mMatchesDefault = true;
    private View mCustomizeSection;
    private ImageButton mCloseCustomizeSectionButton;


    public static CreateReminderDialog newInstance() {
        Bundle args = new Bundle();
        CreateReminderDialog fragment = new CreateReminderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateReminderDialog newInstance(Reminder reminder) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder);

        CreateReminderDialog fragment = new CreateReminderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mTimeValueMap.put(SpanOfTime.Type.MINUTE, createIntArray(0,120));
        mTimeValueMap.put(SpanOfTime.Type.HOUR, createIntArray(0,48));
        mTimeValueMap.put(SpanOfTime.Type.DAY, createIntArray(0,60));
        mTimeValueMap.put(SpanOfTime.Type.WEEK, createIntArray(0,52));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_reminder,null);

        mTimeValueArray = new ArrayAdapter(getContext(),R.layout.spinner_item_right);
        final Spinner timeSpinner = view.findViewById(R.id.reminder_time_spinner);
        timeSpinner.setAdapter(mTimeValueArray);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s  = parent.getItemAtPosition(position).toString();
                try {
                    mTimeValuePos=Integer.parseInt(s)+1;
                }catch(NumberFormatException ignored){ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //pass
            }
        });


        Spinner typeSpinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type_reminder,R.layout.spinner_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).toString().equals("")) {
                    mTimeTypePos = position;
                    mTimeValueArray.clear();
                    SpanOfTime.Type spanType = typeFromPos();
                    mTimeValueArray.addAll(mTimeValueMap.get(spanType));
                    timeSpinner.setSelection(0);
                    updateTimeArray();
                    updateUI();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ///Customize Alert Part
        mCustomizeAlertButton = view.findViewById(R.id.customize_alert_button);
        mCustomizeAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMatchesDefault =false;
                updateCustomizeUI();
            }
        });

        mCloseCustomizeSectionButton = view.findViewById(R.id.close_custom_alert);
        mCloseCustomizeSectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMatchesDefault = true;
                updateCustomizeUI();
            }
        });


        mCustomizeSection = view.findViewById(R.id.customize_alert_section);


        final Reminder oldReminder = ((Reminder) getArguments().getSerializable(ARG_REMINDER));
        
        mAlertTypeIcon = view.findViewById(R.id.alert_type_icon);

        mAlertTypeSpinner = view.findViewById(R.id.alert_type_spinner);
        ArrayAdapter<CharSequence> alertTypeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.alert_type_array,R.layout.spinner_item_black);
        mAlertTypeSpinner.setAdapter(alertTypeAdapter);
        mAlertTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mIsAlarm = position==0;
                updateSound();
                updateAlertTypeIcon();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSoundAlertSpinner = view.findViewById(R.id.alert_sound_spinner);
        ArrayAdapter<CharSequence> soundAlert = ArrayAdapter.createFromResource(getContext()
                ,R.array.alert_sounds,R.layout.spinner_item_black);
        mSoundAlertSpinner.setAdapter(soundAlert);

        mVibrateButton = view.findViewById(R.id.vibrate_button);
        mVibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDoesVibrate=!mDoesVibrate;
                updateVibrateIcon();
            }
        });


        
        builder.setTitle(R.string.alter_alert).setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createReminder(oldReminder.getTask());
                    }}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        });


        if(oldReminder !=null) {
            SpanOfTime.Type type = oldReminder.getTimeBefore().getTimeType();
            int time = (int) oldReminder.getTimeBefore().getTime(type);timeSpinner.setSelection(time);
            int spinnerNumber = setSpinnerNumber(type);
            typeSpinner.setSelection(spinnerNumber);
        }


        updateUI();
        return builder.create();
    }

    private void updateCustomizeUI() {
        int vis = mMatchesDefault ? GONE:VISIBLE;
        mCustomizeSection.setVisibility(vis);
        mCloseCustomizeSectionButton.setVisibility(vis);
    }

    private SpanOfTime.Type typeFromPos() {
        if(mTimeTypePos ==0) {
            return SpanOfTime.Type.MINUTE;
        }
        else if(mTimeTypePos ==1){
            return SpanOfTime.Type.HOUR;
        }
        else if(mTimeTypePos ==2){
            return SpanOfTime.Type.DAY;
        }
        else {
            return SpanOfTime.Type.WEEK;
        }
    }

    private void updateTimeArray() {
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
        CreateReminderDialog.this.getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED,null,null);
    }

    private Reminder createReminder(Task task) {
        SpanOfTime span;
        if(mTimeTypePos ==0) {
            span = SpanOfTime.ofMinutes(mTimeValuePos);
        }
        else if(mTimeTypePos ==1){
            span = SpanOfTime.ofHours(mTimeValuePos);
        }
        else if(mTimeTypePos ==2){
            span = SpanOfTime.ofDays(mTimeValuePos);
        }
        else {
            span = SpanOfTime.ofWeeks(mTimeValuePos);
        }
        if(mMatchesDefault) {
            return new Reminder(task, span);
        }
        else {
            return new Reminder(task,span,mIsAlarm,mDoesVibrate);
        }
    }

    private void sendResult(int resultCode, Reminder newReminder,Reminder oldReminder) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_NEW_REMINDER, newReminder);
        intent.putExtra(EXTRA_OLD_REMINDER,oldReminder);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);

    }

    private void updateSound() {
        //TODO change sound list
    }

    private void updateUI() {
        updateAlertTypeIcon();
        updateVibrateIcon();
        updateCustomizeUI();
    }

    private void updateVibrateIcon() {
        int icon = mDoesVibrate ? R.drawable.ic_vibrate:R.drawable.ic_not_vibrate;
        mVibrateButton.setImageResource(icon);
    }

    private void updateAlertTypeIcon() {
        int icon = mIsAlarm ? R.drawable.ic_alarm : R.drawable.ic_notification;
        mAlertTypeIcon.setImageResource(icon);
    }
}
