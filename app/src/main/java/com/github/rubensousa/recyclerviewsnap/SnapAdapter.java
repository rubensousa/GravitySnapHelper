package com.github.rubensousa.recyclerviewsnap;


import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rubensousa.gravitysnaphelper.GravityPagerSnapHelper;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SnapAdapter extends RecyclerView.Adapter<SnapAdapter.ViewHolder> implements GravitySnapHelper.SnapListener {

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private ArrayList<Snap> snaps;

    public SnapAdapter() {
        snaps = new ArrayList<>();
    }

    public void addSnap(Snap snap) {
        snaps.add(snap);
    }

    @Override
    public int getItemViewType(int position) {
        Snap snap = snaps.get(position);
        switch (snap.getGravity()) {
            case Gravity.CENTER_VERTICAL:
                return VERTICAL;
            case Gravity.CENTER_HORIZONTAL:
                return HORIZONTAL;
            case Gravity.START:
                return HORIZONTAL;
            case Gravity.TOP:
                return VERTICAL;
            case Gravity.END:
                return HORIZONTAL;
            case Gravity.BOTTOM:
                return VERTICAL;
        }
        return HORIZONTAL;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = viewType == VERTICAL ? LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_snap_vertical, parent, false)
                : LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_snap, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(snaps.get(position));
        Snap snap = snaps.get(position);
        int padding = holder.recyclerView.getResources().getDimensionPixelOffset(R.dimen.extra_padding);
        if (snap.getPadding()) {
            if (snap.getGravity() == Gravity.START) {
                holder.recyclerView.setPadding(padding, 0, padding, 0);
            } else if (snap.getGravity() == Gravity.END) {
                holder.recyclerView.setPadding(padding, 0, padding, 0);
            }
        } else {
            holder.recyclerView.setPadding(0, 0, 0, 0);
        }

        if (snap.getGravity() == Gravity.START || snap.getGravity() == Gravity.END) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder
                    .recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
            new GravitySnapHelper(snap.getGravity(), false, this).attachToRecyclerView(holder.recyclerView);
        } else if (snap.getGravity() == Gravity.CENTER_HORIZONTAL ||
                snap.getGravity() == Gravity.CENTER_VERTICAL) {
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder
                    .recyclerView.getContext(), snap.getGravity() == Gravity.CENTER_HORIZONTAL ?
                    RecyclerView.HORIZONTAL : RecyclerView.VERTICAL, false));
            new LinearSnapHelper().attachToRecyclerView(holder.recyclerView);
        } else if (snap.getGravity() == Gravity.CENTER) { // Pager snap
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder
                    .recyclerView.getContext(), RecyclerView.HORIZONTAL, false));
            new GravityPagerSnapHelper(Gravity.START).attachToRecyclerView(holder.recyclerView);
        } else { // Top / Bottom
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder
                    .recyclerView.getContext()));
            new GravitySnapHelper(snap.getGravity()).attachToRecyclerView(holder.recyclerView);
        }


        holder.recyclerView.setAdapter(new Adapter(snap.getGravity() == Gravity.START
                || snap.getGravity() == Gravity.END
                || snap.getGravity() == Gravity.CENTER_HORIZONTAL,
                snap.getGravity() == Gravity.CENTER, snap.getApps()));
    }

    @Override
    public int getItemCount() {
        return snaps.size();
    }

    @Override
    public void onSnap(int position) {
        Log.d("Snapped: ", position + "");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView snapTextView;
        public RecyclerView recyclerView;
        public MaterialButton scrollButton;
        private Snap snap;

        public ViewHolder(View itemView) {
            super(itemView);
            snapTextView = itemView.findViewById(R.id.snapTextView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
            scrollButton = itemView.findViewById(R.id.scrollButton);
            scrollButton.setOnClickListener(this);
        }

        public void bind(Snap snap) {
            this.snap = snap;
            if ((snap.getGravity() == Gravity.START || snap.getGravity() == Gravity.END)
                    && !snap.getPadding()) {
                scrollButton.setVisibility(View.VISIBLE);
            } else {
                scrollButton.setVisibility(View.INVISIBLE);
            }
            snapTextView.setText(snap.getText());
        }

        @Override
        public void onClick(View v) {
            RecyclerView.OnFlingListener listener = recyclerView.getOnFlingListener();
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (listener instanceof GravitySnapHelper
                    && layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager lm = (LinearLayoutManager) layoutManager;
                int firstVisiblePosition = snap.getGravity() == Gravity.START ?
                        lm.findFirstCompletelyVisibleItemPosition()
                        : lm.findLastCompletelyVisibleItemPosition();
                if (firstVisiblePosition != RecyclerView.NO_POSITION) {
                    ((GravitySnapHelper) listener).smoothScrollToPosition(
                            firstVisiblePosition + 1);
                }
            }
        }
    }
}

