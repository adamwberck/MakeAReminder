package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;
import android.util.ArrayMap;

import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpanOfTime implements Serializable{
    //TODO Change to Interval
    //TODO Change Everything to JodaTime
    private long mMinutes;
    private Type mTimeType;


    public static DateTime floorDate(final DateTime dateTime, final int minutes) {
        if (minutes < 1 || 60 % minutes != 0) {
            throw new IllegalArgumentException("minutes must be a factor of 60");
        }

        final DateTime hour = dateTime.hourOfDay().roundFloorCopy();
        final long millisSinceHour = new Duration(hour, dateTime).getMillis();
        final int roundedMinutes = ((int)Math.floor(
                millisSinceHour / 60000.0 / minutes)) * minutes;
        return hour.plusMinutes(roundedMinutes);
    }

    public long getTime(Type type) {
        switch (type) {
            case MINUTE: return getMinutes();
            case HOUR: return getHours();
            case DAY: return getDays();
            case WEEK: return getWeeks();
            default : return 0;
        }
    }

    public String getTimeString(Context context,String startText, String endText) {
        Map<Type,Long> map = getTime();
        List<TimeResource> timeResources = new ArrayList<>(4);

        int weeks = (map.get(Type.WEEK)).intValue();
        String strWeeks = context.getResources()
                .getQuantityString(R.plurals.weeks,weeks,weeks);
        timeResources.add(new TimeResource(strWeeks,weeks));

        int days = (map.get(Type.DAY)).intValue();
        String strDays  = (context.getResources().getQuantityString(R.plurals.day,days,days));
        timeResources.add(new TimeResource(strDays,days));

        int hours =(map.get(Type.HOUR)).intValue();
        String strHours = (context.getResources()
                .getQuantityString(R.plurals.hour,hours,hours));
        timeResources.add(new TimeResource(strHours,hours));

        int minutes = map.get(Type.MINUTE).intValue();
        String strMinutes = context.getResources()
                .getQuantityString(R.plurals.minute,minutes,minutes);
        timeResources.add(new TimeResource(strMinutes,minutes));

        int zeros=0;
        Iterator<TimeResource> iterator = timeResources.iterator();
        while (iterator.hasNext()){
            TimeResource timeResource = iterator.next();
            if(timeResource.getTime()==0){
                zeros++;
                iterator.remove();
            }
        }
        String str0=null,str1=null,str2=null,str3=null;
        try {
            str0 = timeResources.get(0).getString();
            str1 = timeResources.get(1).getString();
            str2 = timeResources.get(2).getString();
            str3 = timeResources.get(3).getString();
        }
        catch (IndexOutOfBoundsException ignored){}
        if(zeros==3){
            return context.getString(R.string.time_text_1,startText,str0,endText);
        }
        if(zeros==2){
            return context.getString(R.string.time_text_2,startText,str0,str1,endText);
        }
        if(zeros==1) {
            return context.getString(R.string.time_text_3,startText,str0,str1,str2,endText);
        }
        return context.getString(R.string.time_text_4,startText,str0,str1,str2,str3,endText);
    }

    public Type getTimeType() {
        return mTimeType;
    }

    public long getMillis() {
        return TimeUnit.MINUTES.toMillis(mMinutes);
    }

    public static SpanOfTime ofMillis(long m) {
        return SpanOfTime.ofMinutes(TimeUnit.MILLISECONDS.toMinutes(m));
    }


    private class TimeResource{
        public TimeResource(String string, int time) {
            mString = string;
            mTime = time;
        }

        public String getString() {
            return mString;
        }

        public void setString(String string) {
            mString = string;
        }

        public int getTime() {
            return mTime;
        }



        private String mString;
        private int mTime;
    }

    public enum Type{
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    public Map<Type,Long> getTime(){

        Map<Type,Long> timeMap =  new ArrayMap<>();
        timeMap.put(Type.WEEK,getWeeks());
        timeMap.put(Type.DAY,getDays()-getWeeks()*7);
        timeMap.put(Type.HOUR,getHours()-getDays()*24);
        timeMap.put(Type.MINUTE,getMinutes()-getHours()*60);
        return timeMap;
    }

    public static SpanOfTime ofMinutes(long minutes){
        return new SpanOfTime(minutes,Type.MINUTE);
    }

    public static SpanOfTime ofHours(long hours){
        return new SpanOfTime(TimeUnit.HOURS.toMinutes(hours),Type.HOUR);

    }

    public static SpanOfTime ofDays(long days){
        return new SpanOfTime(TimeUnit.DAYS.toMinutes(days),Type.DAY);
    }

    public static SpanOfTime ofWeeks(long weeks){
        return new SpanOfTime(TimeUnit.DAYS.toMinutes(weeks*7),Type.WEEK);
    }

    public static SpanOfTime ofMonths(long months){
        return new SpanOfTime(TimeUnit.DAYS.toMinutes(months*30),Type.MONTH);
    }


    public static SpanOfTime ofYears(long years){
        return new SpanOfTime(TimeUnit.DAYS.toMinutes(years*365),Type.YEAR);
    }

    private SpanOfTime(long minutes,Type type){
        mTimeType = type;
        mMinutes = minutes;
    }
    public long getMinutes(){
        return mMinutes;
    }

    public long getHours(){
        return TimeUnit.MINUTES.toHours(getMinutes());
    }

    public long getDays(){
        return TimeUnit.MINUTES.toDays(mMinutes);
    }

    public long getWeeks(){
        return TimeUnit.MINUTES.toDays(mMinutes)/7;
    }

}
