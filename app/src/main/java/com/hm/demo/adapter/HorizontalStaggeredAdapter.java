package com.hm.demo.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.hm.demo.R;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;
import java.util.List;


/**
 * Created by p_dmweidu on 2024/3/31
 * Desc: 用来测试 Copy StaggeredGridLayoutManager 横向布局时候的效果
 */
public class HorizontalStaggeredAdapter extends BaseAdapter<TestBean> {

    private int[] colorArray = new int[4];
    public HorizontalStaggeredAdapter(Context context, List<TestBean> data) {
        super(context, data);
        colorArray[0] = context.getResources().getColor(R.color.color_FFF4F4, null);
        colorArray[1] = context.getResources().getColor(R.color.color_FFF9E7, null);
        colorArray[2] = context.getResources().getColor(R.color.color_EEFCF4, null);
        colorArray[3] = context.getResources().getColor(R.color.color_EDF8FC, null);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (headView != null) {
            position = position - 1;
        }
        int realSize = dataList.size();
        int index = position % realSize;
        return getHolderType(position, dataList.get(index));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (headView != null) {
            position = position - 1;
        }
        int realSize = dataList.size();
        int index = position % realSize;

        if (index < realSize && !(holder instanceof SpecialViewHolder)) {
            bindViewHolder(((BaseViewHolder) holder), dataList.get(index), index);
        }
    }

    @Override
    public void bindViewHolder(BaseViewHolder holder, TestBean testBean, int position) {
        holder.setTextViewText(R.id.tv_name, testBean.getName());
        int colorIndex = position % (colorArray.length);
        holder.setViewBg(R.id.tv_name, colorArray[colorIndex]);
        holder.setOnItemClickListener(R.id.tv_name, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int realSize = dataList.size();
                int index = position % realSize;
                String name = dataList.get(index).getDesc();
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getHolderType(int position, TestBean testBean) {
        return R.layout.item_diff_staggered_layout;
    }
}