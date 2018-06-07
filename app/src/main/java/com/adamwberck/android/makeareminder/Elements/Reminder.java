package com.adamwberck.android.makeareminder.Elements;

import android.annotation.SuppressLint;
import android.content.Context;

import com.adamwberck.android.makeareminder.R;

import java.util.Comparator;


public class Reminder {
    private static Comparator<Reminder> sComparator = new Comparator<Reminder>() {
        @Override
        public int compare(Reminder r1, Reminder r2) {
            if(r1.getTimeBefore()!=r2.getTimeBefore()){
                return (r1.getMinutes() > r2.getMinutes()) ? 1 : -1;
            }else{
                return 0;
            }
        }
    };

    private Context mContext;
    private Task mTask;
    private SpanOfTime mTimeBefore;




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
            SpanOfTime.Type type = mTimeBefore.getTimeType();
            if(type== SpanOfTime.Type.MINUTE){
                String stringType = mContext.getResources().getQuantityString(R.plurals.minute,getMinutes());
                return mContext.getString(R.string.before_due,mTimeBefore.getMinutes()
                        ,stringType);
            }
            else if(type==SpanOfTime.Type.HOUR){

            }
            else if(type==SpanOfTime.Type.DAY){

            }
            else if(type==SpanOfTime.Type.WEEK){

            }
        }
    }

    public SpanOfTime getTimeBefore() {
        return mTimeBefore;
    }

    public long getMinutes() {
        return getTimeBefore().getMinutes();
    }
}
