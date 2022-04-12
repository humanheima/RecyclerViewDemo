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
public class SimpleAdapterAdapter extends RecyclerView.Adapter<SimpleAdapterAdapter.ViewHolder> {

    private Context context;
    private List<CheckBoxModel> data;

    private int selectedPosition = -1;

    private static final String TAG = "SimpleAdapterAdapter";

    public SimpleAdapterAdapter(Context context, List<CheckBoxModel> data) {
        this.context = context;
        this.data = data;
        for (int i = 0; i < data.size(); i++) {
            CheckBoxModel model = data.get(i);
            if (model.isChecked()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recycler_theory, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        Log.e(TAG, "onCreateViewHolder: holder position = " + holder.getAdapterPosition() + " child count =" + parent.getChildCount());
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckBoxModel model = data.get(position);
        holder.textDescription.setText(model.getDescription());
        Log.i(TAG, "onBindViewHolder: position = " + position + " holder = " + holder.toString());
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
