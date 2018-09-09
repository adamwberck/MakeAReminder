package com.adamwberck.android.makeareminder.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Activity.AlarmActivity;
import com.adamwberck.android.makeareminder.Dialog.ChooseSnoozeDialog;
import com.adamwberck.android.makeareminder.Dialog.CreateReminderDialog;
import com.adamwberck.android.makeareminder.Dialog.DatePickerDialog;
import com.adamwberck.android.makeareminder.Dialog.SetRepeatDialog;
import com.adamwberck.android.makeareminder.Dialog.TimePickerDialog;
import com.adamwberck.android.makeareminder.Elements.Task;
import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Repeat;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TaskFragment extends VisibleFragment{
    private static final String TAG = "TaskFragment";

    private Task mTask;
    private Callbacks mCallbacks;
    private EditText mNameField;


    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_COLOR = 2;
    private static final int REQUEST_REMINDER = 3;
    private static final int REQUEST_EDIT = 4;
    private static final int REQUEST_SNOOZE = 5;
    private static final int REQUEST_REPEAT = 6;

    private static final String ARG_TASK_ID = "Task_id";

    private static final String DIALOG_COLOR = "DialogColor";
    private static final String DIALOG_REMINDER = "DialogReminder";
    private static final String DIALOG_REPEAT = "DialogRepeat";
    private static final String DIALOG_DATE  = "DialogDate";
    private static final String DIALOG_PHOTO  = "DialogPhoto";
    private static final String DIALOG_TIME  = "DialogTime";
    private static final String DIALOG_ALARM = "DialogAlarm";

    private static final String EXTRA_Tsk_CHANGED =
            "com.adamwberck.android.makeareminder.Tsk_changed";
    private static final String EXTRA_Tsk_ID =
            "com.adamwberck.android.makeareminder.Tsk_id";
    private Button mTimeButton;
    private Button mDateButton;

    private ListView mReminderListView;
    private ReminderAdapter mReminderAdapter;
    private Button mRepeatButton;
    private Button mQuickSnoozeButton;
    private ImageButton mColorButton;
    private ActionBar mActionBar;
    private Menu mMenu;
    private boolean mDueHasValue = false;
    private ImageButton mCloseDue;
    private View mDueLayout;

    private boolean mAlertHasValue = false;
    private View mAlertLayout;
    private ImageButton mCloseAlert;
    private Button mSnoozeInfoButton;
    private Spinner mGroupSpinner;
    private List<Group> mGroups;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        int ID = getArguments().getInt(ARG_TASK_ID);
        mTask = GroupLab.get(getContext()).getTask(ID);
        int colorInt = mTask.getGroup().getColorInt();
        alterActionBar(colorInt,getContext(),getActivity());
    }


    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.fragment_task,menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        styleMenuButtons(menu,mTask.getGroup().getColorInt(),getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_task:
                if(mTask.getName().isEmpty()){
                    getActivity().onBackPressed();
                }
                else {
                    mTask.startAlarm(getContext());
                    getActivity().finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SIState) {
        View v = inflater.inflate(R.layout.fragment_task, container,false);

        mGroups = GroupLab.get(getContext()).getGroups();

        mDueLayout = v.findViewById(R.id.due_layout);
        mAlertLayout = v.findViewById(R.id.alert_layout);

        mGroupSpinner = v.findViewById(R.id.group_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item_black);
        adapter.addAll(getGroupNames());
        mGroupSpinner.setAdapter(adapter);
        mGroupSpinner.setSelection(mGroups.indexOf(mTask.getGroup()));
        mGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Group lastGroup = mTask.getGroup();
                Group newGroup = mGroups.get(position);
                if(lastGroup.getID().equals(newGroup.getID())){
                    return;
                }
                //GroupLab.get(getContext()).getGroup(lastGroup.getID()).removeTask(mTask);
                //GroupLab.get(getContext()).getGroup(newGroup.getID()).addTask(mTask);
                lastGroup.removeTask(mTask);
                newGroup.addTask(mTask);
                mTask.setGroup(newGroup);
                int colorInt = newGroup.getColorInt();
                alterActionBar(colorInt,getContext(),getActivity());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSnoozeInfoButton = v.findViewById(R.id.snooze_info_text);
        mSnoozeInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Todo  change snooze
            }
        });
        mSnoozeInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = mTask.getID();
                String name = mTask.getName();
                String title = mTask.getName();
                Intent i = AlarmActivity.newIntent(getContext(),id,true,name,title);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i);
            }
        });


        mCloseDue = v.findViewById(R.id.close_due);
        mCloseDue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask.setDate(null);
                mTask.setRepeat(null);
                updateDate();
                updateUI();
            }
        });

        mCloseAlert = v.findViewById(R.id.close_alerts);
        mCloseAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTask.setQuickSnoozeTime(0);
                mTask.getReminders().clear();
                updateUI();
            }
        });

        super.setupUI(v);
        mNameField = v.findViewById(R.id.task_name);
        if(mTask.getName()!=null) {
            mNameField.setText(mTask.getName());
        }


        //TODO color button should be group change button





        mQuickSnoozeButton = v.findViewById(R.id.quick_snooze_text);
        mQuickSnoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = mTask.getID();
                String name = mTask.getName();
                String title = mTask.getName();
                //TODO create default snooze dialog or use one already made
                FragmentManager manager = getFragmentManager();
                ChooseSnoozeDialog d = ChooseSnoozeDialog.newInstance(null,mTask.getName());
                d.setTargetFragment(TaskFragment.this,REQUEST_SNOOZE);
                d.show(manager,DIALOG_ALARM);
            }
        });

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
            }

            @Override
            public void afterTextChanged(Editable s) {
                // pass
            }
        });

        mTimeButton = v.findViewById(R.id.time_text);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerDialog dialog = TimePickerDialog
                        .newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });

        mDateButton = v.findViewById(R.id.date_text);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerDialog dialog = DatePickerDialog
                        .newInstance(mTask.getDate());
                dialog.setTargetFragment(TaskFragment.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });

        updateDate();

        mRepeatButton = v.findViewById(R.id.set_repeat_text);
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO make generic method to do this
                FragmentManager manager = getFragmentManager();
                SetRepeatDialog dialog = SetRepeatDialog.newInstance(mTask.getRepeat());
                dialog.setTargetFragment(TaskFragment.this,REQUEST_REPEAT);
                dialog.show(manager,DIALOG_REPEAT);
            }
        });

        mReminderListView = v.findViewById(R.id.reminder_list_view);
        View footer = getLayoutInflater().inflate(R.layout.list_reminder_footer, mReminderListView,
                false);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add reminder button
                FragmentManager manager = getFragmentManager();
                CreateReminderDialog dialog = CreateReminderDialog.newInstance();
                dialog.setTargetFragment(TaskFragment.this, REQUEST_REMINDER);
                dialog.show(manager,DIALOG_REMINDER);
            }
        });


        mReminderListView.addFooterView(footer);

        updateUI();
        return v;
    }

    private List<String> getGroupNames() {
        List<String> strings = new ArrayList<>(mGroups.size());
        for(Group g : mGroups){
            if(!g.isSpecial()) {
                strings.add(g.getName());
            }
        }
        return strings;
    }

    private void updateDate() {
        if(mTask.getDate()!=null) {
            String time = mTask.getDate().toString("h:mm a");
            mTimeButton.setText(underline(time));

            time = mTask.getDate().toString("MMM d, yyyy");
            mDateButton.setText(underline(time));
        }
        else {
            mTimeButton.setText(R.string.set_time);
            mDateButton.setText(R.string.set_date);
        }
    }





    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
    }

    private void updateTask() {
        //TskLab.get(getActivity()).updateTsk(mTsk);
        if(mCallbacks!=null) {
            mCallbacks.onTaskUpdated(mTask.getID());
        }
    }

    public Task getTask() {
        return mTask;
    }


    public interface Callbacks{
        void onTaskUpdated(int TaskID);
    }


    public static TaskFragment newInstance(int id) {
        Bundle args =  new Bundle();
        args.putInt(ARG_TASK_ID, id);
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
            @SuppressLint("ViewHolder")
            View rowView = mInflater.inflate(R.layout.list_reminder, parent, false);
            final Reminder reminder = (Reminder) getItem(position);

            TextView infoTextView = rowView.findViewById(R.id.text_reminder_info);
            infoTextView.setText(reminder.getInfo(getContext()).trim());
            infoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    CreateReminderDialog dialog = CreateReminderDialog.newInstance(reminder);
                    dialog.setTargetFragment(TaskFragment.this, REQUEST_EDIT);
                    dialog.show(manager,DIALOG_REMINDER);
                }
            });

            ImageView imageView = rowView.findViewById(R.id.image_is_alarm);
            Resources r = getResources();
            Drawable icon = mDataSource.get(position).isAlarm()?
                    r.getDrawable(R.drawable.ic_alarm) :
                    r.getDrawable(R.drawable.ic_notification);
            imageView.setImageDrawable(icon);

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


    @Override
    public void onResume(){
        Log.i(TAG,"Resume");
        updateUI();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        //TskLab.get(getContext()).removeUnnamed();
        GroupLab.saveLab();
        Log.i(TAG,this.toString()+" destroyed.");
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_REMINDER || requestCode == REQUEST_EDIT){
            SpanOfTime span = (SpanOfTime) data
                    .getSerializableExtra(CreateReminderDialog.EXTRA_NEW_REMINDER);
            boolean isAlarm = data.getExtras().getBoolean(CreateReminderDialog.EXTRA_IS_ALARM);
            if(requestCode==REQUEST_EDIT){
                Reminder reminder = (Reminder) data
                        .getSerializableExtra(CreateReminderDialog.EXTRA_DELETE_REMINDER);
                mTask.removeReminder(reminder);
            }
            mTask.addReminder(span,isAlarm);
        }
        if(requestCode == REQUEST_DATE || requestCode == REQUEST_TIME){
            DateTime date;
            if(requestCode == REQUEST_TIME) {
                date = (DateTime) data.getSerializableExtra(TimePickerDialog.EXTRA_TIME);
            }
            else {
                date = (DateTime) data.getSerializableExtra(DatePickerDialog.EXTRA_DATE);
            }
            mTask.setDate(date);
            updateDate();
        }
        if(requestCode == REQUEST_REPEAT){
            Repeat repeat = (Repeat) data.getSerializableExtra(SetRepeatDialog.EXTRA_REPEAT);
            mTask.setRepeat(repeat);
        }

        if(requestCode == REQUEST_SNOOZE){
            long snooze = data.getExtras().getLong(ChooseSnoozeDialog.EXTRA_INTERVAL);
            mTask.setQuickSnoozeTime(snooze);
        }
        updateUI();
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


        Repeat repeat = mTask.getRepeat();
        String repeatText = repeat!=null?repeat.getRepeatTime().getTimeString(getContext(),getString(R.string.every),
                ""):getString(R.string.set_repeat);
        mRepeatButton.setText(underline(repeatText));

        int snoozeVis = mTask.getSnoozeTime()!=null&&mTask.isOverdue() ? VISIBLE:GONE;
        mSnoozeInfoButton.setVisibility(snoozeVis);
        if(snoozeVis==VISIBLE){
            mSnoozeInfoButton.setText(underline(getString(R.string.snoozed_till,
                    mTask.getSnoozeTime().toString("h:mm a MMM dd yyyy"))));
        }


        long snooze = mTask.getQuickSnoozeTime();

        String snoozeText = snooze>0?(SpanOfTime.ofMillis(snooze)).getTimeString(getContext(),
                getString(R.string.snooze_for),"") :
                getString(R.string.set_quick_snooze);
        mQuickSnoozeButton.setText(underline(snoozeText));


        mDueHasValue = mTask.getDate() != null || mTask.getRepeat() != null;
        mAlertHasValue = mTask.getQuickSnoozeTime() != 0 || mTask.getReminders().size() > 0;

        int view;

        view = mDueHasValue ? VISIBLE: GONE;
        mCloseDue.setVisibility(view);

        view = mAlertHasValue ? VISIBLE: GONE;
        mCloseAlert.setVisibility(view);

        getActivity().invalidateOptionsMenu();
    }
}
