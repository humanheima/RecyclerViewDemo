package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hm.demo.R;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.databinding.ActivityBaseRecyclerviewAdapterBinding;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;

public class BaseRecyclerViewAdapterActivity extends BaseActivity<ActivityBaseRecyclerviewAdapterBinding> {


    private static final String TAG = "BaseRecyclerViewAdapter";

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    public static void launch(Context context) {
        Intent starter = new Intent(context, BaseRecyclerViewAdapterActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_base_recyclerview_adapter;
    }

    @Override
    protected void initData() {
        recyclerView = viewBind.recyclerView;
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new BaseAdapter<TestBean>(this, getTestData()) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                if (testBean.getName().endsWith("1") || testBean.getName().endsWith("0")) {
                    return R.layout.item_diff;
                } else {
                    return R.layout.item_diff_copy;
                }
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean, int position) {
                if (holder.getItemViewType() == R.layout.item_diff) {
                    holder.setTextViewText(R.id.tv1, testBean.getName());
                    holder.setTextViewText(R.id.tv2, testBean.getDesc());
                    holder.setImageViewResource(R.id.iv, testBean.getPicture());
                    holder.setOnItemClickListener(R.id.iv, new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Toast.makeText(BaseRecyclerViewAdapterActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (holder.getItemViewType() == R.layout.item_diff_copy) {
                    holder.setTextViewText(R.id.tv1, testBean.getDesc());
                    holder.setTextViewText(R.id.tv2, testBean.getName());
                    holder.setImageViewResource(R.id.iv, testBean.getPicture());
                    holder.setOnItemClickListener(R.id.tv1, new OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            Toast.makeText(BaseRecyclerViewAdapterActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


}
