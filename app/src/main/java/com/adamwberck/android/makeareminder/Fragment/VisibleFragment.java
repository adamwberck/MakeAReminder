package com.adamwberck.android.makeareminder.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.R;

import static com.adamwberck.android.makeareminder.Fragment.TaskFragment.hideSoftKeyboard;

public class VisibleFragment extends Fragment {
    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void alterActionBar( int colorInt, Context context, Activity
            activity) {
        ActionBar actionBar = ((AppCompatActivity)activity).getSupportActionBar();
        Resources r = context.getResources();
        actionBar.setBackgroundDrawable(new ColorDrawable(colorInt));
        TextView tv = setActionBarTextColor(r.getString(R.string.app_name)
                ,colorInt,context);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(tv);
        activity.invalidateOptionsMenu();
    }

    @NonNull
    public static TextView setActionBarTextColor(CharSequence title,int colorInt, Context context) {
        Resources resources = context.getResources();
        int abColor = OverviewFragment.isDark(colorInt) ?
                resources.getColor(R.color.white) : resources.getColor(R.color.black);
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(abColor);
        tv.setTextSize(18);
        return tv;
    }
}
