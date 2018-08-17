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
    //TODO change drag so scroll is possible. Handle or long press. favoring handle at the moment
    private static final String TAG = "OverviewFragment";
    //TODO auto load if one group
    private RecyclerView mGroupRecyclerView;
    private Callbacks mCallbacks;
    private GroupAdapter mAdapter;
    private int mWidth;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton mTrashCan;
    private Group mDraggingGroup;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle sis){
        final View view = inflater.inflate(R.layout.fragment_overview,container,false);

        mTrashCan = view.findViewById(R.id.trashcan_circle);
        //mTrashCan.setVisibility(View.GONE);
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
            final float ALPHA_FULL = 1.0f;

            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                int swipeFlags = 0;

                GroupHolder groupHolder = (GroupHolder) viewHolder;
                int color = Color.parseColor(groupHolder.mGroup.getColor());
                color = darkenColor(color,0x33);
                groupHolder.mCardView.getBackground().setColorFilter(color,PorterDuff.Mode.DARKEN);
                mTrashCan.setVisibility(View.VISIBLE);
                mDraggingGroup = groupHolder.mGroup;
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
                    //viewHolder.itemView.setAlpha(alpha);
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

    private int darkenColor(int color,int darkenValue) {
        int[] rgb = getRGB(color);
        StringBuilder cHex = new StringBuilder().append("#ff");
        for(int c :rgb){
            //int c = rgb[i];
            c -= darkenValue;
            c = Math.max(0,c);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_overview,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_task:
                //TODO create drag drop create task
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
            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //TODO add group edit
                    //GroupLab.get(getContext()).removeGroup(mGroup);
                    //updateUI();
                    return true;
                }
            });*/


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
            //TODO third party item rearrange
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction()==MotionEvent.ACTION_DOWN){
                        return true;
                    }
                    if(event.getAction()==MotionEvent.ACTION_UP){
                        if(mDraggingGroup!=null){
                            int x = (int) event.getRawX();
                            int y = (int) event.getRawY();
                            if(isViewInBounds(mTrashCan,x,y)){
                                GroupLab.get(getContext()).removeGroup(mDraggingGroup);
                                updateUI();
                            }
                            mDraggingGroup=null;
                        }
                    }
                    return false;
                }
            });
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

        private boolean isViewInBounds(View view, int x, int y) {
            Rect outRect = new Rect();
            int[] location = new int[2];

            view.getDrawingRect(outRect);
            view.getLocationOnScreen(location);
            outRect.offset(location[0], location[1]);
            return outRect.contains(x, y);
        }

        public View getView() {
            return mView;
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
            mCardView.getBackground().setColorFilter(Color.parseColor(mGroup.getColor()),
                    PorterDuff.Mode.DARKEN);
            //TODO add fancy animation
            mTrashCan.setVisibility(View.GONE);
            //mDraggingGroup=null;
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
