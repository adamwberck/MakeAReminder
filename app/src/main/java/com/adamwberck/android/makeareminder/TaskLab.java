package com.adamwberck.android.makeareminder;

import android.content.Context;

import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.Interval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class TaskLab implements Serializable{
    //public static final long MINUTE = 60000;
    //TODO Change to SQL Lite
    private static TaskLab sTaskLab;
    private List<Task> mTasks;
    private SpanOfTime mDefaultSnooze;
    private AtomicInteger mAtomicInteger = new AtomicInteger(Integer.MIN_VALUE);
    private transient Context mContext;
    private static final String FILE_NAME = "tasks.info";

    public static void saveLab() {
        try {
            Context context = sTaskLab.mContext;
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sTaskLab);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static TaskLab loadLab(Context context) throws IOException,ClassNotFoundException {
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        TaskLab taskLab = (TaskLab) ois.readObject();
        fis.close();
        ois.close();
        return taskLab;
    }


    public static TaskLab get(Context context){
        if (sTaskLab == null) {
            try {
                sTaskLab = loadLab(context);
                sTaskLab.mContext = context;
            } catch (IOException e) {
                sTaskLab = new TaskLab(context);
            }
            catch (ClassNotFoundException c){
                c.printStackTrace();
                sTaskLab = new TaskLab(context);
            }
        }
        return sTaskLab;
    }

    private TaskLab(Context context) {
        mTasks = new ArrayList<>();
        mContext = context;
    }

    public int getTaskIndex(Task task) {
        return mTasks.indexOf(task);
    }

    public void addTask(Task r){
        mTasks.add(r);
        saveLab();
    }

    public void updateTask(Task task) {
        /*
        int loc = mTasks.indexOf(task);
        mTasks.remove(loc);
        mTasks.add(loc,task);*/
    }

    public void removeTask(Task t){
        if(mTasks.remove(t)){
            ReminderService.cancelServiceAlarm(mContext,t.getID());
            saveLab();
        }
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

    public void removeUnnamed() {
        for(Task t: mTasks){
            if(t.getName().isEmpty()){
                removeTask(t);
            }
        }
    }

    public  int nextValue() {
        return mAtomicInteger.incrementAndGet();
    }
}
