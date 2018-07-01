package com.adamwberck.android.makeareminder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Repeat;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.Elements.Task;

import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;

public class TaskFragment extends VisibleFragment{
    private static final String TAG = "TaskFragment";
    private Task mTask;
    private Callbacks mCallbacks;
    private EditText mNameField;


    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int REQUEST_REMINDER = 3;
    private static final int REQUEST_EDIT = 4;
    private static final int REQUEST_SNOOZE = 5;
    private static final int REQUEST_REPEAT = 6;

    private static final String ARG_TASK_ID = "task_id";
    private static final String ARG_ALARM = "alarm";

    private static final String DIALOG_REMINDER = "DialogReminder";
    private static final String DIALOG_REPEAT = "DialogRepeat";
    private static final String DIALOG_DATE  = "DialogDate";
    private static final String DIALOG_PHOTO  = "DialogPhoto";
    private static final String DIALOG_TIME  = "DialogTime";
    private static final String DIALOG_ALARM = "DialogAlarm";

    private static final String EXTRA_TASK_CHANGED =
            "com.adamwberck.android.makeareminder.task_changed";
    private static final String EXTRA_TASK_ID =
            "com.adamwberck.android.makeareminder.task_id";
    private Button mDateButton;
    private Button mTimeButton;
    private Button mAddReminderButton;
    private ListView mReminderListView;
    private ReminderAdapter mReminderAdapter;
    private Button mRepeatButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        int taskID = getArguments().getInt(ARG_TASK_ID);
        mTask = TaskLab.get(getActivity()).getTask(taskID);
    }

    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_task,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_task:
                TaskLab.saveLab();
                mTask.startAlarm(getActivity().getApplicationContext());
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task, container,false);
        super.setupUI(v);
        mNameField = v.findViewById(R.id.task_name);
        if(mTask.getName()!=null) {
            mNameField.setText(mTask.getName());
        }
        mNameField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    mNameField.clearFocus();
                    hideSoftKeyboard(getActivity());

                    return true;
                }
                return false;
            }
        });
        mNameField.addTextChangedListener(new TextWatcher() {
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

        //TODO fix the button so the dialog starts with the time that's on the button
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

        mRepeatButton = v.findViewById(R.id.repeat_button);
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO make generic method to do this
                FragmentManager manager = getFragmentManager();
                SetRepeatFragment dialog = SetRepeatFragment.newInstance(mTask.getRepeat());
                dialog.setTargetFragment(TaskFragment.this,REQUEST_REPEAT);
                dialog.show(manager,DIALOG_REPEAT);
            }
        });

        mReminderAdapter = new ReminderAdapter(getContext(),mTask.getReminders());
        mReminderListView = v.findViewById(R.id.reminder_list_view);
        mReminderListView.setAdapter(mReminderAdapter);


        mAddReminderButton = v.findViewById(R.id.add_reminder_button);
        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
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

    private void showDialog(Class c,int requestCode,String dialogString) {
        try {
            FragmentManager manager = getFragmentManager();
            DialogFragment dialog = (DialogFragment) c.newInstance();
            dialog.setTargetFragment(TaskFragment.this,requestCode);
            dialog.show(manager,dialogString);
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateDate() {
        if(mTask.getDate()!=null) {
            mDateButton.setText(mTask.getDate().toString("mm/dd/yyyy"));
            mTimeButton.setText(mTask.getDate().toString("hh:mm a"));
        }
        else {
            mDateButton.setText(R.string.set_date);
            mTimeButton.setText(R.string.set_time);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        View v = activity.getCurrentFocus();
        if(v!=null) {
            v.clearFocus();
        }
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
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
        if(mCallbacks!=null) {
            mCallbacks.onTaskUpdated(mTask);
        }
    }

    public Task getTask() {
        return mTask;
    }


    public interface Callbacks{
        void onTaskUpdated(Task task);
    }

    public interface OnDeleteTaskListener {
        void onTaskIdSelected(int TaskID);
    }

    public static TaskFragment newInstance(int taskID) {
        Bundle args =  new Bundle();
        args.putSerializable(ARG_TASK_ID, taskID);
        args.putBoolean(ARG_ALARM,false);

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
            //TODO ViewHolder
            @SuppressLint("ViewHolder")
            View rowView = mInflater.inflate(R.layout.list_reminder, parent, false);
            TextView infoTextView = rowView.findViewById(R.id.text_reminder_info);

            /*
            ViewHolder holder = new ViewHolder();
            holder.text = (TextView) convertView.findViewById(R.id.text_reminder_info);
            holder.button = (ImageButton) convertView.findViewById(R.id.image_button_delete);
            convertView.setTag(holder);*/
            final Reminder reminder = (Reminder) getItem(position);
            //TODO Swipe dismiss reminder
            infoTextView.setText(reminder.getInfo(getContext()));
            infoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    CreateReminderFragment dialog = CreateReminderFragment.newInstance(reminder);
                    dialog.setTargetFragment(TaskFragment.this, REQUEST_EDIT);
                    dialog.show(manager,DIALOG_REMINDER);
                }
            });
            ImageView isAlarm = rowView.findViewById(R.id.image_is_alarm);
            if(reminder.isAlarm()) {
                isAlarm.setImageDrawable(getResources().getDrawable(R.drawable.ic_alarm));
            }
            else {
                isAlarm.setImageDrawable(getResources().getDrawable(R.drawable.ic_notification));
            }
            ImageView delete = rowView.findViewById(R.id.image_delete_reminder);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTask.removeReminder(reminder);
                    updateUI();
                }
            });

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
    public void onDestroy(){
        TaskLab.get(getContext()).removeUnnamed();
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateUI();
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_REMINDER || requestCode == REQUEST_EDIT){
            SpanOfTime span = (SpanOfTime) data
                    .getSerializableExtra(CreateReminderFragment.EXTRA_NEW_REMINDER);
            boolean isAlarm = data.getExtras().getBoolean(CreateReminderFragment.EXTRA_IS_ALARM);
            if(requestCode==REQUEST_EDIT){
                Reminder reminder = (Reminder) data
                        .getSerializableExtra(CreateReminderFragment.EXTRA_DELETE_REMINDER);
                mTask.removeReminder(reminder);
            }
            mTask.addReminder(span,isAlarm);
            return;
        }
        if(requestCode == REQUEST_DATE || requestCode == REQUEST_TIME){
            DateTime date;
            if(requestCode == REQUEST_TIME) {
                date = (DateTime) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            }
            else {
                date = (DateTime) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            }
            mTask.setDate(date);
            updateDate();
            return;
        }
        if(requestCode == REQUEST_REPEAT){
            Repeat repeat = (Repeat) data.getSerializableExtra(SetRepeatFragment.EXTRA_REPEAT);
            mTask.setRepeat(repeat);
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
