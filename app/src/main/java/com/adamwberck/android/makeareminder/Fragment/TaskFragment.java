package com.adamwberck.android.makeareminder.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import java.util.List;

import static android.app.Activity.RESULT_OK;

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


    private ListView mReminderListView;
    private ReminderAdapter mReminderAdapter;
    private Button mRepeatButton;
    private Button mSnoozeButton;
    private ImageButton mColorButton;
    private ActionBar mActionBar;
    private Menu mMenu;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        int ID = getArguments().getInt(ARG_TASK_ID);
        mTask = GroupLab.get(getContext()).getTask(ID);
        //mActionBar = ((AppCompatActivity)getActivity())
                //.getSupportActionBar();
        //mActionBar.setBackgroundDrawable(new ColorDrawable(mTask.getGroup().getColorInt()));
        //TextView tv = setActionBarTextColor(getResources().getString(R.string.app_name)
        //       ,mTask.getGroup(),getContext());

        //mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        //mActionBar.setCustomView(tv);
        getActivity().invalidateOptionsMenu();
    }

    @NonNull
    public static TextView setActionBarTextColor(CharSequence title,Group group,Context context) {
        Resources resources = context.getResources();
        int abColor = OverviewFragment.isDark(group.getColorInt()) ?
                resources.getColor(R.color.white) : resources.getColor(R.color.black);
        TextView tv = new TextView(context);
        tv.setText(title);
        tv.setTextColor(abColor);
        tv.setTextSize(18);
        return tv;
    }

    @Override
    public void  onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.fragment_task,menu);
    }

    private void styleMenuButton() {
        // Find the menu item you want to style
        /*
        View view = getActivity().findViewById(R.id.save_task);


        // Cast to a TextView instance if the menu item was found
        if (view != null && view instanceof TextView) {
            Resources resources = getResources();
            int abColor = OverviewFragment.isDark(mTask.getGroup().getColorInt()) ?
                    resources.getColor(R.color.white) : resources.getColor(R.color.black);
            ((TextView) view).setTextColor(abColor);
            //((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        }
        */
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        styleMenuButton();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle SIState) {
        View v = inflater.inflate(R.layout.fragment_task, container,false);
        super.setupUI(v);
        mNameField = v.findViewById(R.id.task_name);
        if(mTask.getName()!=null) {
            mNameField.setText(mTask.getName());
        }


        //TODO color button should be group change button


        mSnoozeButton = v.findViewById(R.id.snooze_button);
        mSnoozeButton.setOnClickListener(new View.OnClickListener() {
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

        mTimeButton = v.findViewById(R.id.time_button);
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

        updateDate();

        mRepeatButton = v.findViewById(R.id.repeat_button);
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

    private void updateDate() {
        if(mTask.getDate()!=null) {
            mTimeButton.setText(mTask.getDate().toString("h:mm a"));
        }
        else {
            mTimeButton.setText(R.string.default_time);
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
        TaskFragment TskFragment = (TaskFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment_container);
        if(TskFragment !=null) {
            //mDeleteCallBack = (OnDeleteCrimeListener) context;
        }
    }

    private void setTskChanged(int TskChanged){
        Intent data = new Intent();
        data.putExtra(EXTRA_Tsk_CHANGED, TskChanged);
        getActivity().setResult(RESULT_OK, data);
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
            Drawable icon = mDataSource.get(position).isAlarm()?r.getDrawable(R.drawable.ic_alarm) :
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
            //mTask.setDefaultSnooze(snooze);
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
        if(repeat!=null){
            String s = repeat.getRepeatTime().getTimeString(getContext(),getString(R.string.every),
                    "");
            mRepeatButton.setText(s);
        }
        else {
            mRepeatButton.setText(R.string.default_repeat);
        }

        //long snooze = mTask.getDefaultSnoozeTime();
        /*
        String snoozeText = snooze>0?(SpanOfTime.ofMillis(snooze)).getTimeString(getContext(),
                getString(R.string.snooze_for),"") :
                getString(R.string.default_snooze);
        mSnoozeButton.setText(snoozeText);*/

        getActivity().invalidateOptionsMenu();
    }
}
