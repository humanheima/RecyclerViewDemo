package com.hm.demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hm.demo.R;
import com.hm.demo.model.CountDownModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dumingwei on 2017/10/20.
 */
public class CountDownAdapter extends RecyclerView.Adapter<CountDownAdapter.CountDownHolder> {

    private List<CountDownModel> data;
    private List<CountDownHolder> holderList;

    public CountDownAdapter(List<CountDownModel> data) {
        this.data = data;
        holderList = new ArrayList<>();
    }

    @Override
    public CountDownHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_count_down, parent, false);
        return new CountDownHolder(view);
    }

    @Override
    public void onBindViewHolder(CountDownHolder holder, int position) {
        holder.setPosition(position);
        if (!holderList.contains(holder)) {
            holderList.add(holder);
        }
        holder.textTime.setText(data.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public void notifyData() {
        for (int i = 0; i < holderList.size(); i++) {
            holderList.get(i).textTime.setText(data.get(holderList.get(i).position).getTime());
        }
    }

    static class CountDownHolder extends RecyclerView.ViewHolder {

        private int position;
        TextView textTime;

        public void setPosition(int position) {
            this.position = position;
        }

        public CountDownHolder(View itemView) {
            super(itemView);
            textTime = itemView.findViewById(R.id.text_time);

        }
    }

}
