package com.albertsu.helloupload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

import cn.wswin.util.upload.UploadInfo;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.MyViewHolder> {

    private Context activity;
    private List<UploadInfo> mData = new ArrayList<>();

    public UploadAdapter(Context activity) {
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pic_transf, parent, false);
        return new UploadAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final UploadInfo uploadInfo = mData.get(position);

        holder.image.setImageResource(R.mipmap.ic_launcher);
        holder.tv_name.setText(uploadInfo.getName());
        holder.tv_staus.setText(UploadInfo.statusTxt[uploadInfo.getState()] +" | "+ uploadInfo.getCurrentLength());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void addData(UploadInfo data) {
        mData.add(data);
        notifyDataSetChanged();
    }

    public void addNewData(List<UploadInfo> data) {
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }


     class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView tv_name,tv_staus;

        private MyViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_staus = itemView.findViewById(R.id.tv_status);
        }
    }
}

