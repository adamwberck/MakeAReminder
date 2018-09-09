package com.adamwberck.android.makeareminder.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;
import com.adamwberck.android.makeareminder.Service.StartDayService;
import com.adamwberck.android.makeareminder.Activity.TaskListActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaskListFragment extends VisibleFragment{

    //TODO sort tasks based on due date and completeness
    private static final String ARG_TASK = "task";
    private static final String ARG_ALARM = "alarm";
    private static final String ARG_NAME = "name";
    private static final String ARG_GROUP_ID = "group_id";
    
    private static final int REQUEST_SNOOZE = 0;
    private static final String DIALOG_ALARM = "Dialog_ALARM";
    private Group mGroup;
    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;

    private Callbacks mCallbacks;




    public static Fragment newInstance(Task task) {
        Bundle args =  new Bundle();
        args.putSerializable(ARG_TASK, task);

        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance(UUID uuid) {
        Bundle args =  new Bundle();
        TaskListFragment fragment = new TaskListFragment();
        args.putSerializable(ARG_GROUP_ID,uuid);

        fragment.setArguments(args);
        return fragment;
    }


    public interface Callbacks {
        void onTaskSelected(Task task);
    }

    private OnDeleteTaskListener mDeleteCallBack;

    public interface OnDeleteTaskListener {
        void onTaskDeleted(Task task);
    }


    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        setHasOptionsMenu(true);
        UUID id = (UUID) getArguments().getSerializable(ARG_GROUP_ID);
        mGroup = GroupLab.get(getContext()).getGroup(id);
        alterActionBar(mGroup.getColorInt(),getContext(),getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_tasklist,menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        styleMenuButtons(menu,mGroup.getColorInt(),getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_task:
                Task currentTask = ((TaskListActivity)getActivity()).getTask();
                if(currentTask!=null) {
                    String name = currentTask.getName();
                    if (name.isEmpty()) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                                R.string.name_task_warning, Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        newTask();
                    }
                }
                else {
                    newTask();
                }
                return true;
            case R.id.start_day:
                StartDayService.testServiceAlarm(getContext());
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newTask() {
        Task task = new Task(getContext(),mGroup);
        mGroup.addTask(task);
        mCallbacks.onTaskSelected(task);
        updateUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle saInSt){
        View v = inflater.inflate(R.layout.fragment_tasklist,container,false);
        super.setupUI(v);

        mTaskRecyclerView = v.findViewById(R.id.task_recycler_view);

        mTaskRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setTaskRecyclerViewItemTouchListener();

        updateUI();
        return v;
    }

    public void setTaskRecyclerViewItemTouchListener(){
        ItemTouchHelper.SimpleCallback itemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
                    @Override
                    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                        if (viewHolder != null){
                            final View foregroundView = ((TaskHolder) viewHolder).mForeground;

                            getDefaultUIUtil().onSelected(foregroundView);
                        }
                    }
                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                            int actionState, boolean isCurrentlyActive) {
                        final View foregroundView = ((TaskHolder) viewHolder).mForeground;

                        drawBackground(viewHolder, dX, actionState);

                        getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX, dY,
                                actionState, isCurrentlyActive);
                    }

                    @Override
                    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                                int actionState, boolean isCurrentlyActive) {
                        final View foregroundView = ((TaskHolder) viewHolder).mForeground;

                        drawBackground(viewHolder, dX, actionState);

                        getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
                                actionState, isCurrentlyActive);
                    }

                    @Override
                    public boolean onMove(RecyclerView recyclerView
                            ,RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Task task = mAdapter.mTasks.get(position);
                        mAdapter.notifyItemChanged(position);
                        task.toggleComplete();
                    }

                    @Override
                    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
                        final View backgroundView = ((TaskHolder) viewHolder).mBackground;
                        final View foregroundView = ((TaskHolder) viewHolder).mForeground;

                        // TODO: should animate out instead. how?
                        backgroundView.setRight(0);

                        getDefaultUIUtil().clearView(foregroundView);
                    }

                    private void drawBackground(RecyclerView.ViewHolder viewHolder, float dX, int actionState) {
                        final View backgroundView = ((TaskHolder) viewHolder).mBackground;

                        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                            //noinspection NumericCastThatLosesPrecision
                            backgroundView.setRight((int) Math.max(dX, 0));
                        }
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mTaskRecyclerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        mDeleteCallBack = (OnDeleteTaskListener) context;
        try {
            Task task = (Task) getArguments().getSerializable(ARG_TASK);
            if(task==null){
                return;
            }
            mCallbacks.onTaskSelected(task);
        }
        catch (NullPointerException ignored){}
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCallBack = null;
    }

    public void updateUI() {
        List<Task> tasks = mGroup.getTasks();


        if (mAdapter == null) {
            mAdapter = new TaskAdapter(tasks);
            mTaskRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTasks(tasks);
            mAdapter.notifyDataSetChanged();
        }

    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mSwipeIcon;
        private TextView mSwipeText;
        private TextView mTitleTextView;
        private TextView mDateTextView;

        private Task mTask;
        public View mForeground;
        public View mBackground;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            super(inflater.inflate(viewType, parent, false));
            mForeground=itemView.findViewById(R.id.foreground);
            mBackground=itemView.findViewById(R.id.background);
            mSwipeText = mBackground.findViewById(R.id.background_swipe_text);
            mSwipeIcon = mBackground.findViewById(R.id.background_swipe_icon);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle("Delete Check");
                    alertDialog.setMessage("Are you sure you want to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mGroup.removeTask(mTask);
                                    dialog.dismiss();
                                    updateUI();
                                }
                            });
                    alertDialog.show();
                    updateUI();
                    return false;
                }
            });

            mTitleTextView = itemView.findViewById(R.id.task_title);
            mDateTextView = itemView.findViewById(R.id.task_due_date);
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onTaskSelected(mTask);
        }


        public void bind(Task task) {
            List<TextView> taskText = new ArrayList<>();
            taskText.add(mTitleTextView);
            taskText.add(mDateTextView);

            mTask = task;
            String name = mTask.getName();
            if(!name.isEmpty()) {
                mTitleTextView.setText(name);
                for(TextView t: taskText){
                    t.setTextColor(getResources().getColor(R.color.black));
                }
                String dateText;
                if(mTask.getSnoozeTime()!=null&&mTask.isOverdue()){
                    dateText=getString(R.string.snoozed_till,
                            mTask.getSnoozeTime().toString("d MMM YYYY hh:mm a"));
                }
                else {
                    dateText = mTask.getDate() == null ?
                            getString(R.string.no_date) :
                            mTask.getDate().toString("d MMM YYYY hh:mm a", Locale.getDefault());
                }
                mDateTextView.setText(dateText);
                mBackground.setBackgroundColor(getContext().getResources()
                        .getColor(R.color.lightGreen));
                mSwipeText.setText(R.string.complete);
                mSwipeIcon.setImageResource(R.drawable.ic_check);
                if(task.isComplete()){
                    for(TextView t: taskText){
                        t.setTextColor(getResources().getColor(R.color.darkGray));
                        String s = t.getText().toString();
                        t.setText(strikeThrough(s));
                    }
                    mBackground.setBackgroundColor(getContext().getResources()
                            .getColor(R.color.lightGray));
                    mSwipeText.setText(R.string.restore);
                    mSwipeIcon.setImageResource(R.drawable.ic_undo);
                }
                else if(task.isOverdue()){
                    for(TextView t: taskText){
                        t.setTextColor(getResources().getColor(R.color.red));
                    }
                }
                return;
            }
            mTitleTextView.setText(R.string.new_task);
            mDateTextView.setText("");
            for(TextView t: taskText){
                t.setTextColor(getResources().getColor(R.color.gray));
            }
        }

    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskHolder> {
        private List<Task> mTasks;

        public TaskAdapter(List<Task> tasks) {
            mTasks = tasks;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_task;
        }

        public void setTasks(List<Task> tasks) {
            mTasks = tasks;
        }



        @Override
        public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            return new TaskHolder(layoutInflater, parent, viewType);

        }

        @Override
        public void onBindViewHolder(TaskHolder holder, int position) {
            Task task = mTasks.get(position);
            holder.bind(task);
        }


        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }
}
