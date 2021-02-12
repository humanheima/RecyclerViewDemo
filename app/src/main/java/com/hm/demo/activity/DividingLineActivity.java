package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.hm.demo.R;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.databinding.ActivityDividingLineBinding;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;
import com.hm.demo.widget.LinearLayoutItemDecoration;

public class DividingLineActivity extends BaseActivity<ActivityDividingLineBinding> {

    public static void launch(Context context) {
        Intent intent = new Intent(context, DividingLineActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_dividing_line;
    }

    @Override
    protected void initData() {
        RecyclerView rv = viewBind.rv;
        //rv.setLayoutManager(new GridLayoutManager(this, 4));
        //rv.addItemDecoration(new DividerGridItemDecoration(this));
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.addItemDecoration(new LinearLayoutItemDecoration(this, LinearLayoutItemDecoration.VERTICAL_LIST));
        rv.setAdapter(new BaseAdapter<TestBean>(this, getTestData()) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                return R.layout.item_diff;
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, TestBean testBean, int position) {
                holder.setTextViewText(R.id.tv1, testBean.getName());
                holder.setTextViewText(R.id.tv2, testBean.getDesc());
                holder.setImageViewResource(R.id.iv, testBean.getPicture());
                holder.setOnItemClickListener(R.id.iv, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Toast.makeText(DividingLineActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
