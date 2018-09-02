package com.adamwberck.android.makeareminder.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.R;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.WHITE;
import static com.adamwberck.android.makeareminder.Fragment.TaskFragment.hideSoftKeyboard;

public class VisibleFragment extends Fragment {
    public void setupUI(final View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    v.requestFocus();
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

    public static void alterActionBar( int colorInt, Context context, Activity activity) {
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

    protected static void styleMenuButtons(Menu menu, int colorInt, Activity activity){

        for(int i=0;i<menu.size();i++){
            styleMenuButton(colorInt,menu.getItem(i),activity);
        }
    }

    private static void styleMenuButton(int colorInt, MenuItem menuItem,Activity activity) {
        // Cast to a TextView instance if the menu item was found
        View view = activity.findViewById(menuItem.getItemId());
        int abColor = OverviewFragment.isDark(colorInt) ?
                WHITE : BLACK;
        Drawable icon = menuItem.getIcon();
        if(icon!=null) {
            icon.setColorFilter(abColor, PorterDuff.Mode.SRC_ATOP);
        }

        if (view != null && view instanceof TextView) {
            ((TextView) view).setTextColor(abColor);
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        }
    }

}
