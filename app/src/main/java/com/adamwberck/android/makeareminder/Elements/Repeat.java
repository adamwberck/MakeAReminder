package com.adamwberck.android.makeareminder.Elements;

import com.adamwberck.android.makeareminder.SortedObjectList;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Repeat implements Serializable{
    private long mRawPeriod;
    private SpanOfTime mRepeatTime;
    private List<Integer> mDaysOfTheWeek = new SortedObjectList<>(7,
            new IntComparator());
    private List<Integer> mDaysOfMonth = new SortedObjectList<>(31,
            new IntComparator());
    private List<DateTime> mDates;
    private List<LocalTime> mTimes = new SortedObjectList<LocalTime>(
            10,new TimeComparator());


    private class IntComparator implements Comparator<Integer>, Serializable{
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o1,o2);
        }
    }

    public boolean isMoreOften(){
        return mTimes.size()!=0&&getTimeType()== SpanOfTime.Type.DAY;
    }

    public void addTime(LocalTime localTime){
        mTimes.add(localTime);
    }
    public void removeTime(LocalTime localTime){
        mTimes.remove(localTime);
    }

    public boolean isRepeatOnWeekDay(int position) {
        return mDaysOfTheWeek.contains(position);
    }

    public boolean isRepeatOnMonthDay(int i) {
        return mDaysOfMonth.contains(i);
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

    public SpanOfTime getRepeatTime() {
        return mRepeatTime;
    }

    public LocalTime getSoonestTime() {
        try {
            for (LocalTime time : mTimes) {
                DateTime date = time.toDateTimeToday();
                if(date.isAfterNow()){
                    return time;
                }
            }
        }
        catch (NullPointerException e){
            return null;
        }
        return null;
    }

    public List<Integer> getDayOfWeekNumbers() {
        return mDaysOfTheWeek;
    }

    public List<Integer> getMonthDays() {
        return mDaysOfMonth;
    }


    public Repeat(long everyPeriod, SpanOfTime repeatTime) {
        mRawPeriod = everyPeriod;
        mRepeatTime = repeatTime;
        mDates = new ArrayList<>(366);
    }


    public void toggleWeek(int dayOfWeek){
        boolean onOrOff = mDaysOfTheWeek.contains(dayOfWeek);
        if(onOrOff){
            for(int i=0;i<mDaysOfTheWeek.size();i++){
                int day = mDaysOfTheWeek.get(i);
                if(day==dayOfWeek){
                    mDaysOfTheWeek.remove(i);
                }
            }
        }
        else {
            mDaysOfTheWeek.add(dayOfWeek);
        }
    }

    public void toggleDayOfMonth(int dayOfMonth){
        boolean onOrOff = mDaysOfTheWeek.contains(dayOfMonth);
        if(onOrOff){
            mDaysOfTheWeek.remove(dayOfMonth);
        }
        else {
            mDaysOfTheWeek.add(dayOfMonth);
        }
    }

    private class TimeComparator implements Comparator<LocalTime>, Serializable {

        @Override
        public int compare(LocalTime o1, LocalTime o2) {
            if(o1.isEqual(o2)){
                return 0;
            }
            return o1.isBefore(o2)?1:-1;
        }
    }
}
