package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;
import android.util.ArrayMap;

import com.adamwberck.android.makeareminder.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpanOfTime implements Serializable{
    private long mMinutes;
    private Type mTimeType;

    public long getTime(Type type) {
        switch (type) {
            case MINUTE: return getMinutes();
            case HOUR: return getHours();
            case DAY: return getDays();
            case WEEK: return getWeeks();
            default : return 0;
        }
    }

    public String getTimeString(Context context) {
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
            return context.getString(R.string.before_due_1,str0);
        }
        if(zeros==2){
            return context.getString(R.string.before_due_2,str0,str1);
        }
        if(zeros==1) {
            return context.getString(R.string.before_due_3,str0,str1,str2);
        }
        return context.getString(R.string.before_due_4,str0,str1,str2,str3);
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
        WEEK
    }

    public Type getTimeType() {
        return mTimeType;
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
        SpanOfTime span = new SpanOfTime(minutes);
        span.mTimeType=Type.MINUTE;
        return span;
    }

    public static SpanOfTime ofHours(long hours){
        SpanOfTime span = new SpanOfTime(TimeUnit.HOURS.toMinutes(hours));
        span.mTimeType=Type.HOUR;
        return span;
    }

    public static SpanOfTime ofDays(long days){
        SpanOfTime span = new SpanOfTime(TimeUnit.DAYS.toMinutes(days));
        span.mTimeType=Type.DAY;
        return span;
    }

    public static SpanOfTime ofWeeks(long weeks){
        SpanOfTime span = new SpanOfTime(TimeUnit.DAYS.toMinutes(weeks*7));
        span.mTimeType=Type.WEEK;
        return span;
    }
    private SpanOfTime(long minutes){
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
