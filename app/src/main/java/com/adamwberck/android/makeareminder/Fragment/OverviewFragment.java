package com.adamwberck.android.makeareminder.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.adamwberck.android.makeareminder.Elements.Group;
import com.adamwberck.android.makeareminder.GroupLab;
import com.adamwberck.android.makeareminder.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class OverviewFragment extends VisibleFragment {
    private static final String TAG = "OverviewFragment";
    final float[] ADD_POS = new float[2];
    //TODO auto load if one group
    private RecyclerView mGroupRecyclerView;
    private Callbacks mCallbacks;
    private GroupAdapter mAdapter;
    private int mWidth;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton mTrashCan;
    private GroupHolder mDraggingGroupHolder;
    public int mDraggingColor;
    private boolean mOverTrashcan = true;
    private ImageButton mAddTask;
    private boolean mHasTaskJustAdded = false;

    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    public interface Callbacks {
        void onGroupSelected(UUID id);
        void onGroupEdit(UUID id);
        void onTaskAdded(Group group);
    }

    public static Fragment newInstance() {
        Bundle args =  new Bundle();

        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"Resume");
        if(mHasTaskJustAdded){
            mAddTask.animate()
                    .x(ADD_POS[0])
                    .y(ADD_POS[1])
                    .setDuration(400)
                    .start();
            mAddTask.getBackground().setColorFilter(getResources()
                    .getColor(R.color.lightGray), PorterDuff.Mode.DARKEN);
        }
        mHasTaskJustAdded=false;
        updateUI();
    }


    private boolean isViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle sis){
        final View view = inflater.inflate(R.layout.fragment_overview,container,false);

        mTrashCan = view.findViewById(R.id.trashcan_circle);
        mAddTask = view.findViewById(R.id.add_task_circle);
        Log.i(TAG,"onCreateView");

        mAddTask.post(new Runnable() {
            @Override
            public void run() {
                ADD_POS[0] = mAddTask.getX();
                ADD_POS[1] = mAddTask.getY();
            }
        });


        mAddTask.setOnTouchListener(new View.OnTouchListener() {
            float dX, dY;
            boolean onCard;
            Group mGroup;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        onCard =false;
                        break;

                    case MotionEvent.ACTION_MOVE:

                        v.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        onCard = false;
                        for (int i = 0; i < mGroupRecyclerView.getChildCount(); i++) {
                            View innerView = mGroupRecyclerView.getChildAt(i);
                            GroupHolder groupHolder =
                                    (GroupHolder) mGroupRecyclerView.getChildViewHolder(innerView);
                            Group hoverGroup = groupHolder.getGroup();
                            if(hoverGroup.isSpecial()){
                                return true;
                            }
                            View card = groupHolder.mCardView;
                            if(isViewInBounds(innerView,(int)event.getRawX(),(int)event.getRawY())){
                                mGroup = hoverGroup;
                                card.getBackground().setColorFilter(alterColor(mGroup.getColorInt(),
                                        0x77), PorterDuff.Mode.DARKEN);
                                mAddTask.getBackground().setColorFilter(getResources()
                                    .getColor(R.color.darkGray), PorterDuff.Mode.DARKEN);
                                onCard = true;
                            }
                            else {
                                card.getBackground().setColorFilter(hoverGroup.getColorInt(),
                                        PorterDuff.Mode.DARKEN);
                            }
                        }
                        if(!onCard){
                            mAddTask.getBackground().setColorFilter(getResources()
                                    .getColor(R.color.lightGray), PorterDuff.Mode.DARKEN);
                        }
                        break;
                    case MotionEvent.ACTION_UP:

                        if(onCard) {
                            mCallbacks.onTaskAdded(mGroup);
                            mHasTaskJustAdded = true;
                        }
                        else {
                            v.animate()
                                    .x(ADD_POS[0])
                                    .y(ADD_POS[1])
                                    .setDuration(400)
                                    .start();
                            mAddTask.getBackground().setColorFilter(getResources()
                                    .getColor(R.color.lightGray), PorterDuff.Mode.DARKEN);
                        }
                    default:
                        return false;
                }
                return true;
            }
        });

        turnOnAddCircle();


        mGroupRecyclerView = view.findViewById(R.id.group_recycler_view);
        mGroupRecyclerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG,"touch rv:"+event.getX()+","+event.getY());

                if(isViewInBounds(mTrashCan,(int)event.getRawX(),(int)event.getRawY())){
                    Log.i(TAG,"Touching Can????");
                    if(mDraggingGroupHolder!=null){
                        String color = mDraggingGroupHolder.mGroup.getColor();
                        Log.i(TAG,"Adapter Pos: "+mDraggingGroupHolder.getAdapterPosition());
                        Log.i(TAG,"Layout  Pos: "+mDraggingGroupHolder.getLayoutPosition());
                        mOverTrashcan = true;
                        int iColor = alterColor(Color.parseColor(color),0x88,4);
                        //iColor = alterColor(iColor,0x22,0);
                        mDraggingGroupHolder.mCardView.getBackground().setColorFilter(
                                iColor, PorterDuff.Mode.MULTIPLY);
                        mTrashCan.getBackground()
                                .setColorFilter(getResources().getColor(R.color.darkGray),
                                        PorterDuff.Mode.DARKEN);
                    }
                }
                else {
                    try {
                        mDraggingGroupHolder.setColor(mDraggingColor);
                        mOverTrashcan = false;
                        mTrashCan.getBackground()
                                .setColorFilter(getResources().getColor(R.color.lightGray),
                                        PorterDuff.Mode.DARKEN);
                    }catch (NullPointerException ignored){}
                }

                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(mDraggingGroupHolder!=null&&mOverTrashcan){
                        mAdapter.onItemDismiss(mDraggingGroupHolder);
                        //mDraggingGroupHolder.getView().setVisibility(View.GONE);
                    }
                }
                return false;
            }
        });



        //setTaskRecyclerViewItemTouchListener();
        //mGroupRecyclerView.setAdapter(new GroupAdapter(mGroups));
        updateUI();
        view.post(new Runnable() {
            @Override
            public void run() {
                mGroupRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                        getColumns(view.getWidth())));
            }
        });
        //setTaskRecyclerViewItemTouchListener();

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {

            @Override
            public int interpolateOutOfBoundsScroll (RecyclerView recyclerView,int viewSize,
                                                     int viewSizeOutOfBounds,int totalSize,
                                                     long msSinceStartScroll){
                if(!mOverTrashcan) {
                    return super.interpolateOutOfBoundsScroll(recyclerView, viewSize,
                            viewSizeOutOfBounds, totalSize, msSinceStartScroll);
                }
                else {
                    return 0;
                }
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                GroupHolder groupHolder = (GroupHolder) viewHolder;
                mDraggingGroupHolder = groupHolder;
                if(mDraggingGroupHolder.getGroup().isSpecial()){
                    return 0;
                }

                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = 0;


                int color = Color.parseColor(groupHolder.mGroup.getColor());
                mDraggingColor = alterColor(color,0x33);
                groupHolder.mCardView.getBackground().setColorFilter(color,PorterDuff.Mode.DARKEN);
                turnOnTrashcan();
                mTrashCan.getBackground().setColorFilter(getResources().getColor(R.color.lightGray),
                                PorterDuff.Mode.DARKEN);

                Log.i(TAG,"Drag Flags: "+dragFlags);
                return makeMovementFlags(dragFlags, swipeFlags);
            }


            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source,
                                  RecyclerView.ViewHolder target) {
                mAdapter.onItemMove(source.getAdapterPosition(),target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }



            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                    RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                Log.i(TAG,"dx: " +dX + " dy: " + dY);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                        isCurrentlyActive);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                // We only want the active item to change
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    ((GroupHolder)viewHolder).onItemSelected();
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                ((GroupHolder)viewHolder).onItemClear();
            }

        };
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mGroupRecyclerView);


        return view;
    }

    private void turnOnTrashcan() {
        mAddTask.setVisibility(View.GONE);
        mTrashCan.setVisibility(View.VISIBLE);
    }

    private void turnOnAddCircle() {
        mTrashCan.setVisibility(View.GONE);
        mAddTask.setVisibility(View.VISIBLE);
    }

    private int alterColor(int color, int alterValue, int rgbNum) {
        int[] rgb = getRGB(color);
        StringBuilder cHex = new StringBuilder();
        if(rgbNum==4){
            cHex.append("#"+Integer.toHexString(alterValue));
        }
        else {
            cHex.append("#ff");
        }
        alterValue *= isDark(color)?1:-1;
        int i=0;
        for(int c :rgb){
            //int c = rgb[i];
            int alterTemp = i==rgbNum ? alterValue:(int)(alterValue*.5);
            c += alterTemp;
            c += alterTemp;
            i++;
            c = Math.max(0,c);
            c = Math.min(0xff,c);
            String hex = Integer.toHexString(c);
            hex = c<=0xf?"0"+hex:hex;
            cHex.append(hex);
        }
        return Color.parseColor(cHex.toString());
    }

    private int alterColor(int color, int alterValue) {
        int[] rgb = getRGB(color);
        StringBuilder cHex = new StringBuilder().append("#ff");
        alterValue *= isDark(color)?1:-1;
        for(int c :rgb){
            //int c = rgb[i];
            c += alterValue;
            c = Math.max(0,c);
            c = Math.min(0xff,c);
            String hex = Integer.toHexString(c);
            hex = c<=0xf?"0"+hex:hex;
            cHex.append(hex);
        }
        return Color.parseColor(cHex.toString());
    }

    private int[] getRGB(int argb) {
        int rgb[] = new int[3];
        rgb[0] = (argb>>16)&0xFF;
        rgb[1] = (argb>>8)&0xFF;
        rgb[2] = (argb)&0xFF;
        return rgb;
    }

    private int getColumns(int width) {
        Log.i(TAG,"width: " + width);
        return width/200;
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
        Group group = new Group();
        GroupLab.get(getContext()).addGroup(group);
        GroupLab.saveLab();
        mCallbacks.onGroupEdit(group.getID());
        //updateUI();
    }

    private void updateUI(){
        GroupLab groupLab = GroupLab.get(getActivity());
        List<Group> groups = groupLab.getGroups();


        if (mAdapter == null) {
            mAdapter = new GroupAdapter(groups);
            mGroupRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setGroups(groups);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class GroupHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        private final ImageButton mEditButton;
        private final View mCardView;
        private View mView;
        private TextView mGroupNameTextView;
        private TextView mDueTextView;
        private TextView mOverdueTextView;
        private Group mGroup;


        private GroupHolder(LayoutInflater inflater, ViewGroup parent,int viewType)
        {
            super(inflater.inflate(viewType,parent,false));
            mView = itemView.findViewById(R.id.item_view);
            itemView.setOnClickListener(this);
            mCardView = itemView.findViewById(R.id.card_layout);

            mGroupNameTextView = itemView.findViewById(R.id.group_name);
            mDueTextView = itemView.findViewById(R.id.num_task_due);
            mOverdueTextView = itemView.findViewById(R.id.num_task_overdue);
            mEditButton = itemView.findViewById(R.id.edit_group_button);
            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallbacks.onGroupEdit(mGroup.getID());
                }
            });
        }

        @Override
        public void onClick(View v) {
            if(mGroup.isSpecial()) {
                newGroup();
                return;
            }
            mCallbacks.onGroupSelected(mGroup.getID());
        }

        public void bind(Group group){
            mGroup = group;
            List<TextView> texts = new ArrayList<>();
            texts.add(mGroupNameTextView);
            texts.add(mDueTextView);
            texts.add(mOverdueTextView);
            mGroupNameTextView.setText(mGroup.getName());
            mDueTextView.setText(getResources().getString(R.string.group_due,mGroup.getDueToday()));
            mOverdueTextView.setText(getResources().getString(R.string.group_overdue,
                    mGroup.getOverdue()));
            View card = itemView.findViewById(R.id.card_layout);
            if(mGroup.isSpecial()){
                mGroupNameTextView.setText("");
                mDueTextView.setText(R.string.new_group);

                mDueTextView.setTextSize(40f);
                mDueTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                mDueTextView.setTextColor(getResources().getColor(R.color.gray));
                mEditButton.setVisibility(View.GONE);
                mOverdueTextView.setText("");
                card.setBackgroundResource(R.drawable.ic_card_new);
            }
            else {
                mDueTextView.setTextSize(24f);
                mDueTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                mEditButton.setVisibility(View.VISIBLE);
                card.setBackgroundResource(R.drawable.ic_card);
                card.getBackground().setColorFilter(Color.parseColor(mGroup.getColor()),
                        PorterDuff.Mode.DARKEN);
                int textColor = isDark(Color.parseColor(mGroup.getColor()))?
                        Color.parseColor("#ffffffff") :
                        Color.parseColor("#ff000000");
                for(TextView tv : texts){
                    tv.setTextColor(textColor);
                    ImageButton ib = itemView.findViewById(R.id.edit_group_button);
                    ib.setColorFilter(textColor,PorterDuff.Mode.SRC_ATOP);
                }
            }

        }

        public View getView() {
            return mView;
        }

        public void onItemSelected() {
        }

        public void onItemClear() {
            mCardView.getBackground().setColorFilter(Color.parseColor(mGroup.getColor()),
                    PorterDuff.Mode.DARKEN);
            turnOnAddCircle();
            mOverTrashcan = false;
            mDraggingGroupHolder=null;
            mDraggingColor=0x000000;
        }

        public void setColor(int iColor) {
            mCardView.getBackground().setColorFilter(iColor, PorterDuff.Mode.DARKEN);
        }

        public Group getGroup() {
            return mGroup;
        }
    }

    public static boolean isDark(int argb) {
        double rgb[] = new double[3];
        rgb[0] = (argb>>16)&0xFF;
        rgb[1] = (argb>>8)&0xFF;
        rgb[2] = (argb)&0xFF;
        for(int i = 0; i<rgb.length;i++){
            rgb[i] = rgb[i] / 255.0;
            if (rgb[i]<=0.03928){
                rgb[i] = rgb[i]/12.92;
            }
            else {
                rgb[i] = Math.pow(((rgb[i]+0.055)/1.055),2.4);
            }
        }
        double l = 0.2126 * rgb[0] +0.7152 * rgb[1] + 0.0722 * rgb[2];
        //int a = (argb>>24)&0xFF;
        return l < 0.179;
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder>
    {
        private List<Group> mGroups;

        public GroupAdapter(List<Group> groups) {
            mGroups = groups;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_group;
        }

        @NonNull
        @Override
        public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new GroupHolder(layoutInflater,parent,viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull final GroupHolder holder, int position) {
            Group group =  mGroups.get(position);
            holder.bind(group);



        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }

        public void setGroups(List<Group> groups) {
            mGroups = groups;
        }

        public boolean onItemMove(int fromPosition, int toPosition) {
            Log.i(TAG,"From Pos: "+fromPosition);
            Log.i(TAG,"To   Pos: "+toPosition);
            if(!mOverTrashcan && !(toPosition==mGroups.size()-1)) {
                Collections.swap(mGroups, fromPosition, toPosition);
                notifyItemMoved(fromPosition, toPosition);
            }
            return false;
        }

        public void onItemDismiss(GroupHolder groupHolder) {
            mGroups.remove(groupHolder.getAdapterPosition());
            notifyDataSetChanged();
            //notifyItemRemoved(groupHolder.getAdapterPosition());
            //notifyItemRangeChanged(groupHolder.getAdapterPosition(),getItemCount());
            //TODO improve animation
        }

        public List<Group> getGroups() {
            return mGroups;
        }
    }
}
