package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.List;

public class OverviewFragment extends VisibleFragment {

    private RecyclerView mGroupRecyclerView;
    private Callbacks mCallbacks;
    private GroupAdapter mAdapter;

    public interface Callbacks {
        void onGroupSelected(Group group);
    }

    public static Fragment newInstance() {
        Bundle args =  new Bundle();

        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis){
        View v = inflater.inflate(R.layout.fragment_overview,container,false);

        mGroupRecyclerView = v.findViewById(R.id.group_recycler_view);
        mGroupRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
        //mGroupRecyclerView.setAdapter(new GroupAdapter(mGroups));
        return v;
    }

    public void setTaskRecyclerViewItemTouchListener(){
        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView
                            ,RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        ///Group group = mAdapter.mTasks.get(position);
                        //mDeleteCallBack.onTaskIdSelected(task.getID());
                        //TaskLab.get(getActivity()).removeTask(task);
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mGroupRecyclerView);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_overview,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_task:
                //TODO create drag drop
                return super.onOptionsItemSelected(item);
            case R.id.new_group:
                newGroup();
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (OverviewFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    private void newGroup() {

    }

    private void updateUI(){
        //TODO add GroupLab replace TaskLab
        /*
        TaskLab taskLab = TaskLab.get(getActivity());
        List<Task> tasks = taskLab.getTasks();


        if (mAdapter == null) {
            mAdapter = new TaskListFragment.TaskAdapter(tasks);
            mTaskRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTasks(tasks);
            mAdapter.notifyDataSetChanged();
        }
        */
    }

    private class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mGroupNameTextView;
        private TextView mDueTextView;
        private TextView mOverdueTextView;
        private Group mGroup;


        private GroupHolder(LayoutInflater inflater, ViewGroup parent,int viewType){
            super(inflater.inflate(viewType,parent,false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //TODO add group edit
                    return false;
                }
            });
            itemView.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    //TODO add drag rearrange
                    return false;
                }
            });


            mGroupNameTextView = itemView.findViewById(R.id.group_name);
            mDueTextView = itemView.findViewById(R.id.num_task_due);
            mOverdueTextView = itemView.findViewById(R.id.num_task_overdue);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.onGroupSelected(mGroup);
        }

        public void bind(Group group){
            mGroup = group;

            mGroupNameTextView.setText(mGroup.getName());
            mDueTextView.setText(getResources().getString(R.string.group_due,mGroup.getDueToday()));
            mOverdueTextView.setText(getResources().getString(R.string.group_overdue,
                    mGroup.getOverdue()));


        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {
        private List<Group> mGroups;

        public GroupAdapter(List<Group> groups) {
            mGroups = groups;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_task;
        }
        /*
        public void setGroups(List<Group> groups) {
            mGroups = groups;
        }*/


        @NonNull
        @Override
        public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new GroupHolder(layoutInflater,parent,viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
            Group group =  mGroups.get(position);
            holder.bind(group);
        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }
    }
}
