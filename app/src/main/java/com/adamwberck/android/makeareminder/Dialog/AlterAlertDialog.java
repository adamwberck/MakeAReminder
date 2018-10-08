package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;
import com.adamwberck.android.makeareminder.Sound;
import com.adamwberck.android.makeareminder.SoundPlayer;

public class AlterAlertDialog extends DismissDialog {
    private static final String ARG_REMINDER = "reminder";
    public static final String EXTRA_BASE_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    private Spinner mAlertTypeSpinner;
    private ImageView mAlertTypeIcon;
    private Spinner mSoundAlertSpinner;
    private float mVolume;
    private boolean mIsAlarm;
    private boolean mDoesVibrate;
    private ImageView mVibrateIcon;
    private Switch mVibrateSwitch;
    private Sound mSound;


    public static AlterAlertDialog newInstance(@NonNull Reminder reminder) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_REMINDER, reminder);

        AlterAlertDialog fragment = new AlterAlertDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final Reminder oldReminder = ((Reminder) getArguments().getSerializable(ARG_REMINDER));




        View view = inflater.inflate(R.layout.dialog_alter_alert,null);
        mAlertTypeIcon = view.findViewById(R.id.alert_type_icon);


        mAlertTypeSpinner = view.findViewById(R.id.alert_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.alert_type_array,R.layout.spinner_item_black);
        mAlertTypeSpinner.setAdapter(adapter);
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

        mVibrateIcon = view.findViewById(R.id.vibrate_icon);
        mVibrateSwitch = view.findViewById(R.id.vibrate_switch);
        mVibrateSwitch.setChecked(mDoesVibrate);
        mVibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDoesVibrate = isChecked;
                updateVibrateIcon();
            }
        });

        SeekBar seekBar = view.findViewById(R.id.volume_slider);
        ImageButton soundTest = view.findViewById(R.id.sound_test_button);
        soundTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SoundPlayer soundPlayer = GroupLab.get(getContext()).getSoundPlayer();
                soundPlayer.play(mSound,1f);
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

        //init based on current settings
        if(oldReminder!=null){
            mIsAlarm = oldReminder.isAlarm();
            mDoesVibrate = oldReminder.doesVibrate();
            mAlertTypeSpinner.setSelection(mIsAlarm?0:1);
            mSound = oldReminder.getSound();
            mVolume = oldReminder.getVolume();
            updateSeekbar(seekBar);
        }


        updateUI();
        builder.setTitle(R.string.alter_alert).setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Reminder newReminder = createReminder(oldReminder.getTask());
                sendResult(Activity.RESULT_OK,newReminder);
            }}).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancel();
            }
        });
        return builder.create();
    }

    private Reminder createReminder(Task task) {
        SpanOfTime span = SpanOfTime.ofMillis(0);
        return new Reminder(task,span,0,mIsAlarm,mDoesVibrate,mSound,mVolume);
    }

    private void updateSeekbar(SeekBar seekBar) {
        seekBar.setProgress(Math.round(mVolume*100.0f));
    }

    private void updateUI() {
        updateAlertTypeIcon();
        updateVibrateIcon();
        updateSound();
    }

    private void updateVibrateIcon() {
        int icon = mDoesVibrate ? R.drawable.ic_vibrate:R.drawable.ic_not_vibrate;
        mVibrateIcon.setImageResource(icon);
        mVibrateSwitch.setChecked(mDoesVibrate);
    }

    private void updateAlertTypeIcon() {
        int icon = mIsAlarm ? R.drawable.ic_alarm : R.drawable.ic_notification;
        mAlertTypeIcon.setImageResource(icon);
        mAlertTypeSpinner.setSelection(mIsAlarm?0:1);
    }

    private void updateSound() {
        //TODO change sound list
    }

    private void cancel() {
        AlterAlertDialog.this.getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED,null);
    }

    private void sendResult(int resultCode, Reminder reminder) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_BASE_REMINDER, reminder);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
