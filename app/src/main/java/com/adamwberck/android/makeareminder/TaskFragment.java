package com.adamwberck.android.makeareminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class TaskFragment extends Fragment{
    private static final String DIALOG_REMINDER = "DialogReminder";
    private Task mTask;
    private Callbacks mCallbacks;
    private EditText mTitleField;


    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_REMINDER = 3;


    private static final String ARG_TASK_ID = "task_id";
    private static final String DIALOG_DATE  = "DialogDate";
    private static final String DIALOG_PHOTO  = "DialogPhoto";
    private static final String DIALOG_TIME  = "DialogTime";

    private static final String EXTRA_TASK_CHANGED =
            "com.adamwberck.android.makeareminder.task_changed";
    private static final String EXTRA_TASK_ID =
            "com.adamwberck.android.makeareminder.task_id";
    private Button mDateButton;
    private Button mTimeButton;
    private Button mReminderButton;
    private ListView mReminderListView;
    private ReminderAdapter mReminderAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID taskID = (UUID) getArguments().getSerializable(ARG_TASK_ID);
        mTask = TaskLab.get(getActivity()).getTask(taskID);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task, container,false);

        mTitleField = v.findViewById(R.id.task_title);
        if(mTask.getName()!=null)
            mTitleField.setText(mTask.getName());

        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // pass
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTask.setName(s.toString());
                updateTask();
                setTaskChanged((TaskLab.get(getActivity()).getTaskIndex(mTask)));
            }

            @Override
            public void afterTextChanged(Editable s) {
                // pass
            }
        });

        mDateButton = v.findViewById(R.id.date_button);

        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);
            }
        });

        mTimeButton = v.findViewById(R.id.time_button);

        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment
                        .newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });
        updateDate();

        mReminderAdapter = new ReminderAdapter(getContext(),mTask.getReminders());
        mReminderListView = v.findViewById(R.id.reminder_list_view);
        mReminderListView.setAdapter(mReminderAdapter);


        mReminderButton = v.findViewById(R.id.add_reminder_button);
        mReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                CreateReminderFragment dialog = CreateReminderFragment.newInstance();
                dialog.setTargetFragment(TaskFragment.this, REQUEST_REMINDER);
                dialog.show(manager,DIALOG_REMINDER);
            }
        });

        return v;
    }

    private void updateDate() {
        String dateMed = DateFormat.getMediumDateFormat(getContext()).format(mTask.getDate());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mTask.getDate());

        int h = calendar.get(Calendar.HOUR);
        if(h==0) h=12;
        int m = calendar.get(Calendar.MINUTE);

        int ampm = calendar.get(Calendar.AM_PM);
        String[] AMPM = {"AM","PM"};

        DecimalFormat df = new DecimalFormat("00");

        mDateButton.setText(dateMed);
        mTimeButton.setText(h+":"+df.format(m)+" "+AMPM[ampm]);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
        TaskFragment taskFragment = (TaskFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        if(taskFragment !=null) {
            //mDeleteCallBack = (OnDeleteCrimeListener) context;
        }
    }

    private void setTaskChanged(int taskChanged){
        Intent data = new Intent();
        data.putExtra(EXTRA_TASK_CHANGED, taskChanged);
        getActivity().setResult(RESULT_OK, data);
    }

    private void updateTask() {
        TaskLab.get(getActivity()).updateTask(mTask);
        mCallbacks.onTaskUpdated(mTask);
    }

    public Task getTask() {
        return mTask;
    }


    public interface Callbacks{
        void onTaskUpdated(Task task);
    }

    public interface OnDeleteTaskListener {
        void onTaskIdSelected(UUID TaskID);
    }

    public static TaskFragment newInstance(UUID taskID) {
        Bundle args =  new Bundle();
        args.putSerializable(ARG_TASK_ID, taskID);

        TaskFragment fragment = new TaskFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private class ReminderAdapter extends BaseAdapter{
        private Context mContext;
        private LayoutInflater mInflater;
        private List<Reminder> mDataSource;

        private ReminderAdapter(Context context, List<Reminder> reminders){
            mContext = context;
            mDataSource = reminders;
            mInflater =(LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getCount() {
            return mDataSource.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataSource.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get view for row item
            View rowView = mInflater.inflate(R.layout.list_reminder, parent, false);
            TextView infoTextView = rowView.findViewById(R.id.text_reminder_info);

            /*
            ViewHolder holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text_reminder_info);
            holder.button = (ImageButton) convertView.findViewById(R.id.image_button_delete);
            convertView.setTag(holder);*/

            Reminder reminder = (Reminder) getItem(position);
            infoTextView.setText(reminder.getInfo());

            return rowView;
        }

    }
    /*
    private static class ViewHolder{
        TextView text;
        ImageButton button;
        int position;
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_REMINDER){
            SpanOfTime span = (SpanOfTime) data
                    .getSerializableExtra(CreateReminderFragment.EXTRA_SPAN);
            mTask.addReminder(span);
            updateUI();
        }
    }

    private void updateUI() {
        List<Reminder> reminders = mTask.getReminders();


        if (mReminderAdapter== null) {
            mReminderAdapter = new ReminderAdapter(getContext(),reminders);
            mReminderListView.setAdapter(mReminderAdapter);
        } else {
            mReminderListView.setAdapter(mReminderAdapter);
            mReminderAdapter.notifyDataSetChanged();
        }
    }
}
