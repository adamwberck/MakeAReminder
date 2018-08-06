package com.adamwberck.android.makeareminder.Dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adamwberck.android.makeareminder.R;

import java.util.ArrayList;
import java.util.List;

public class ColorChooserDialog extends DialogFragment {

    private static final String ARG_COLOR = "color";
    private static final String EXTRA_COLOR =
            "com.adamwberck.android.makeareminder.Dialog.ColorChooserDialog.color" ;
    private int mColor;
    private int mButtonNumber = 0;
    private List<Integer> mColorList;

    public static ColorChooserDialog newInstance(int color) {
        Bundle args = new Bundle();
        args.putInt(ARG_COLOR,color);
        ColorChooserDialog fragment = new ColorChooserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_color_chooser,
                null);
        mColor = getArguments().getInt(ARG_COLOR);
        mColorList = intsToList(getResources().getIntArray(R.array.color_picker));
        //final Task task = (Task) getArguments().getSerializable(ARG_TASK);
        //final HorizontalScrollView horizontalScrollView =  v.findViewById(R.id.snooze_scrollview);
        setupButtons(v.findViewById(R.id.color_buttons));

        //End Buttons
        Button snoozeButton = v.findViewById(R.id.change_color_button);
        snoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK,mColor);
            }
        });
        Button cancelButton = v.findViewById(R.id.cancel_dialog_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResult(Activity.RESULT_OK,-1);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Set Color");
        return builder.create();
    }

    private static List<Integer> intsToList(int[] ints) {
        List<Integer> list = new ArrayList<>(ints.length);
        for(int i : ints){
            list.add(i);
        }
        return list;
    }

    public void setupButtons(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof Button) {
            Button b = (Button) view;
            b.setId(mButtonNumber);
            //b.setBackgroundColor(mColorList.get(mButtonNumber++));
            b.setBackgroundColor(mColorList.get(mButtonNumber++));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick(v);
                    updateSelected();
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupButtons(innerView);
            }
        }
    }

    private void updateSelected() {
    }

    private void buttonClick(View v) {
        int buttonNum = v.getId();
        mColor = mColorList.get(buttonNum);
    }

    private void sendResult(int resultCode, int color ){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COLOR,color);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
        getDialog().cancel();
    }
    private void cancel() {
        this.getDialog().cancel();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, new Intent());
    }
}
