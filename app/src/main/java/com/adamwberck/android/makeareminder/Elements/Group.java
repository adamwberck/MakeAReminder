package com.adamwberck.android.makeareminder.Elements;

import android.content.Context;
import android.graphics.Color;

import com.adamwberck.android.makeareminder.GroupLab;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Group implements Serializable{
    public static final UUID SPECIAL_ID = new UUID(0,0);
    private UUID mID = UUID.randomUUID();
    private String mColor = "#ffa4fff9";
    private String mName = (mID.toString()).substring(0,6);
    private Repeat mDefaultRepeat;
    private long mDefaultSnooze;
    private DateTime mDefaultTime;
    private List<Reminder> mDefaultReminders = new SortedObjectList<>(10,
            Reminder.getComparator());

    private List<Task> mTasks = new ArrayList<>();

    public Group() {
    }

    private Group(UUID uuid) {
        mID = uuid;
    }

    public int getTaskIndex(Task task) {
        return mTasks.indexOf(task);
    }

    public void addTask(Task task){
        mTasks.add(task);
        //TODO ensure saving

    }

    public boolean removeTask(Task t){
        //if(mTasks.remove(t)){
            //ReminderService.cancelServiceAlarm(mContext,t.getID());
            //TODO cancel alarms
        //}
        return mTasks.remove(t);
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public Task getTask(int id){
        for(Task t: mTasks){
            if(t.getID()==id){
                return t;
            }
        }
        return null;
    }


    public String getName() {
        return mName;
    }

    public int getDueToday() {
        int total = 0;
        for(Task t : mTasks){
            if(t.isDueToday()){
                total++;
            }
        }
        return total;
    }

    public int getOverdue() {
        int total = 0;
        for(Task t: mTasks){
            if(t.isOverdue()){
                total++;
            }
        }
        return total;
    }

    public UUID getID() {
        return mID;
    }

    public boolean removeTask(int taskId) {
        for(Task t: mTasks){
            if(t.getID()==taskId){
                return mTasks.remove(t);
            }
        }
        return false;
    }

    public void setName(String name) {
        mName = name;
    }

    public DateTime getDefaultTime() {
        return mDefaultTime;
    }

    public Repeat getDefaultRepeat() {
        return mDefaultRepeat;
    }

    public List<Reminder> getDefaultReminders() {
        return mDefaultReminders;
    }

    public void removeReminder(Reminder reminder) {
        mDefaultReminders.remove(reminder);
    }

    public void addReminder(SpanOfTime span, boolean isAlarm) {
        Reminder r = new Reminder(span, isAlarm);
        addReminder(r);
    }

    public void addReminder(Reminder r){
        mDefaultReminders.add(r);
        GroupLab.saveLab();
    }

    public void setDefaultTime(DateTime defaultTime) {
        if(defaultTime!=null) {
            mDefaultTime = SpanOfTime.floorDate(defaultTime,1);
            addReminder(new Reminder(SpanOfTime.ofMinutes(0), true));
        }
        mDefaultTime = defaultTime;
    }

    public void setDefaultRepeat(Repeat defaultRepeat) {
        mDefaultRepeat = defaultRepeat;
    }

    public void setColor(String color) {
        mColor = color;
    }

    public String getColor() {
        return mColor;
    }

    public int getColorInt() {
        return Color.parseColor(getColor());
    }

    public static Group specialGroup() {
        return new Group(SPECIAL_ID);
    }

    public boolean isSpecial() {
        return mID.equals(SPECIAL_ID);
    }
}
