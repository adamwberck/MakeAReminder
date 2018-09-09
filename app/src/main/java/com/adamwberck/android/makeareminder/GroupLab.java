package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.util.Log;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.LocalTime;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class GroupLab implements Serializable{
    private final static String TAG = "GroupLab";
    private static final Random RANDOM = new Random();

    private static List<String> sGroupColors;
    private static List<Integer> sColorUsed = new ArrayList<>(12);


    //public static final long MINUTE = 60000;
    //TODO add settings page
    private LocalTime mStartOfDay = new LocalTime(3,0);
    //TODO Change to SQL Lite
    private static GroupLab sGroupLab;
    private List<Group> mGroups;
    private transient Context mContext;
    private static final String FILE_NAME = "group.info";
    private AtomicInteger mAtomicInteger = new AtomicInteger(Integer.MIN_VALUE);

    public static void saveLab() {
        try {
            Context context = sGroupLab.mContext;
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(sGroupLab);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static GroupLab loadLab(Context context) throws IOException,ClassNotFoundException {
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(fis);
        GroupLab taskLab = (GroupLab) ois.readObject();
        fis.close();
        ois.close();
        return taskLab;
    }


    public static GroupLab get(Context context){
        if (sGroupLab == null) {
            try {
                sGroupLab = loadLab(context);
                sGroupLab.mContext = context;
            } catch (IOException e) {
                Log.i(TAG, "IOException");
                sGroupLab = new GroupLab(context);
            } catch (ClassNotFoundException c) {
                c.printStackTrace();
                Log.i(TAG, "Class not found exception");
                sGroupLab = new GroupLab(context);
            }
            sGroupLab = new GroupLab(context);
            sGroupLab.mGroups.add(Group.newSpecialGroup());

            //test
            Group group = new Group();
            Task task = new Task(context,group);
            task.setName("Eat a Cookie");
            group.addTask(task);
            sGroupLab.mGroups.add(sGroupLab.mGroups.size()-1,group);
        }
        return sGroupLab;
    }

    private GroupLab(Context context) {
        mGroups = new ArrayList<>();
        mContext = context;
        if(sGroupColors==null){
            initGroupColors();
        }
    }

    public int getGroupIndex(Group group) {
        return mGroups.indexOf(group);
    }

    public void addGroup(Group g){
        if(sGroupColors.size()==0){
            initGroupColors();
        }
        String color = sGroupColors.remove(sGroupColors.size()-1);
        g.setColor(color);
        mGroups.add(mGroups.size()-1,g);
        if(!mGroups.get(mGroups.size()-1).getID().equals(Group.SPECIAL_ID)){
            mGroups.remove(Group.newSpecialGroup());
            mGroups.add(Group.newSpecialGroup());
        }
        saveLab();
    }

    private void initGroupColors() {
        List<String> gc =
                Arrays.asList(mContext.getResources().getStringArray(R.array.group_colors));
        sGroupColors = new ArrayList<>();
        sGroupColors.addAll(gc);
        Collections.shuffle(sGroupColors);
    }

    public void updateTask(Task task) {
        /*
        int loc = mTasks.indexOf(task);
        mTasks.remove(loc);
        mTasks.add(loc,task);*/
    }

    public void removeGroup(Group g){
        if(mGroups.remove(g)){
            saveLab();
        }
    }


    public List<Group> getGroups() {
        return mGroups;
    }

    public Group getGroup(UUID id){
        for(Group g: mGroups){
            if(g.getID().equals(id)){
                return g;
            }
        }
        return null;
    }

    public void removeUnnamed() {
        //TODO remove Unnamed tasks or groups
    }

    public boolean removeTask(Task task){
        return getTask(task.getID())!=null;
    }

    public long getStartDay() {
        return mStartOfDay.toDateTimeToday().getMillis();
        //return new DateTime().plusMinutes(1).getMillis();
    }

    public  int nextValue() {
        return mAtomicInteger.incrementAndGet();
    }

    public Task getTask(int taskID) {
        for (Group g : mGroups){
            List<Task> tasks = g.getTasks();
            for(Task t: tasks){
                if(taskID == t.getID() ){
                    return t;
                }
            }
        }
        return null;
    }

    public List<Task> getTasks() {
        List<Task> tasks = new LinkedList<>();
        for (Group g: mGroups){
            try {
                tasks.addAll(g.getTasks());
            }
            catch (NullPointerException ignored){}
        }
        return tasks;
    }

}
