package com.hm.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hm.demo.R;
import com.hm.demo.model.CheckBoxModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by dumingwei on 2017/10/10.
 */
public class CheckBoxAdapter extends RecyclerView.Adapter<CheckBoxAdapter.ViewHolder> {

    private Context context;
    private List<CheckBoxModel> data;

    private int selectedPosition = -1;

    public CheckBoxAdapter(Context context, List<CheckBoxModel> data) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkbox, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.imgCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedPosition != holder.getAdapterPosition()) {
                    data.get(selectedPosition).setChecked(false);
                    notifyItemChanged(selectedPosition);
                    selectedPosition = holder.getAdapterPosition();
                    data.get(selectedPosition).setChecked(true);
                    notifyItemChanged(selectedPosition);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CheckBoxModel model = data.get(position);
        holder.textDescription.setText(model.getDescription());
        holder.imgCheckBox.setSelected(model.isChecked());
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_description)
        TextView textDescription;
        @BindView(R.id.img_check_box)
        ImageView imgCheckBox;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
