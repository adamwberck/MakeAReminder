package com.adamwberck.android.makeareminder.Dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.GRAY;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SetRepeatDialog extends DismissDialog {
    public static final String ARG_REPEAT = "repeat";
    public static final String EXTRA_REPEAT = "com.adamwberck.android.makeareminder.extrarepeat";
    private static final int REQUEST_TIME = 0;
    private static final String DIALOG_TIME = "DialogTime";
    private static final String EXTRA_REMOVE = "com.adamwberck.android.makeareminder.removerepeat";
    private static final String TAG = "SetRepeatDialog";

    private long mDuration;
    private int mTimeTypeInt;
    private int mWeekInt;
    private Repeat mRepeat;
    private ListView mMoreOftenList;
    private TimeListAdapter mTimeListAdapter;
    private TextView mMoreOftenText;
    private boolean mMoreOftenPressed = false;
    private ArrayAdapter mTimeValueArray;
    private Map<SpanOfTime.Type,List<Integer>> mTimeValueMap = new ArrayMap<>(4);

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
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mTimeValueMap.put(SpanOfTime.Type.DAY,intArray(60));
        mTimeValueMap.put(SpanOfTime.Type.WEEK,intArray(52));
        mTimeValueMap.put(SpanOfTime.Type.MONTH,intArray(36));
        mTimeValueMap.put(SpanOfTime.Type.YEAR,intArray(5));

        mRepeat = (Repeat) getArguments().getSerializable(ARG_REPEAT);
        if(mRepeat==null){
            mRepeat = new Repeat(1,SpanOfTime.ofDays(1));
        }
        mDuration = mRepeat.getRawPeriod();
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_repeat,null);
        //TODO change edit text to dropdown
        mTimeValueArray = new ArrayAdapter(getContext(),R.layout.spinner_item_right);
        final Spinner timeSpinner = view.findViewById(R.id.repeat_time_spinner);
        timeSpinner.setAdapter(mTimeValueArray);
        timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String s  = parent.getItemAtPosition(position).toString();
                try {
                    mDuration = Long.parseLong(s);
                }catch(NumberFormatException ignored){ }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //pass
            }
        });



        mMoreOftenText= view.findViewById(R.id.more_often_option);
        mMoreOftenText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mMoreOftenPressed=!mMoreOftenPressed;
                updateUI();
            }
        });
        mMoreOftenList = view.findViewById(R.id.more_often_repeat_list);
        mMoreOftenList.setAdapter(mTimeListAdapter);
        View footer = inflater
                .inflate(R.layout.list_time_repeat_footer, mMoreOftenList, false);
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add reminder button
                FragmentManager manager = getFragmentManager();
                TimePickerDialog dialog = TimePickerDialog
                        .newInstance(new DateTime());
                dialog.setTargetFragment(SetRepeatDialog.this, REQUEST_TIME);
                dialog.show(manager,DIALOG_TIME);
            }
        });
        mMoreOftenList.addFooterView(footer);




        final GridView gridView = view.findViewById(R.id.day_buttons);
        final DayAdapter weekAdapter = new DayAdapter(getContext(),Arrays.asList(getResources()
                .getStringArray(R.array.days_of_the_week)),true);
        List<String> list = daysOfMonthList();
        list.add(getString(R.string.every_day));
        final DayAdapter monthAdapter = new DayAdapter(getContext(),list, false);

        updateGridView(gridView, weekAdapter, monthAdapter);


        //Setup WeekButton Listeners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.time_type_repeat,R.layout.spinner_item);
        Spinner typeSpinner = view.findViewById(R.id.repeat_time_type_spinner);
        typeSpinner.setAdapter(adapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).toString().equals("")) {
                    mTimeTypeInt = position;
                    mRepeat.setRepeatTime(getSpanOfTime());
                    updateGridView(gridView, weekAdapter, monthAdapter);
                    mTimeValueArray.clear();
                    mTimeValueArray.addAll(mTimeValueMap.get(getSpanOfTime().getTimeType()));
                    timeSpinner.setSelection(0);
                    updateUI();
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
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRepeat = null;
                        sendResult(Activity.RESULT_CANCELED);
                    }
                });


        typeSpinner.setSelection(setSpinnerNumber(mRepeat.getTimeType()));
        updateUI();
        return builder.create();
    }

    private static List<Integer> intArray(int size) {
        List<Integer> list = new ArrayList<>(size);
        for(int i=1;i<=size;i++){
            list.add(i);
        }
        return list;
    }

    private SpanOfTime getSpanOfTime() {
        SpanOfTime span;
        if(mTimeTypeInt==0){
            span = SpanOfTime.ofDays(mDuration);
        }
        else if (mTimeTypeInt==1){
            span = SpanOfTime.ofWeeks(mDuration);
        }
        else if(mTimeTypeInt==2){
            span = SpanOfTime.ofMonths(mDuration);
        }
        else {
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


    private void updateGridView(GridView gridView, DayAdapter weekAdapter, DayAdapter monthAdapter) {
        if(mTimeTypeInt==1||mTimeTypeInt==2){
            gridView.setVisibility(VISIBLE);
            if(mTimeTypeInt==1){
                gridView.setAdapter(weekAdapter);
            }else {
                gridView.setAdapter(monthAdapter);
            }
        }
        else {
            gridView.setVisibility(GONE);
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
                    else if(position==31){
                        mRepeat.toggleLastDay();
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
                int background = mRepeat.isRepeatOnWeekDay(position) ?
                        R.drawable.ic_button_circle_on :
                        R.drawable.ic_button_circle_off;
                button.setBackgroundResource(background);
            }
            else {
                if(position<31) {
                    int background = mRepeat.isRepeatOnMonthDay(position + 1) ?
                            R.drawable.ic_button_circle_on :
                            R.drawable.ic_button_circle_off;
                    button.setBackgroundResource(background);
                }
                else {
                    int background = mRepeat.isLastDayOfMonth() ?
                            R.drawable.ic_button_circle_on :
                            R.drawable.ic_button_circle_off;
                    button.setBackgroundResource(background);
                }
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
            textTime.setText(localTime.toString("h:mm a",Locale.getDefault()));
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
            mMoreOftenList.setAdapter(mTimeListAdapter);
        } else {
            mMoreOftenList.setAdapter(mTimeListAdapter);
            mTimeListAdapter.notifyDataSetChanged();
        }
        int vis;
        SpanOfTime.Type type = mRepeat.getTimeType();
        vis = type== SpanOfTime.Type.DAY && (mRepeat.isMoreOften() || mMoreOftenPressed)
                ? VISIBLE : GONE;
        mMoreOftenList.setVisibility(vis);
        vis = type == SpanOfTime.Type.DAY ? VISIBLE:GONE;
        mMoreOftenText.setVisibility(vis);
        int color = mTimeListAdapter.getCount()>0?GRAY:BLACK;
        mMoreOftenText.setTextColor(color);
    }
}
