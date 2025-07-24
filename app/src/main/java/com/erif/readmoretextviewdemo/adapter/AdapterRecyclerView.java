package com.erif.readmoretextviewdemo.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.erif.readmoretextviewdemo.R;
import com.erif.readmoretextview.TextViewReadMore;
import com.erif.readmoretextviewdemo.model.ModelItemRecyclerView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            mHolder.imgProfile.setImageResource(item.getProfile());
            mHolder.text.setText(item.getText());
            mHolder.text.collapsed(item.isCollapsed());
            mHolder.txtName.setText(item.getName());
            mHolder.img.setImageResource(item.getImg());
            mHolder.img.setVisibility(item.isShowImage() ? View.VISIBLE : View.GONE);

            mHolder.text.onClickExpand(v -> mHolder.text.toggle());
            mHolder.text.onClickCollapse(v -> mHolder.text.toggle());
            mHolder.text.toggleListener(collapsed -> {
                item.setCollapsed(collapsed);
                update(position);
            });

            mHolder.img.setOnClickListener(v -> update(position));

            SimpleDateFormat format = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault());
            String date = format.format(new Date());
            mHolder.txtSubtitle.setText(date);

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
        private final ShapeableImageView imgProfile;
        private final TextView txtName;
        private final TextView txtSubtitle;
        private final ImageView img;
        public Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.item_recyclerView_txt);
            imgProfile = itemView.findViewById(R.id.item_recyclerView_imgProfile);
            txtName = itemView.findViewById(R.id.item_recyclerView_txtName);
            txtSubtitle = itemView.findViewById(R.id.item_recyclerView_txtSubtitle);
            img = itemView.findViewById(R.id.item_recyclerView_img);
        }
    }

}
