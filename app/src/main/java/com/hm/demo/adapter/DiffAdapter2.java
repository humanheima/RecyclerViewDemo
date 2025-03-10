package com.hm.demo.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.hm.demo.R;
import com.hm.demo.model.TestBean;

import java.util.List;

/**
 * Created by p_dmweidu on 2025/3/10
 * Desc: 直接继承自 ListAdapter
 */
public class DiffAdapter2 extends ListAdapter<TestBean, DiffAdapter.DiffVH> {

    private final String TAG = getClass().getSimpleName();
    private Context context;

//    public DiffAdapter2(List<TestBean> mDatas, Context context) {
//        this.mDatas = mDatas;
//        this.context = context;
//    }

    public DiffAdapter2(Context context, @NonNull DiffUtil.ItemCallback<TestBean> diffCallback) {
        super(diffCallback);
        this.context = context;
    }

    @Override
    public DiffAdapter.DiffVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diff_util, parent, false);
        return new DiffAdapter.DiffVH(view);
    }

    @Override
    public void onBindViewHolder(DiffAdapter.DiffVH holder, int position) {
        TestBean bean = getItem(position);
        holder.iv.setImageResource(bean.getPicture());
        holder.tvName.setText(bean.getName());
        holder.tvDesc.setText(bean.getDesc());
    }

    @Override
    public void onBindViewHolder(@NonNull DiffAdapter.DiffVH holder, int position, List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Bundle payload = (Bundle) payloads.get(0);
            TestBean bean = getItem(position);
            for (String key : payload.keySet()) {
                if (key.equals("KEY_DESC")) {
                    holder.tvDesc.setText(bean.getDesc());
                    break;
                }
                if (key.equals("KEY_PIC")) {
                    holder.iv.setImageResource(payload.getInt(key));
                }
            }
        }
    }


}
