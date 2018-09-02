package com.adamwberck.android.makeareminder.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Repeat;
import com.adamwberck.android.makeareminder.Elements.SpanOfTime;
import com.adamwberck.android.makeareminder.R;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SetRepeatDialog extends DismissDialog {
    public static final String ARG_REPEAT = "repeat";
    public static final String EXTRA_REPEAT = "com.adamwberck.android.makeareminder.extrarepeat";
    private static final int REQUEST_TIME = 0;
    private static final String DIALOG_TIME = "DialogTime";
    private static final String EXTRA_REMOVE = "com.adamwberck.android.makeareminder.removerepeat";

    private long mDuration;
    private int mTimeTypeInt;
    private int mWeekInt;
    private Repeat mRepeat;
    private ListView mListView;
    private TimeListAdapter mTimeListAdapter;

    //TODO more often should be open

    //TODO add hourly repeats
    //TODO add exclusion days for hourly repeats

    public static SetRepeatDialog newInstance(Repeat currentRepeat) {

        Bundle args = new Bundle();
        args.putSerializable(ARG_REPEAT, currentRepeat);

        SetRepeatDialog fragment = new SetRepeatDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mRepeat = (Repeat) getArguments().getSerializable(ARG_REPEAT);
        if(mRepeat==null){
            mRepeat = new Repeat(1,SpanOfTime.ofDays(1));
        }
        mDuration = mRepeat.getRawPeriod();
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_repeat,null);
        //TODO change edit text to dropdown
        EditText timeText = view.findViewById(R.id.repeat_time_text);
        timeText.setText(String.format(Locale.getDefault(),"%d", mRepeat.getRawPeriod()));
        timeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String s1 = s.toString();
                try {
                    mDuration = Long.parseLong(s1);
                }catch(NumberFormatException ignored){ }
            }
        });
        mListView = view.findViewById(R.id.time_repeat_list);
        mListView.setAdapter(mTimeListAdapter);
        final TextView moreOften = view.findViewById(R.id.more_often_option);
        final View moreOftenGroup = view.findViewById(R.id.more_often_group) ;

        int vis = mRepeat.isMoreOften()? View.VISIBLE:View.GONE;
        moreOftenGroup.setVisibility(vis);

        moreOftenVisibility(moreOften);
        moreOften.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int vis = moreOftenGroup.getVisibility()==View.GONE ? View.VISIBLE:View.GONE;
                moreOftenGroup.setVisibility(vis);
            }
        });
        final Button addRepeatTime = moreOftenGroup.findViewById(R.id.add_repeat_time_button);
        addRepeatTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerDialog dialog = TimePickerDialog
                        .newInstance(new DateTime());
                dialog.setTargetFragment(SetRepeatDialog.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });

        Spinner spinner = view.findViewById(R.id.repeat_time_spinner);
        final GridView gridView = view.findViewById(R.id.day_buttons);
        final DayAdapter weekAdapter = new DayAdapter(getContext(),Arrays.asList(getResources()
                .getStringArray(R.array.days_of_the_week)),true);
        final DayAdapter monthAdapter = new DayAdapter(getContext(),daysOfMonthList(),
                false);

        updateGridView(gridView, weekAdapter, monthAdapter);
        //Setup WeekButton Listeners


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type_repeat,R.layout.spinner_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).toString().equals("")) {
                    moreOftenGroup.setVisibility(View.GONE);
                    mTimeTypeInt = position;
                    mRepeat.setRepeatTime(getTimeType());
                    moreOftenVisibility(moreOften);
                    updateGridView(gridView, weekAdapter, monthAdapter);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        builder.setTitle(R.string.repeat_title)
                .setView(view)
                .setPositiveButton(R.string.repeat_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setRepeat();
                    }
                })
                .setNegativeButton(R.string.clear_time, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRepeat = null;
                        sendResult(Activity.RESULT_OK);
                    }
                });


        spinner.setSelection(setSpinnerNumber(mRepeat.getTimeType()));
        updateUI();
        return builder.create();
    }

    private SpanOfTime getTimeType() {
        SpanOfTime span;
        if(mTimeTypeInt==0){
            span = SpanOfTime.ofDays(mDuration);
        }
        else if (mTimeTypeInt==1){
            span = SpanOfTime.ofWeeks(mDuration);
        }
        else if(mTimeTypeInt==2){
            //TODO add month support
            span = SpanOfTime.ofMonths(mDuration);
        }
        else {
            //TODO add year support
            span = SpanOfTime.ofYears(mDuration);
        }
        return span;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        if(requestCode==REQUEST_TIME){
            LocalTime localTime =
                    new LocalTime(data.getSerializableExtra(TimePickerDialog.EXTRA_TIME));
            mRepeat.addTime(localTime);
        }
        updateUI();
    }
    private void moreOftenVisibility(TextView moreOften) {
        if(mTimeTypeInt==0){
            moreOften.setVisibility(View.VISIBLE);
        }
        else {
            moreOften.setVisibility(View.GONE);
        }
    }

    private void updateGridView(GridView gridView, DayAdapter weekAdapter, DayAdapter monthAdapter) {
        if(mTimeTypeInt==1||mTimeTypeInt==2){
            gridView.setVisibility(View.VISIBLE);
            if(mTimeTypeInt==1){
                gridView.setAdapter(weekAdapter);
            }else {
                gridView.setAdapter(monthAdapter);
            }
        }
        else {
            gridView.setVisibility(View.GONE);
        }
    }

    private static List<String> daysOfMonthList() {
        List<String> list = new ArrayList<>(31);
        for(int i=1;i<=31;i++){
            list.add(i+"");
        }
        return list;
    }

    private static int setSpinnerNumber(SpanOfTime.Type type) {
        if(type==SpanOfTime.Type.DAY){
            return 0;
        }
        else if(type== SpanOfTime.Type.WEEK){
            return 1;
        }
        else if(type==SpanOfTime.Type.MONTH){
            return 2;
        }
        else if(type==SpanOfTime.Type.YEAR){
            return 3;
        }
        return -1;
    }

    private void cancel() {
        getDialog().cancel();
        sendResult(Activity.RESULT_CANCELED);
    }

    private void setRepeat() {
        //TODO fix this so it works with field element
        sendResult(Activity.RESULT_OK);
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_REPEAT, mRepeat);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }

    private class DayAdapter extends BaseAdapter {

        private final Context mContext;
        private List<String> mDataSource;
        private final LayoutInflater mInflater;
        private final boolean mIsWeek;

        public DayAdapter(Context context, List<String> dataSource,boolean isWeek) {
            mContext = context;
            mDataSource = dataSource;
            mIsWeek = isWeek;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            //TODO replace with ViewHolder
            @SuppressLint("ViewHolder")
            View v = mInflater.inflate(R.layout.button_circle, parent, false);
            final Button button = v.findViewById(R.id.toggle_button);
            String name = (String) getItem(position);
            button.setText(name);
            updateDayButton(position, button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mIsWeek) {
                        mRepeat.toggleWeek(position);
                    }
                    else {
                        mRepeat.toggleDayOfMonth(position+1);
                    }
                    updateDayButton(position, button);
                }
            });
            return v;
        }

        private void updateDayButton(int position, Button button) {
            if(mIsWeek) {
                int background = mRepeat.isRepeatOnWeekDay(position) ? R.drawable.ic_button_circle_on :
                        R.drawable.ic_button_circle_off;
                button.setBackgroundResource(background);
            }
            else {
                int background = mRepeat.isRepeatOnMonthDay(position+1) ? R.drawable.ic_button_circle_on :
                        R.drawable.ic_button_circle_off;
                button.setBackgroundResource(background);
            }
        }

        public void setDataSource(List<String> dataSource) {
            mDataSource = dataSource;
        }
    }

    private class TimeListAdapter extends BaseAdapter {
        private final Context mContext;
        private final List<LocalTime> mDataSource;
        private final LayoutInflater mInflater;
        public TimeListAdapter(Context context, List<LocalTime> dataSource) {
            mContext = context;
            mDataSource = dataSource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            final LocalTime localTime = (LocalTime) getItem(position);
            View v = mInflater.inflate(R.layout.list_time_repeat, parent, false);
            TextView textTime = v.findViewById(R.id.text_time_repeat);
            //TODO add option to change time format
            textTime.setText(localTime.toString("hh:mm a",Locale.getDefault()));
            textTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager manager = getFragmentManager();
                    TimePickerDialog dialog = TimePickerDialog
                            .newInstance(new DateTime());
                    dialog.setTargetFragment(SetRepeatDialog.this, REQUEST_TIME);
                    dialog.show(manager,DIALOG_TIME);
                }
            });
            ImageView close = v.findViewById(R.id.time_delete);
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mRepeat.removeTime(localTime);
                    updateUI();
                }
            });
            return v;
        }
    }

    private void updateUI() {
        List<LocalTime> times = mRepeat.getTimes();

        if (mTimeListAdapter == null) {
            mTimeListAdapter = new TimeListAdapter(getContext(),times);
            mListView.setAdapter(mTimeListAdapter);
        } else {
            mListView.setAdapter(mTimeListAdapter);
            mTimeListAdapter.notifyDataSetChanged();
        }

    }
}
