package com.hm.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hm.demo.R;
import com.hm.demo.model.CheckBoxModel;
import java.util.List;

/**
 * Created by dumingwei on 2017/10/10.
 */
public class TestItemViewSpaceAdapter extends RecyclerView.Adapter<TestItemViewSpaceAdapter.ViewHolder> {

    private Context context;
    private List<CheckBoxModel> data;

    private int selectedPosition = 0;

    private static final String TAG = "TestItemViewSpaceAdapte";

    public TestItemViewSpaceAdapter(Context context, List<CheckBoxModel> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recycler_theory, parent, false);
        final ViewHolder holder = new ViewHolder(view);
//        if (selectedPosition == 1) {
//            holder.itemView.setVisibility(View.GONE);
//            Log.i(TAG, "onCreateViewHolder: holder.itemView.setVisibility(View.GONE)");
//        } else {
//            holder.itemView.setVisibility(View.VISIBLE);
//        }
//        holder.itemView.setTag("selectedPosition：" + selectedPosition);
//        selectedPosition++;
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckBoxModel model = data.get(position);
        holder.textDescription.setText(model.getDescription());
        if (selectedPosition == 1) {
            holder.itemView.setVisibility(View.GONE);
            //holder.textDescription.setVisibility(View.GONE);
            Log.i(TAG, "onCreateViewHolder: holder.itemView.setVisibility(View.GONE)");
        } else {
            //holder.textDescription.setVisibility(View.VISIBLE);
            holder.itemView.setVisibility(View.VISIBLE);
        }
        holder.itemView.setTag("selectedPosition：" + selectedPosition);
        selectedPosition++;
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView textDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            textDescription = itemView.findViewById(R.id.text_description);
        }
    }

}
