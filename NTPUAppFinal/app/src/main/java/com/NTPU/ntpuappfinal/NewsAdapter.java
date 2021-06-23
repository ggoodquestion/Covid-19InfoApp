package com.NTPU.ntpuappfinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<News> mData;
    private OnRecyclerViewClickListener listener;

    public NewsAdapter(){

    }

    public NewsAdapter(List<News> mData){
        this.mData = mData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        if(listener != null){
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClickListener(v);
                }
            });
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.imgCover.setImageDrawable(mData.get(position).cover);
        String preview;
        if(mData.get(position).content.length() > 40) preview = mData.get(position).content.trim().substring(0, 41) + "...";
        else preview = mData.get(position).content;
        holder.txtTitle.setText(mData.get(position).title);
        holder.txtContent.setText(preview);
    }

    @Override
    public int getItemCount() {
        if(mData == null) return 0;
        return mData.size();
    }

    public void updateAdapter(List<News> nData){
        this.mData = nData;
        notifyDataSetChanged();
    }

    public void setItemClickListener(OnRecyclerViewClickListener itemClickListener) {
        listener = itemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCover;
        TextView txtTitle, txtContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.img_cover);
            txtTitle = itemView.findViewById(R.id.txt_title);
            txtContent = itemView.findViewById(R.id.txt_content);
        }
    }

    public interface OnRecyclerViewClickListener{
        void onItemClickListener(View view);
    }
}
