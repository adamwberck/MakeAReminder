package com.adamwberck.android.makeareminder.Elements;

import android.annotation.SuppressLint;
import android.content.Context;

import com.adamwberck.android.makeareminder.R;

import java.io.Serializable;
import java.util.Comparator;



public class Reminder implements Serializable{
    private static Comparator<Reminder> sComparator = new Comparator<Reminder>() {
        @Override
        public int compare(Reminder r1, Reminder r2) {
            if(r1.getTimeBefore()!=r2.getTimeBefore()){
                return (r1.getMinutes() < r2.getMinutes()) ? 1 : -1;
            }else{
                return 0;
            }
        }
    };

    private Context mContext;
    private Task mTask;
    private SpanOfTime mTimeBefore;

    @Override
    public boolean equals(Object o){
        return getMinutes()==((Reminder)o).getMinutes();
    }


    public Reminder(Task task, SpanOfTime duration, Context context) {
        mTask = task;
        mContext = context;
        mTimeBefore = duration;
    }

    public static Comparator<Reminder> getComparator() {
        return sComparator;
    }


    @SuppressLint("NewApi")
    public String getInfo() {
        if(mTimeBefore.getMinutes() == 0){
            return mContext.getString(R.string.when_due);
        }else {
            return getTimeBefore().getTimeString(mContext);
        }

    }


    public SpanOfTime getTimeBefore() {
        return mTimeBefore;
    }

    public long getMinutes() {
        return getTimeBefore().getMinutes();
    }
}
