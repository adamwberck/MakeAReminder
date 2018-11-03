package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.NotificationChannel;
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

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;

public class ReminderParentDialog extends DismissDialog {
    private static final int REQUEST_ALARM = 0;
    private static final int REQUEST_NOTIFICATION = 1;

    private static final String TAG = "ParentReminder";


    private Switch mAlarmSwitch;
    private ImageView mAlertTypeIcon;
    private Button mRingtoneSelectorButton;
    private ImageView mVibrateIcon;
    private Switch mVibrateSwitch;
    private SeekBar mSeekbar;

    protected float mVolume;
    protected boolean mIsAlarm;
    protected boolean mDoesVibrate;
    protected Ringtone mAlarmRingtone;
    protected Ringtone mNotificationRingtone;
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


        mRingtoneSelectorButton = view.findViewById(R.id.ringtone_selector_button);
        mRingtoneSelectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ringtoneStop();
                if(mIsAlarm) {
                    Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    ringtoneIntent
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                    startActivityForResult(ringtoneIntent, REQUEST_ALARM);
                }
                else{
                    Intent ringtoneIntent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                    ringtoneIntent
                            .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
                    startActivityForResult(ringtoneIntent, REQUEST_ALARM);
                }
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

        if(mIsAlarm) {
            mRingtoneUri = RingtoneManager
                    .getActualDefaultRingtoneUri(getActivity()
                            .getApplicationContext(), RingtoneManager.TYPE_ALARM);
            mAlarmRingtone.setStreamType(AudioManager.STREAM_ALARM);
            mAlarmRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
        }
        else{
            mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(
                    getActivity().getApplicationContext(),RingtoneManager.TYPE_NOTIFICATION);
            mNotificationRingtone.setStreamType(AudioManager.STREAM_NOTIFICATION);
        }



        final Drawable play = getResources().getDrawable(R.drawable.ic_play_sound);
        final Drawable pause = getResources().getDrawable(R.drawable.ic_pause_sound);
        soundTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ringtone ringtone = mIsAlarm ? mAlarmRingtone : mNotificationRingtone;
                if(ringtone.isPlaying()){
                    ringtone.stop();
                    soundTest.setImageDrawable(play);
                }
                else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        ringtone.setVolume(mVolume);
                    }
                    ringtone.play();
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
            mVolume = oldReminder.getVolume();

            if(mIsAlarm) {
                mAlarmRingtone = oldReminder.getRingtone(getContext());
            }
            else {
                mNotificationRingtone = oldReminder.getRingtone(getContext());
            }
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
        Ringtone ringtone = mIsAlarm?mAlarmRingtone:mNotificationRingtone;
        mRingtoneSelectorButton.setText(ringtone.getTitle(getContext()));
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data){
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(REQUEST_ALARM==requestCode){
            mRingtoneUri = (Uri) data.getExtras().get(EXTRA_RINGTONE_PICKED_URI);
            mAlarmRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
            mAlarmRingtone.setStreamType(AudioManager.STREAM_ALARM);

            Log.i(TAG,"onActivity: Uri = " + mRingtoneUri.toString());
            Log.i(TAG,"onActivity: Alarm = " + mAlarmRingtone.getTitle(getContext()));
        }
        if(REQUEST_NOTIFICATION==requestCode){
            mRingtoneUri = (Uri) data.getExtras().get(EXTRA_RINGTONE_PICKED_URI);
            mNotificationRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
            mNotificationRingtone.setStreamType(AudioManager.STREAM_ALARM);

            Log.i(TAG,"onActivity: Uri = " + mRingtoneUri.toString());
            Log.i(TAG,"onActivity: Notif = " + mNotificationRingtone.getTitle(getContext()));
        }
        updateParentUI();
    }


    protected void ringtoneStop() {
        mAlarmRingtone.stop();
        mNotificationRingtone.stop();
    }

}
