package com.hm.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.hm.demo.R;
import com.hm.demo.model.CheckBoxModel;
import java.util.List;

/**
 * Created by dumingwei on 2017/10/10.
 */
public class TestAnimatorAdapterAdapter extends RecyclerView.Adapter<TestAnimatorAdapterAdapter.ViewHolder> {

    private Context context;
    private List<CheckBoxModel> data;

    private static final String TAG = "TestAnimatorAdapterAdap";

    public TestAnimatorAdapterAdapter(Context context, List<CheckBoxModel> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_test_animation, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckBoxModel model = data.get(position);
        holder.checkBox.setSelected(model.isChecked());
        holder.textDescription.setText(model.getDescription());
        Log.i(TAG, "onBindViewHolder: position = " + position + " holder = " + holder + " model = " + model);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CheckBox checkBox;
        TextView textDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_box);
            textDescription = itemView.findViewById(R.id.text_description);
        }
    }

}
