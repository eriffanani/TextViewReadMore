package com.erif.readmoretextview.helper;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erif.readmoretextview.R;
import com.erif.readmoretextview.TextViewReadMore;

import java.util.ArrayList;
import java.util.List;

public class AdapterRecyclerView extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ModelItemRecyclerView> list = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new Holder(inflater.inflate(R.layout.item_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof Holder) {
            ModelItemRecyclerView item = list.get(position);
            ((Holder) holder).text.setText(item.getText());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<ModelItemRecyclerView> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    private static class Holder extends RecyclerView.ViewHolder {
        private final TextViewReadMore text;
        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_recyclerView_txt);
        }
    }

}
