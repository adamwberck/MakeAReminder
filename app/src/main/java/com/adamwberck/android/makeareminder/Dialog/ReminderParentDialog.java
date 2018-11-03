package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Dialog.DismissDialog;
import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;

public class ReminderParentDialog extends DismissDialog {
    private static final int REQUEST_RINGTONE = 0;

    private static final String TAG = "ParentReminder";

    private Switch mAlarmSwitch;
    private ImageView mAlertTypeIcon;
    private Button mSoundAlertButton;
    private ImageView mVibrateIcon;
    private Switch mVibrateSwitch;
    private SeekBar mSeekbar;

    protected float mVolume;
    protected boolean mIsAlarm;
    protected boolean mDoesVibrate;
    protected Ringtone mRingtone;
    protected Uri mRingtoneUri;

    public void setupRingtoneEdit(View view, Reminder oldReminder,Task oldTask){
        mAlertTypeIcon = view.findViewById(R.id.alert_type_icon);
        mAlarmSwitch = view.findViewById(R.id.alarm_switch);
        mAlarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mIsAlarm = !isChecked;
                updateAlertTypeIcon();
            }
        });


        mSoundAlertButton = view.findViewById(R.id.ringtone_selector_button);
        mSoundAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtoneStop();
                Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                ringtoneIntent
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
                startActivityForResult(ringtoneIntent,REQUEST_RINGTONE);
            }
        });

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

        mSeekbar = view.findViewById(R.id.volume_slider);
        TextView volumeText = view.findViewById(R.id.volume_text);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mSeekbar.setVisibility(View.VISIBLE);
        }
        else {
            volumeText.setText(R.string.vibration);
            mSeekbar.setVisibility(View.INVISIBLE);
            mVolume = 1.0f;
        }
        final ImageButton soundTest = view.findViewById(R.id.sound_test_button);


        mRingtoneUri = RingtoneManager
                .getActualDefaultRingtoneUri(getActivity()
                        .getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);


        final Drawable play = getResources().getDrawable(R.drawable.ic_play_sound);
        final Drawable pause = getResources().getDrawable(R.drawable.ic_pause_sound);
        soundTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRingtone.isPlaying()){
                    mRingtone.stop();
                    soundTest.setImageDrawable(play);
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        mRingtone.setVolume(mVolume);
                    }
                    mRingtone.play();
                    soundTest.setImageDrawable(pause);
                }
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
            mAlarmSwitch.setChecked(!mIsAlarm);
            mRingtone = oldReminder.getRingtone(getContext());
            mVolume = oldReminder.getVolume();
            updateSeekbar();
        }
        else {
            //IMPORT BASE REMINDER
            assert oldTask != null;
            Reminder baseReminder = oldTask.getBaseReminder();
            mIsAlarm=baseReminder.isAlarm();
            mDoesVibrate=baseReminder.doesVibrate();
        }
    }

    protected void updateSeekbar() {
        mSeekbar.setProgress(Math.round(mVolume*100.0f));
    }

    protected void updateParentUI() {
        updateAlertTypeIcon();
        updateVibrateIcon();
        updateRingtone();
    }

    protected void updateVibrateIcon() {
        int icon = mDoesVibrate ? R.drawable.ic_vibrate:R.drawable.ic_not_vibrate;
        mVibrateIcon.setImageResource(icon);
        mVibrateSwitch.setChecked(mDoesVibrate);
    }

    protected void updateAlertTypeIcon() {
        int icon = mIsAlarm ? R.drawable.ic_alarm : R.drawable.ic_notification;
        mAlertTypeIcon.setImageResource(icon);
        mAlarmSwitch.setChecked(!mIsAlarm);
    }

    protected void updateRingtone() {
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
        updateParentUI();
    }


    protected void ringtoneStop() {
        mRingtone.stop();
    }

}
