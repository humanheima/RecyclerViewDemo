package com.hm.demo.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.hm.demo.R;
import com.hm.demo.impl.TextWatcherAdapter;
import com.hm.demo.model.Goods;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by dumingwei on 2017/9/1.
 */
public class TestEditTextAdapter extends RecyclerView.Adapter<TestEditTextAdapter.ViewHolder> {

    private final String TAG = getClass().getSimpleName();
    private Context context;
    private List<Goods> goodsList;
    private DecimalFormat format;
    private OnPriceChangeListener onPriceChangeListener;

    public TestEditTextAdapter(Context context, List<Goods> goodsList) {
        this.context = context;
        this.goodsList = goodsList;
        format = new DecimalFormat("#0.00");
    }

    public void setOnPriceChangeListener(OnPriceChangeListener onPriceChangeListener) {
        this.onPriceChangeListener = onPriceChangeListener;
    }

    @Override
    public TestEditTextAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_test_edittext, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.editGoodsPrice.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                Log.e("afterTextChanged", s.toString());
                String priceStr = s.toString();
                if (TextUtils.isEmpty(priceStr)) {
                    priceStr = "0.00";
                    holder.editGoodsPrice.setText(priceStr);
                }
                if (priceStr.matches("0+//.00") && !priceStr.matches("[1-9]+0+//.00")) {
                    priceStr = "0.00";
                    holder.editGoodsPrice.setText(priceStr);
                }
                if (onPriceChangeListener != null) {
                    onPriceChangeListener.change(holder.getAdapterPosition(), Double.valueOf(holder.editGoodsPrice.getText().toString()));
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final TestEditTextAdapter.ViewHolder holder, final int position) {
        final Goods goods = goodsList.get(position);
        holder.textGoodsName.setText(goods.getName());
        holder.editGoodsPrice.setText(new DecimalFormat("#0.00").format(goods.getPrice()));

    }

    @Override
    public int getItemCount() {
        return goodsList == null ? 0 : goodsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textGoodsName;
        EditText editGoodsPrice;

        ViewHolder(View view) {
            super(view);
            textGoodsName = view.findViewById(R.id.text_goods_name);
            editGoodsPrice = view.findViewById(R.id.edit_goods_price);
        }
    }

    public interface OnPriceChangeListener {
        void change(int position, double price);
    }
}
