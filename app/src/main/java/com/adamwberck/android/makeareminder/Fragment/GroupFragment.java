package com.adamwberck.android.makeareminder.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Dialog.ColorChooserDialog;
import com.adamwberck.android.makeareminder.Dialog.CreateReminderDialog;
import com.adamwberck.android.makeareminder.Dialog.DatePickerDialog;
import com.adamwberck.android.makeareminder.Dialog.SetRepeatDialog;
import com.adamwberck.android.makeareminder.Dialog.TimePickerDialog;
import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.Elements.Reminder;
import com.adamwberck.android.makeareminder.Elements.Repeat;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;
import com.skydoves.colorpickerpreference.ColorPickerDialog;

import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;

public class GroupFragment extends VisibleFragment{
    private static final String TAG = "GroupFragment";

    private Group mGroup;
    private Callbacks mCallbacks;
    private EditText mNameField;


    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_COLOR = 2;
    private static final int REQUEST_REMINDER = 3;
    private static final int REQUEST_EDIT = 4;
    private static final int REQUEST_SNOOZE = 5;
    private static final int REQUEST_REPEAT = 6;

    private static final String ARG_GROUP = "group";

    private static final String DIALOG_COLOR = "DialogColor";
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
    private Button mTimeButton;
    private Button mAddReminderButton;
    private ListView mReminderListView;
    private ReminderAdapter mReminderAdapter;
    private Button mRepeatButton;
    private Button mTestAlarmButton;
    private Button mSnoozeButton;
    private Button mCompleteButton;
    private ImageButton mClearDateButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mGroup = (Group) getArguments().getSerializable(ARG_GROUP);
    }

    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_group,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_task:
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle SIState) {
        View v = inflater.inflate(R.layout.fragment_group, container,false);
        super.setupUI(v);
        mNameField = v.findViewById(R.id.task_name);
        if(mGroup.getName()!=null) {
            mNameField.setText(mGroup.getName());
        }

        Button colorButton =  v.findViewById(R.id.group_color_button);
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start Color picker
                FragmentManager manager = getFragmentManager();
                int rgb =  mGroup.getColor();
                //int red = (rgb>>16)&0x0ff;
                //int green=(rgb>>8) &0x0ff;
                //int blue= (rgb)    &0x0ff;
                //int rgb = ((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff);
                ColorChooserDialog chooserDialog = ColorChooserDialog.newInstance(rgb);
                chooserDialog.setTargetFragment(GroupFragment.this,REQUEST_COLOR);
                chooserDialog.show(manager,DIALOG_COLOR);
            }
        });

        mSnoozeButton = v.findViewById(R.id.snooze_button);
        mSnoozeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UUID id = mGroup.getID();
                String name = mGroup.getName();
                String title = mGroup.getName();
                //TODO create default snooze dialog or use one
                updateUI();
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
                mGroup.setName(s.toString());
                updateGroup();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // pass
            }
        });

        mTimeButton = v.findViewById(R.id.time_button);
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerDialog dialog = TimePickerDialog
                        .newInstance(mGroup.getDefaultTime());
                dialog.setTargetFragment(GroupFragment.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });

        mClearDateButton = v.findViewById(R.id.clear_time_button);
        mClearDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroup.setDefaultTime(null);
            }
        });

        updateDate();

        mRepeatButton = v.findViewById(R.id.repeat_button);
        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO make generic method to do this
                FragmentManager manager = getFragmentManager();
                SetRepeatDialog dialog = SetRepeatDialog.newInstance(mGroup.getDefaultRepeat());
                dialog.setTargetFragment(GroupFragment.this,REQUEST_REPEAT);
                dialog.show(manager,DIALOG_REPEAT);
            }
        });

        mReminderAdapter = new ReminderAdapter(getContext(),mGroup.getDefaultReminders());
        mReminderListView = v.findViewById(R.id.reminder_list_view);
        mReminderListView.setAdapter(mReminderAdapter);


        mAddReminderButton = v.findViewById(R.id.add_reminder_button);
        mAddReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                CreateReminderDialog dialog = CreateReminderDialog.newInstance();
                dialog.setTargetFragment(GroupFragment.this, REQUEST_REMINDER);
                dialog.show(manager,DIALOG_REMINDER);
            }
        });

        return v;
    }

    private void updateDate() {
        if(mGroup.getDefaultTime()!=null) {
            mTimeButton.setText(mGroup.getDefaultTime().toString("hh:mm a"));
        }
        else {
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
        if(inputMethodManager!=null) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        if(context instanceof Callbacks) {
            mCallbacks = (Callbacks) context;
        }
        GroupFragment taskFragment = (GroupFragment) getActivity().getSupportFragmentManager()
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

    private void updateGroup() {
        //TaskLab.get(getActivity()).updateTask(mTask);
        if(mCallbacks!=null) {
            mCallbacks.onGroupUpdated(mGroup);
        }
    }

    public Group getGroup() {
        return mGroup;
    }


    public interface Callbacks{
        void onGroupUpdated(Group group);
    }


    public static GroupFragment newInstance(Group group) {
        Bundle args =  new Bundle();
        args.putSerializable(ARG_GROUP, group);
        GroupFragment fragment = new GroupFragment();
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
                    CreateReminderDialog dialog = CreateReminderDialog.newInstance(reminder);
                    dialog.setTargetFragment(GroupFragment.this, REQUEST_EDIT);
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
                    mGroup.removeReminder(reminder);
                    updateUI();
                }
            });
            updateUI();
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
    public void onResume(){
        Log.i(TAG,"Resume");
        updateUI();
        super.onResume();
    }

    @Override
    public void onDestroy(){
        //TaskLab.get(getContext()).removeUnnamed();
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
                mGroup.removeReminder(reminder);
            }
            mGroup.addReminder(span,isAlarm);
        }
        if(requestCode == REQUEST_DATE || requestCode == REQUEST_TIME){
            DateTime date;
            if(requestCode == REQUEST_TIME) {
                date = (DateTime) data.getSerializableExtra(TimePickerDialog.EXTRA_TIME);
            }
            else {
                date = (DateTime) data.getSerializableExtra(DatePickerDialog.EXTRA_DATE);
            }
            mGroup.setDefaultTime(date);
            updateDate();
        }
        if(requestCode == REQUEST_REPEAT){
            Repeat repeat = (Repeat) data.getSerializableExtra(SetRepeatDialog.EXTRA_REPEAT);
            mGroup.setDefaultRepeat(repeat);
        }
        updateUI();
    }

    private void updateUI() {

        List<Reminder> reminders = mGroup.getDefaultReminders();

        if (mReminderAdapter== null) {
            mReminderAdapter = new ReminderAdapter(getContext(),reminders);
            mReminderListView.setAdapter(mReminderAdapter);
        } else {
            mReminderListView.setAdapter(mReminderAdapter);
            mReminderAdapter.notifyDataSetChanged();
        }
        Repeat repeat = mGroup.getDefaultRepeat();
        if(repeat!=null){
            String s = repeat.getRepeatTime().getTimeString(getContext(),"Every ",
                    "");
            mRepeatButton.setText(s);
        }
        else {
            mRepeatButton.setText(R.string.repeat);
        }

    }
}
