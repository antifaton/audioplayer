package com.ant.audioplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AdapterRecycler extends RecyclerView.Adapter {

    private static onItemClickListener onItemClickListener;
    private static final String TAG = "AdapterRecyclerClass";
    private final List<ListItem> dataSet;

    public AdapterRecycler(List<ListItem> dataSet){
        this.dataSet = dataSet;
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) instanceof Folder) {
            return RowType.PLAYER_FOLDER;
        } else if (dataSet.get(position) instanceof Song) {
            return RowType.PLAYER_SONG;
        } else {
            return -1;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == RowType.PLAYER_FOLDER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new FolderViewHolder(view);
        } else if (viewType == RowType.PLAYER_SONG){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new SongViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FolderViewHolder){
            ((FolderViewHolder) holder).textView.setText(dataSet.get(position).getName());
        } else if (holder instanceof SongViewHolder){
            ((SongViewHolder) holder).textView.setText(dataSet.get(position).getName());
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public static class FolderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;

        public FolderViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dataPath);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.dataPath);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(onItemClickListener onItemClickListener){
        AdapterRecycler.onItemClickListener = onItemClickListener;
    }

    public interface onItemClickListener {
        void onItemClick(int position, View v);
    }
}
