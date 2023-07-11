package com.erif.readmoretextview.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erif.readmoretextview.R;
import com.erif.readmoretextview.TextViewReadMore;
import com.erif.readmoretextview.model.ModelItemRecyclerView;

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
        if (holder instanceof Holder mHolder) {
            ModelItemRecyclerView item = list.get(position);
            mHolder.img.setImageResource(item.getImg());
            mHolder.text.setText(item.getText());
            mHolder.text.collapsed(item.isCollapsed());

            mHolder.text.onClickExpand(v -> mHolder.text.toggle());
            mHolder.text.onClickCollapse(v -> mHolder.text.toggle());
            mHolder.text.toggleListener(collapsed -> {
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
        private final TextViewReadMore text;
        private final ImageView img;
        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_recyclerView_txt);
            img = itemView.findViewById(R.id.item_recyclerView_img);
        }
    }

}
