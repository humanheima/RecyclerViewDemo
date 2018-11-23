package com.hm.demo.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.demo.R;
import com.hm.demo.model.TestBean;

import java.util.List;

/**
 * Created by dumingwei on 2017/4/18.
 */
public class DiffAdapter extends RecyclerView.Adapter<DiffAdapter.DiffVH> {

    private final String TAG = getClass().getSimpleName();
    private List<TestBean> mDatas;
    private Context context;

    public void setmDatas(List<TestBean> mDatas) {
        this.mDatas = mDatas;
    }

    public DiffAdapter(List<TestBean> mDatas, Context context) {
        this.mDatas = mDatas;
        this.context = context;
    }

    @Override
    public DiffVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diff, parent, false);
        return new DiffVH(view);
    }

    @Override
    public void onBindViewHolder(DiffVH holder, int position) {
        TestBean bean = mDatas.get(position);
        holder.tv1.setText(bean.getName());
        holder.tv2.setText(bean.getDesc());
        holder.iv.setImageResource(bean.getPicture());
    }

    @Override
    public void onBindViewHolder(DiffVH holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Bundle payload = (Bundle) payloads.get(0);
            TestBean bean = mDatas.get(position);
            for (String key : payload.keySet()) {
                if (key.equals("KEY_DESC")) {
                    holder.tv2.setText(bean.getDesc());
                    break;
                }
                if (key.equals("KEY_PIC")) {
                    holder.iv.setImageResource(payload.getInt(key));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    class DiffVH extends RecyclerView.ViewHolder {
        TextView tv1, tv2;
        ImageView iv;

        public DiffVH(View itemView) {
            super(itemView);
            tv1 = (TextView) itemView.findViewById(R.id.tv1);
            tv2 = (TextView) itemView.findViewById(R.id.tv2);
            iv = (ImageView) itemView.findViewById(R.id.iv);
        }
    }
}