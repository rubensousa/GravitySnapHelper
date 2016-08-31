package com.github.rubensousa.recyclerviewsnap;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<App> mApps;
    private boolean mHorizontal;

    public Adapter(boolean horizontal, List<App> apps) {
        mHorizontal = horizontal;
        mApps = apps;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mHorizontal ? new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter, parent, false)) :
                new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_vertical, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        App app = mApps.get(position);
        holder.imageView.setImageResource(app.getDrawable());
        holder.nameTextView.setText(app.getName());
        holder.ratingTextView.setText(String.valueOf(app.getRating()));

    }

    @Override
    public int getItemCount() {
        return mApps.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView nameTextView;
        public TextView ratingTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            nameTextView = (TextView) itemView.findViewById(R.id.nameTextView);
            ratingTextView = (TextView) itemView.findViewById(R.id.ratingTextView);
        }

    }

}

