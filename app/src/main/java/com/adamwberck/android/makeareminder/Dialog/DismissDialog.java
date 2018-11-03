package com.adamwberck.android.makeareminder.Dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DismissDialog extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        getDialog().setCanceledOnTouchOutside(true);
        return super.onCreateView(inflater,container,savedInstanceState);
    }


    public static List<Integer> createIntArray(int start,int size) {
        List<Integer> list = new ArrayList<>(size);
        for(int i=start;i<=size;i++){
            list.add(i);
        }
        return list;
    }

    public static void reduceTextSize(final View view, int size) {
        if(view instanceof TextView){
            TextView textView = (TextView) view;
            if(textView.getTextSize()>size) {
                textView.setTextSize(size);
            }
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                reduceTextSize(innerView,size);
            }
        }
    }
}
