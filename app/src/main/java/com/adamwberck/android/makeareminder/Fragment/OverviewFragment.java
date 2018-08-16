package com.adamwberck.android.makeareminder.Fragment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.DragEvent;
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
    //TODO change drag so scroll is possible. Handle or long press. favoring handle at the moment
    private static final String TAG = "OverviewFragment";
    //TODO auto load if one group
    private RecyclerView mGroupRecyclerView;
    private Callbacks mCallbacks;
    private GroupAdapter mAdapter;
    private int mWidth;
    private ItemTouchHelper mItemTouchHelper;

    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    public interface Callbacks {
        void onGroupSelected(UUID id);
        void onGroupEdit(UUID id);
    }

    public static Fragment newInstance() {
        Bundle args =  new Bundle();

        OverviewFragment fragment = new OverviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        Log.i(TAG,"Created");
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume(){
        super.onResume();
        updateUI();
    }

    public interface ItemTouchHelperAdapter {
        boolean onItemMove(int fromPosition, int toPosition);
        void onItemDismiss(int position);
    }

    private interface ItemTouchHelperViewHolder {

        /**
         * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
         * Implementations should update the item view to indicate it's active state.
         */
        void onItemSelected();


        /**
         * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active item
         * state should be cleared.
         */
        void onItemClear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle sis){
        final View view = inflater.inflate(R.layout.fragment_overview,container,false);

        mGroupRecyclerView = view.findViewById(R.id.group_recycler_view);

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
            public final float ALPHA_FULL = 1.0f;

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = 0;
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
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    // Fade out the view as it is swiped out of the parent's bounds
                    final float alpha = ALPHA_FULL - Math.abs(dX)
                            / (float) viewHolder.itemView.getWidth();
                    viewHolder.itemView.setAlpha(alpha);
                    viewHolder.itemView.setTranslationX(dX);
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState,
                            isCurrentlyActive);
                }
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                // We only want the active item to change
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    if (viewHolder instanceof ItemTouchHelperViewHolder) {
                        // Let the view holder know that this item is being moved or dragged
                        ItemTouchHelperViewHolder itemViewHolder =
                                (ItemTouchHelperViewHolder) viewHolder;
                        itemViewHolder.onItemSelected();
                    }
                }

                super.onSelectedChanged(viewHolder, actionState);
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);

                viewHolder.itemView.setAlpha(ALPHA_FULL);

                if (viewHolder instanceof ItemTouchHelperViewHolder) {
                    // Tell the view holder it's time to restore the idle state
                    ItemTouchHelperViewHolder itemViewHolder
                            = (ItemTouchHelperViewHolder) viewHolder;
                    itemViewHolder.onItemClear();
                }
            }
        };
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mGroupRecyclerView);
        return view;
    }

    private int getColumns(int width) {
        Log.i(TAG,"width: " + width);
        return width/200;
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
                        //int position = viewHolder.getAdapterPosition();
                        ///Group group = mAdapter.mTasks.get(position);
                        //mDeleteCallBack.onTaskIdSelected(task.getID());
                        //TaskLab.get(getActivity()).removeTask(task);
                    }
                };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouchCallback);

        itemTouchHelper.attachToRecyclerView(mGroupRecyclerView);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_overview,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_task:
                //TODO create drag drop
                return super.onOptionsItemSelected(item);
            case R.id.new_group:
                newGroup();
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        GroupLab.get(getContext()).addGroup(new Group());
        GroupLab.saveLab();
        updateUI();
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
            implements View.OnClickListener,ItemTouchHelperViewHolder {
        private final ImageButton mEditButton;
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
            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //TODO add group edit
                    //GroupLab.get(getContext()).removeGroup(mGroup);
                    //updateUI();
                    return true;
                }
            });*/
            itemView.setOnDragListener(new View.OnDragListener() {
                @Override
                public boolean onDrag(View v, DragEvent event) {
                    //TODO add drag rearrange
                    return false;
                }
            });


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

        public View getView() {
            return mView;
        }

        @Override
        public void onItemSelected() {
            //itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            //itemView.setBackgroundColor(0);
        }
    }

    private boolean isDark(int argb) {
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
        implements ItemTouchHelperAdapter
    {
        private List<Group> mGroups;

        public GroupAdapter(List<Group> groups) {
            mGroups = groups;
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.list_item_group;
        }
        /*
        public void setGroups(List<Group> groups) {
            mGroups = groups;
        }*/

        //@Override
        //public boolean onItemMove(int from,int to){
        //    return true;
        //}


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

            holder.getView().setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        onStartDrag(holder);
                    }
                    return false;
                }
            });

        }

        @Override
        public int getItemCount() {
            return mGroups.size();
        }

        public void setGroups(List<Group> groups) {
            mGroups = groups;
        }

        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            Collections.swap(mGroups, fromPosition, toPosition);
            notifyItemMoved(fromPosition, toPosition);
            return false;
        }

        @Override
        public void onItemDismiss(int position) {

        }
    }
}
