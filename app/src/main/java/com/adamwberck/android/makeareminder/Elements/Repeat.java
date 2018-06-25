package com.adamwberck.android.makeareminder.Elements;

import android.support.v4.util.SparseArrayCompat;
import android.util.ArrayMap;

import com.adamwberck.android.makeareminder.SortedObjectList;

import org.joda.time.DateTimeComparator;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Repeat implements Serializable{
    private long mRawPeriod;
    private SpanOfTime mRepeatTime;
    private Map<DayOfWeek,Boolean> mDaysOfWeek = new ArrayMap<>(7);
    //private Map<Integer,Boolean> mDaysOfMonth = new ArrayMap<>(31);
    private SparseArrayCompat<Boolean> mDaysOfMonth = new SparseArrayCompat<>(31);
    private List<Date> mDates;
    private List<LocalTime> mTimes = new SortedObjectList<LocalTime>(
            10,new TimeComparator());

    public void addTime(LocalTime localTime){
        mTimes.add(localTime);
    }
    public void removeTime(LocalTime localTime){
        mTimes.remove(localTime);
    }
    public boolean isWeek(int position) {
        return isWeek(getDayOfWeek(position));
    }

    public boolean isWeek(DayOfWeek dayOfWeek) {
        return mDaysOfWeek.get(dayOfWeek);
    }

    public boolean isMonth(int i) {
        return mDaysOfMonth.get(i);
    }

    public List<LocalTime> getTimes() {
        return mTimes;
    }

    public long getRawPeriod() {
        return mRawPeriod;
    }

    public SpanOfTime.Type getTimeType() {
        return mRepeatTime.getTimeType();
    }

    public void setRepeatTime(SpanOfTime timeType) {
        mRepeatTime = timeType;
    }


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
        mRawPeriod = everyPeriod;
        mRepeatTime = repeatTime;

        for(DayOfWeek day: DayOfWeek.values()){
            mDaysOfWeek.put(day,false);
        }
        for(int i=1;i<=31;i++){
            mDaysOfMonth.put(i,false);
        }
        mDates = new ArrayList<>(366);
    }


    public void toggleWeek(DayOfWeek dayOfWeek){
        boolean onOrOff = mDaysOfWeek.get(dayOfWeek);
        mDaysOfWeek.put(dayOfWeek,!onOrOff);
    }

    public void toggleWeek(int num){
        toggleWeek(getDayOfWeek(num));
    }

    private static DayOfWeek getDayOfWeek(int num) {
        switch (num){
            case 0: return DayOfWeek.SUNDAY;
            case 1: return DayOfWeek.MONDAY;
            case 2: return DayOfWeek.TUESDAY;
            case 3: return DayOfWeek.WEDNESDAY;
            case 4: return DayOfWeek.THURSDAY;
            case 5: return DayOfWeek.FRIDAY;
            case 6: return DayOfWeek.SUNDAY;
        }
        return null;
    }

    public void toggleDayOfMonth(int dayOfMonth){
        boolean onOrOff = mDaysOfMonth.get(dayOfMonth);
        mDaysOfMonth.put(dayOfMonth,!onOrOff);
    }

    private class TimeComparator implements Comparator<LocalTime> {

        @Override
        public int compare(LocalTime o1, LocalTime o2) {
            if(o1.isEqual(o2)){
                return 0;
            }
            return o1.isBefore(o2)?-1:1;
        }
    }
}
