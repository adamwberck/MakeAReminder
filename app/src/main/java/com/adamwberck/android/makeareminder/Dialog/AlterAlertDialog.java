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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import static android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI;

public class AlterAlertDialog extends ReminderParentDialog {
    private static final String ARG_REMINDER = "reminder";
    public static final String EXTRA_BASE_REMINDER
            = "com.adamwberck.android.makeareminder.newreminder";
    private static final String TAG = "AlterAlert";

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

        setupRingtoneEdit(view,oldReminder,null);

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

    private void updateUI() {
        updateParentUI();
    }

    private Reminder createReminder(Task task) {
        SpanOfTime span = SpanOfTime.ofMillis(0);
        return new Reminder(task,span,0,mIsAlarm,mDoesVibrate,mRingtoneUri,mVolume);
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

}
