package com.adamwberck.android.makeareminder.Dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;

public class AlterAlertDialog extends DismissDialog {
    private static final String ARG_REMINDER = "reminder";
    public static final String EXTRA_BASE_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    private static final int REQUEST_RINGTONE = 0;
    private static final String TAG = "AlterAlert";
    private Spinner mAlertTypeSpinner;
    private ImageView mAlertTypeIcon;
    private Button mSoundAlertButton;
    private float mVolume;
    private boolean mIsAlarm;
    private boolean mDoesVibrate;
    private ImageView mVibrateIcon;
    private Switch mVibrateSwitch;
    private Ringtone mRingtone;
    private Uri mRingtoneUri;


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
                updateRingtone();
                updateAlertTypeIcon();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mSoundAlertButton = view.findViewById(R.id.alert_sound_button);
        mSoundAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

        SeekBar seekBar = view.findViewById(R.id.volume_slider);
        ImageButton soundTest = view.findViewById(R.id.sound_test_button);
        

        mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getActivity().getApplicationContext(), RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(getActivity(), mRingtoneUri);
        mRingtone.setStreamType(AudioManager.STREAM_ALARM);
        soundTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SoundPlayer soundPlayer = GroupLab.get(getContext()).getSoundPlayer();
                //soundPlayer.play(mSound,1f);
                if(mRingtone.isPlaying()){
                    mRingtone.stop();
                }
                else {
                    AudioManager manager = (AudioManager)
                            getContext().getSystemService(Context.AUDIO_SERVICE);
                    int vol = manager.getStreamVolume(AudioManager.STREAM_ALARM);
                    int max = manager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                    Log.i(TAG,"Vol: " +vol+"/"+max );

                    //manager.setStreamVolume(AudioManager.STREAM_ALARM,);
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

        //init based on current settings
        if(oldReminder!=null){
            mIsAlarm = oldReminder.isAlarm();
            mDoesVibrate = oldReminder.doesVibrate();
            mAlertTypeSpinner.setSelection(mIsAlarm?0:1);
            mRingtone = oldReminder.getRingtone(getContext());
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
        return new Reminder(task,span,0,mIsAlarm,mDoesVibrate,mRingtoneUri,mVolume);
    }

    private void updateSeekbar(SeekBar seekBar) {
        seekBar.setProgress(Math.round(mVolume*100.0f));
    }

    private void updateUI() {
        updateAlertTypeIcon();
        updateVibrateIcon();
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
        mAlertTypeSpinner.setSelection(mIsAlarm?0:1);
    }

    private void updateRingtone() {
        mSoundAlertButton.setText(mRingtone.getTitle(getContext()));
    }

    private void cancel() {
        AlterAlertDialog.this.getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED,null);
        stopRingtone();
    }

    private void stopRingtone() {
        if(mRingtone.isPlaying()){
            mRingtone.stop();
        }
    }

    private void sendResult(int resultCode, Reminder reminder) {
        stopRingtone();
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_BASE_REMINDER, reminder);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
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
