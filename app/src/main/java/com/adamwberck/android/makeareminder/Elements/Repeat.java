package com.adamwberck.android.makeareminder.Elements;

import android.annotation.SuppressLint;
import android.util.ArrayMap;

import org.joda.time.DateTimeConstants;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repeat implements Serializable{
    private long mEveryPeriod;
    private SpanOfTime mRepeatTime;
    private Map<DayOfWeek,Boolean> mDaysOfWeek;
    private Map<Integer,Boolean> mDaysOfMonth;
    private List<Date> mDates;

    public enum DayOfWeek{
        SUNDAY,
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY
    }

    public Repeat(long everyPeriod, SpanOfTime repeatTime) {
        mEveryPeriod = everyPeriod;
        mRepeatTime = repeatTime;
        if(repeatTime.getTimeType()==SpanOfTime.Type.WEEK){
            mDaysOfWeek = new ArrayMap<>(7);
            for(DayOfWeek day: DayOfWeek.values()){
                mDaysOfWeek.put(day,false);
            }
            return;
        }
        if(repeatTime.getTimeType()==SpanOfTime.Type.MONTH){
            mDaysOfMonth = new ArrayMap<>(31);
            for(int i=1;i<=31;i++){
                mDaysOfMonth.put(i,false);
            }
            return;
        }
        if(repeatTime.getTimeType()==SpanOfTime.Type.YEAR){
            mDates = new ArrayList<>(366);
        }

    }


    public void toggleWeek(DayOfWeek dayOfWeek){
        boolean onOrOff = mDaysOfWeek.get(dayOfWeek);
        mDaysOfWeek.put(dayOfWeek,!onOrOff);
    }

    public void toggleDayOfMonth(int dayOfMonth){
        boolean onOrOff = mDaysOfMonth.get(dayOfMonth);
        mDaysOfMonth.put(dayOfMonth,!onOrOff);
    }
}
