package com.erif.readmoretextview.helper;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erif.readmoretextview.R;
import com.erif.readmoretextview.TextViewReadMore2;

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
            Holder mHolder = (Holder) holder;
            ModelItemRecyclerView item = list.get(position);
            mHolder.text.setText(item.getText());
            mHolder.text.collapsed(item.isCollapsed());
            mHolder.text.onClickExpand(v -> mHolder.text.toggle());
            mHolder.text.onClickCollapse(v -> mHolder.text.toggle());

            mHolder.text.actionListener(collapsed -> {
                item.setCollapsed(collapsed);
                update(position);
            });

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

    private void update(int position) {
        notifyItemChanged(position);
    }

    private static class Holder extends RecyclerView.ViewHolder {
        private final TextViewReadMore2 text;
        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_recyclerView_txt);
        }
    }

}
