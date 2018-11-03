package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import java.util.List;
import java.util.Map;

import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CreateReminderDialog extends DismissDialog {
    private static final String ARG_REMINDER = "reminder";
    private static final String ARG_TASK = "task";
    public static final String EXTRA_NEW_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    public static final String EXTRA_OLD_REMINDER
            = "com.adamwberck.android.makeareminder.oldreminder";
    private static final String TAG = "ReminderDialog";
    private static final int REQUEST_RINGTONE = 0;

    private Switch mAlarmSwitch;
    private Button mSoundAlertButton;
    private int mTimeTypePos;
    private ImageView mAlertTypeIcon;
    private ImageView mVibrateIcon;
    private Spinner mSoundAlertSpinner;
    private ArrayAdapter<Integer> mTimeValueArray;
    private Map<SpanOfTime.Type,List<Integer>> mTimeValueMap = new ArrayMap<>(4);
    private int mTimeValuePos;
    private boolean mDoesVibrate;
    private boolean mIsAlarm;
    private Button mCustomizeAlertButton;
    private boolean mMatchesDefault = true;
    private LinearLayout mCustomizeSection;
    private ImageButton mCloseCustomizeSectionButton;
    private Switch mVibrateSwitch;
    private Ringtone mRingtone;
    private float mVolume;
    private Uri mRingtoneUri;


    public static CreateReminderDialog newInstance(@NonNull Reminder reminder) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder);

        CreateReminderDialog fragment = new CreateReminderDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateReminderDialog newInstance(@NonNull Task task) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, null);
        args.putSerializable(ARG_TASK,task);

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

        mTimeValueArray = new ArrayAdapter(getContext(),R.layout.spinner_item_big_right);
        final Spinner timeValueSpinner = view.findViewById(R.id.reminder_time_spinner);
        timeValueSpinner.setAdapter(mTimeValueArray);
        timeValueSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s  = parent.getItemAtPosition(position).toString();
                try {
                    mTimeValuePos=Integer.parseInt(s);
                }catch(NumberFormatException ignored){ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //pass
            }
        });


        Spinner typeSpinner = view.findViewById(R.id.type_spinner);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type_reminder,R.layout.spinner_big_item);
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).toString().equals("")) {
                    mTimeTypePos = position;
                    SpanOfTime.Type spanType = typeFromPos();

                    int oldValuePos = timeValueSpinner.getSelectedItemPosition();

                    mTimeValueArray.clear();
                    mTimeValueArray.addAll(mTimeValueMap.get(spanType));
                    timeValueSpinner.setSelection(oldValuePos);

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

        //TODO use objects to stop rewriting code from alter alder
        //TODO fix sound playing after close
        //TODO fix ringtone name not displaying correctly
        mCustomizeSection = view.findViewById(R.id.customize_alert_section);
        inflater.inflate(R.layout.dialog_alter_alert,mCustomizeSection);
        reduceTextSize(mCustomizeSection,20);

        final Reminder oldReminder = ((Reminder) getArguments().getSerializable(ARG_REMINDER));

        mAlertTypeIcon = mCustomizeSection.findViewById(R.id.alert_type_icon);
        mAlarmSwitch = mCustomizeSection.findViewById(R.id.alarm_switch);
        mAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsAlarm = !isChecked;
                updateAlertTypeIcon();
            }
        });


        mSoundAlertButton = mCustomizeSection.findViewById(R.id.ringtone_selector_button);
        mSoundAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                ringtoneIntent
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
                startActivityForResult(ringtoneIntent,REQUEST_RINGTONE);
            }
        });

        mVibrateIcon = mCustomizeSection.findViewById(R.id.vibrate_icon);
        mVibrateSwitch = mCustomizeSection.findViewById(R.id.vibrate_switch);
        mVibrateSwitch.setChecked(mDoesVibrate);
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDoesVibrate = isChecked;
                updateVibrateIcon();
            }
        });

        SeekBar seekBar = mCustomizeSection.findViewById(R.id.volume_slider);
        TextView volumeText = mCustomizeSection.findViewById(R.id.volume_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            seekBar.setVisibility(View.VISIBLE);
        }
        else {
            volumeText.setText(R.string.vibration);
            seekBar.setVisibility(View.INVISIBLE);
            mVolume = 1.0f;
        }
        ImageButton soundTest = mCustomizeSection.findViewById(R.id.sound_test_button);


        mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity().getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        soundTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRingtone.isPlaying()){
                    mRingtone.stop();
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mRingtone.setVolume(mVolume);
                    }
                    mRingtone.play();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mVolume = (progress*1.0f)/100.0f;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //End Customize Alert

        //Read old settings
        final Task oldTask;
        if(oldReminder != null) {
            //IMPORT OLD REMINDER IF EDIT
            mMatchesDefault=oldReminder.matchesDefault();
            mIsAlarm=oldReminder.isAlarm();
            mDoesVibrate=oldReminder.doesVibrate();
            oldTask = oldReminder.getTask();

            //Set Type to correct spinner number
            SpanOfTime.Type type = oldReminder.getTimeBefore().getTimeType();
            int typeSpinnerNumber = getSpinnerNumberFromType(type);
            typeSpinner.setSelection(typeSpinnerNumber);
            mTimeValueArray.clear();
            mTimeValueArray.addAll(mTimeValueMap.get(type));

            mRingtone = oldReminder.getRingtone(getContext());
            mVolume = oldReminder.getVolume();
            updateSeekbar(seekBar);

            //Set Time Value to correct spinner number
            timeValueSpinner.setSelection(oldReminder.getInputTime());
        }
        else{
            //IMPORT BASE REMINDER
            oldTask = (Task) getArguments().getSerializable(ARG_TASK);
            assert oldTask != null;
            Reminder baseReminder = oldTask.getBaseReminder();

            mMatchesDefault=true;
            mIsAlarm=baseReminder.isAlarm();
            mDoesVibrate=baseReminder.doesVibrate();

            mRingtone = oldTask.getBaseReminder().getRingtone(getContext());
            mVolume = oldTask.getBaseReminder().getVolume();
            updateSeekbar(seekBar);
        }
        updateSeekbar(seekBar);


        String title = oldReminder==null ? getString(R.string.create_reminder) :
                getString(R.string.edit_reminder);

        builder.setTitle(title).setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Reminder newReminder = createReminder(oldTask);
                        sendResult(Activity.RESULT_OK,newReminder,oldReminder);
                    }}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        });
        updateUI();
        return builder.create();
    }

    private void updateSeekbar(SeekBar seekBar) {
        seekBar.setProgress(Math.round(mVolume*100.0f));
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

    private static int getSpinnerNumberFromType(SpanOfTime.Type type) {
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
        if(mTimeTypePos == 0) {
            span = SpanOfTime.ofMinutes(mTimeValuePos);
        }
        else if(mTimeTypePos == 1){
            span = SpanOfTime.ofHours(mTimeValuePos);
        }
        else if(mTimeTypePos == 2){
            span = SpanOfTime.ofDays(mTimeValuePos);
        }
        else {
            span = SpanOfTime.ofWeeks(mTimeValuePos);
        }
        if(mMatchesDefault) {
            return new Reminder(task, span,mTimeValuePos);
        }
        else {
            return new Reminder(task,span,mTimeValuePos,mIsAlarm,mDoesVibrate,mRingtoneUri,mVolume);
        }
    }

    private void sendResult(int resultCode, Reminder newReminder,Reminder oldReminder) {
        if (getTargetFragment() == null) {
            return;
        }
        mRingtone.stop();
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
        updateRingtone();
    }

    private void updateVibrateIcon() {
        int icon = mDoesVibrate ? R.drawable.ic_vibrate:R.drawable.ic_not_vibrate;
        mVibrateIcon.setImageResource(icon);
        mVibrateSwitch.setChecked(mDoesVibrate);
    }

    private void updateAlertTypeIcon() {
        int icon = mIsAlarm ? R.drawable.ic_alarm : R.drawable.ic_notification;
        mAlertTypeIcon.setImageResource(icon);
        mAlarmSwitch.setChecked(!mIsAlarm);
    }

    private void updateRingtone() {
        mSoundAlertButton.setText(mRingtone.getTitle(getContext()));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(REQUEST_RINGTONE==requestCode){
            mRingtoneUri = (Uri) data.getExtras().get(EXTRA_RINGTONE_PICKED_URI);
            mRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
            mRingtone.setStreamType(AudioManager.STREAM_ALARM);

            Log.i(TAG,"onActivity: Uri = " + mRingtoneUri.toString());
            Log.i(TAG,"onActivity: Ringtone = " + mRingtone.getTitle(getContext()));
        }
        updateUI();
    }
}
