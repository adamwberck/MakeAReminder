package com.adamwberck.android.makeareminder.Elements;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class SpanOfTime implements Serializable{
    private long mMinutes;
    private Type mTimeType;


    public enum Type{
        MINUTE,
        HOUR,
        DAY,
        WEEK
    }

    public Type getTimeType() {
        return mTimeType;
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

    private SpanOfTime(long minutes) {
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

    public long getMonths(){
        //TODO
        return 0;
    }
}
