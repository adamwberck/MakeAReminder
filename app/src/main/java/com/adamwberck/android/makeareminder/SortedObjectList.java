package com.adamwberck.android.makeareminder;



import android.support.annotation.NonNull;

import com.adamwberck.android.makeareminder.Elements.Reminder;

import org.joda.time.DateTimeComparator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SortedObjectList<T> extends ArrayList<T> implements Serializable{
    private Comparator<T> mComparator;
    public SortedObjectList(int initialCapacity, Comparator<T> comparator) {
        super(initialCapacity);
        mComparator = comparator;
    }

    public boolean hasComparator(){
        return mComparator!=null;
    }



    @Override
    public boolean add(T item){
        if(contains(item)){
            return false;
        }
        else {
            for(int i=0;i<this.size();i++){
                if(mComparator.compare(get(i),item)<0){
                    super.add(i,item);
                    return true;
                }
            }
            return super.add(item);
        }
    }

    @Override
    public void add(int index,T item){
    }

    @Override
    public boolean contains(Object o){
        for(int i=0;i<size();i++){
            if(o.equals(get(i))){
                return true;
            }
        }
        return false;
    }
}
