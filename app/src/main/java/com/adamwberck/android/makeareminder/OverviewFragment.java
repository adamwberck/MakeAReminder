package com.adamwberck.android.makeareminder;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.adamwberck.android.makeareminder.Elements.Task;

import java.util.List;
import java.util.UUID;

public class OverviewFragment extends Fragment{
    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_ALARM = "alarm";
    private static final int REQUEST_SNOOZE = 0;
    private static final String DIALOG_ALARM = "Dialog_ALARM";
    private RecyclerView mTaskRecyclerView;
    private TaskAdapter mAdapter;

    private Callbacks mCallbacks;


    public void deleteTask(UUID taskId) {
        Task task = TaskLab.get(getActivity()).getTask(taskId);
        TaskLab.get(getActivity()).removeTask(task);
    }

    public static Fragment newInstance(UUID id, boolean isAlarmOn) {
        Bundle args =  new Bundle();
        args.putSerializable(ARG_TASK_ID, id);
        args.putBoolean(ARG_ALARM,isAlarmOn);

        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface Callbacks {
        void onTaskSelected(Task task);
    }

    private OnDeleteTaskListener mDeleteCallBack;

    public interface OnDeleteTaskListener {
        void onTaskIdSelected(UUID TaskID);
    }


    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        setHasOptionsMenu(true);
    }

    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_overview,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_task:
                Task currentTask = ((OverviewActivity)getActivity()).getTask();
                String name = "null";
                if(currentTask!=null){
                    name = currentTask.getName();
                }
                if(name==null||name.isEmpty()) {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(),
                            R.string.name_task_warning, Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    Task task = new Task(getContext());
                    TaskLab.get(getActivity()).addTask(task);
                    updateUI();
                    mCallbacks.onTaskSelected(task);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle saInSt){
        View v = inflater.inflate(R.layout.fragment_overview,container,false);

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
                    public boolean onMove(RecyclerView recyclerView
                            ,RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Task task = mAdapter.mTasks.get(position);
                        mDeleteCallBack.onTaskIdSelected(task.getID());
                        TaskLab.get(getActivity()).removeTask(task);
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
        UUID taskID = (UUID) getArguments().getSerializable(ARG_TASK_ID);
        if(taskID!=null) {
            Task task = TaskLab.get(getActivity()).getTask(taskID);
            boolean isAlarmOn = getArguments().getBoolean(ARG_ALARM);
            if (isAlarmOn) {
                FragmentManager manager = getFragmentManager();
                AlarmAlertFragment dialog = AlarmAlertFragment.newInstance(task);
                dialog.setTargetFragment(OverviewFragment.this, REQUEST_SNOOZE);
                dialog.show(manager, DIALOG_ALARM);
            }
            mCallbacks.onTaskSelected(task);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
        mDeleteCallBack = null;
    }

    public void updateUI() {
        TaskLab taskLab = TaskLab.get(getActivity());
        List<Task> tasks = taskLab.getTasks();


        if (mAdapter == null) {
            mAdapter = new TaskAdapter(tasks);
            mTaskRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTasks(tasks);
            mAdapter.notifyDataSetChanged();
        }

    }

    private class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTitleTextView;
        private TextView mDateTextView;

        private Task mTask;

        public TaskHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
            super(inflater.inflate(viewType, parent, false));
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
                                    TaskLab.get(getActivity()).removeTask(mTask);
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
        }

        @Override
        public void onClick(View view) {
            mCallbacks.onTaskSelected(mTask);
        }


        public void bind(Task task) {
            mTask = task;
            String name = mTask.getName();
            if(name!=null) {
                if(!name.equals("")) {
                    mTitleTextView.setText(name);
                    mTitleTextView.setTextColor(getResources().getColor(R.color.black));
                    return;
                }
            }
            mTitleTextView.setText(R.string.new_task);
            mTitleTextView.setTextColor(getResources().getColor(R.color.gray));
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
