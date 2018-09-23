package com.adamwberck.android.makeareminder.Elements;

import android.content.res.Resources;

import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class Repeat implements Serializable{
    private long mRawPeriod;
    private SpanOfTime mRepeatTime;
    private List<Integer> mDaysOfTheWeek = new SortedObjectList<>(7,
            new IntComparator());

    private List<Integer> mDaysOfMonth = new SortedObjectList<>(31,
            new IntComparator());
    private boolean mLastDayOfMonth = false;

    private List<DateTime> mDates;
    private List<LocalTime> mTimes = new SortedObjectList<LocalTime>(
            10,new TimeComparator());

    public void setLastDayOfMonth(boolean lastDayOfMonth) {
        mLastDayOfMonth = lastDayOfMonth;
    }

    public boolean isLastDayOfMonth() {
        return mLastDayOfMonth;
    }


    public String displayString(Resources r){
        SpanOfTime.Type type = mRepeatTime.getTimeType();
        final String EVERY = r.getString(R.string.every);
        final String REGULAR_SEPARATOR = r.getString(R.string.reg_separator);
        final String FINAL_SEPARATOR = r.getString(R.string.final_separator);
        int rawInt = (int) mRawPeriod;
        String out;
        switch (type){
            case DAY:
                String day = r.getQuantityString(R.plurals.day,rawInt,rawInt);
                out = r.getString(R.string.time_text_1,EVERY,day,"");
                if(isMoreOften()){
                    final String AT = r.getString(R.string.at);
                    StringBuilder moreOftenTimes= new StringBuilder();
                    for(int i=0;i<mTimes.size()-1;i++){
                        LocalTime time = mTimes.get(i);
                        moreOftenTimes.append(time.toString("h:mm a", Locale.getDefault()))
                                .append(REGULAR_SEPARATOR);
                    }
                    LocalTime time = mTimes.get(mTimes.size()-1);
                    moreOftenTimes.append(time.toString("h:mm a", Locale.getDefault()));
                    //out Every X days at 3:00 pm 4:00 pm
                    return r.getString(R.string.time_text_1,out.trim(),AT,
                            moreOftenTimes.toString());
                }
                else {
                    //out Every X days
                    return out.trim();
                }
            case WEEK:
                //Assemble Week Names
                StringBuilder dayNames = new StringBuilder();
                String[] days = r.getStringArray(R.array.days_of_the_week);
                for (int i = 0; i < mDaysOfTheWeek.size() - 2; i++) {
                    int dayOfWeek = mDaysOfTheWeek.get(i);

                    String dayStr = days[dayOfWeek];
                    dayNames.append(dayStr).append(REGULAR_SEPARATOR);
                }
                //Add an '&'
                if(mDaysOfTheWeek.size()>1){
                    String dayStr = days[mDaysOfTheWeek.get(mDaysOfTheWeek.size()-2)];
                    dayNames.append(dayStr).append(FINAL_SEPARATOR);
                }
                String dayStr = days[mDaysOfTheWeek.get(mDaysOfTheWeek.size()-1)];
                dayNames.append(dayStr);

                //Every X-day
                if(rawInt<2){
                    out = r.getString(R.string.time_text_1,EVERY,dayNames,"");
                    return out;
                }
                //Every X weeks on a,b,c
                else {
                    String week = r.getQuantityString(R.plurals.weeks, rawInt, rawInt);
                    out = r.getString(R.string.time_text_1, EVERY, week, "");
                    final String ON = r.getString(R.string.on);
                    //out Every X days at 3:00 pm 4:00 pm
                    return r.getString(R.string.time_text_1, out.trim(), ON, dayNames.toString());
                }
            case MONTH:
                //Assemble Week Names
                StringBuilder dayNumbers = new StringBuilder();
                int size = mDaysOfMonth.size();
                //Add all but last two
                for (int i = 0; i < size - 2; i++) {
                    String dayOrdinal = SpanOfTime.ordinal(mDaysOfMonth.get(i));
                    dayNumbers.append(dayOrdinal).append(REGULAR_SEPARATOR);
                }
                if(mLastDayOfMonth){
                    //add last two (or one)
                    if (size >= 2) {
                        String dayOrdinal = SpanOfTime.ordinal(mDaysOfMonth.get(size - 2));
                        dayNumbers.append(dayOrdinal).append(REGULAR_SEPARATOR);
                    }
                    String dayOrdinal = SpanOfTime.ordinal(mDaysOfMonth.get(size - 1));
                    dayNumbers.append(dayOrdinal).append(FINAL_SEPARATOR);
                    dayNumbers.append(r.getString(R.string.the_last_day));
                }
                //Add an '&'
                else {
                    if (size >= 2) {
                        String dayOrdinal = SpanOfTime.ordinal(mDaysOfMonth.get(size - 2));
                        dayNumbers.append(dayOrdinal).append(FINAL_SEPARATOR);
                    }
                    //Add the final
                    String dayOrdinal = SpanOfTime.ordinal(mDaysOfMonth.get(size - 1));
                    dayNumbers.append(dayOrdinal);
                }

                //Determine plurality
                String month = r.getQuantityString(R.plurals.months, rawInt, rawInt);
                out = r.getString(R.string.time_text_1, EVERY, month, "");

                //Every X month on the a,b,c
                final String ON_THE = r.getString(R.string.on_the);
                return r.getString(R.string.time_text_1, out.trim(), ON_THE, dayNumbers.toString());
            case YEAR:
                //TODO choose dates
                String year = r.getQuantityString(R.plurals.years,rawInt,rawInt);
                return r.getString(R.string.time_text_1,EVERY,year,"").trim();
        }
        return "null";
    }

    public void toggleLastDay() {
        if(mDaysOfMonth.size()>0) {
            mLastDayOfMonth = !mLastDayOfMonth;
        }
    }

    public void setRawPeriod(long raw) {
        mRawPeriod = raw;
    }


    private static class IntComparator implements Comparator<Integer>, Serializable{
        @Override
        public int compare(Integer o1, Integer o2) {
            return Integer.compare(o2,o1);
        }
    }

    public boolean isMoreOften(){
        return mTimes.size()>0&&getTimeType()== SpanOfTime.Type.DAY;
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

    public void setRepeatTime(SpanOfTime span,long raw) {
        mRawPeriod = raw;
        mRepeatTime = span;
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
        DateTime now = new DateTime();
        toggleWeek((now.getDayOfWeek()==7)? 0 : now.getDayOfWeek());
        toggleDayOfMonth(now.getDayOfMonth());
    }


    public void toggleWeek(int dayOfWeek){
        boolean onOrOff = mDaysOfTheWeek.contains(dayOfWeek);
        if(onOrOff){
            if(mDaysOfTheWeek.size()>1) {
                mDaysOfTheWeek.remove(Integer.valueOf(dayOfWeek));
            }
        }
        else {
            mDaysOfTheWeek.add(dayOfWeek);
        }
    }

    public void toggleDayOfMonth(int dayOfMonth){
        boolean onOrOff = mDaysOfMonth.contains(dayOfMonth);
        if(onOrOff){
            if(mDaysOfMonth.size()>1||mLastDayOfMonth) {
                mDaysOfMonth.remove(Integer.valueOf(dayOfMonth));
            }
        }
        else {
            mDaysOfMonth.add(dayOfMonth);
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
