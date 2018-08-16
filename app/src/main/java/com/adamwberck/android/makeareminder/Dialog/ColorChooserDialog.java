package com.adamwberck.android.makeareminder.Dialog;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.adamwberck.android.makeareminder.R;

import java.util.Arrays;
import java.util.List;

public class ColorChooserDialog extends DialogFragment {

    private static final String ARG_COLOR = "color";
    public static final String EXTRA_COLOR =
            "com.adamwberck.android.makeareminder.Dialog.ColorChooserDialog.color" ;
    private String mColor;
    private int mSelectedButton = -1;

    private int mButtonNumber = 0;
    private List<String> mColorList;
    private View mColorButtons;

    public static ColorChooserDialog newInstance(String color) {
        Bundle args = new Bundle();
        args.putString(ARG_COLOR,color);
        ColorChooserDialog fragment = new ColorChooserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_color_chooser,
                null);
        mColor = getArguments().getString(ARG_COLOR);
        mColorList = Arrays.asList(getResources().getStringArray(R.array.group_colors));
        //final Task task = (Task) getArguments().getSerializable(ARG_TASK);
        //final HorizontalScrollView horizontalScrollView =  v.findViewById(R.id.snooze_scrollview);
        mColorButtons = v.findViewById(R.id.color_buttons);
        setupButtons(mColorButtons);

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
                sendResult(Activity.RESULT_OK,null);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Set Color");
        return builder.create();
    }
    public void setupButtons(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof Button) {
            Button b = (Button) view;
            b.setId(mButtonNumber);

            if(b.getId()==mSelectedButton){
                b.setBackground(getResources().getDrawable(R.drawable.ic_button_square_color_on));
            }
            else{
                b.setBackground(getResources().getDrawable(R.drawable.ic_button_square_color_off));
            }

            String color = mColorList.get(mButtonNumber++);
            b.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.DARKEN);
            b.invalidate();

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonClick(v);
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

    private void updateSelected(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        for(int i=0;i<mColorList.size();i++){
            Button b = view.findViewById(i);
            if (b.getId() == mSelectedButton) {
                b.setBackground(getResources().getDrawable(R.drawable.ic_button_square_color_on));
            } else {
                b.setBackground(getResources().getDrawable(R.drawable.ic_button_square_color_off));
            }
            String color = mColorList.get(i);
            b.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.DARKEN);
            b.invalidate();
        }
    }

    private void buttonClick(View v) {
        int buttonNum = v.getId();
        mSelectedButton = buttonNum;
        mColor = mColorList.get(buttonNum);
        updateSelected(mColorButtons);
    }

    private void sendResult(int resultCode, String color ){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_COLOR,color);
        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
        getDialog().cancel();
    }
    private void cancel() {
        this.getDialog().cancel();
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, new Intent());
    }
}
