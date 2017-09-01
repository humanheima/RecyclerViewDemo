package com.example.loadmoredemo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.impl.TextWatcherAdapter;
import com.example.loadmoredemo.model.Goods;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final TestEditTextAdapter.ViewHolder holder, final int position) {
        final Goods goods = goodsList.get(position);
        holder.textGoodsName.setText(goods.getName());
        holder.editGoodsPrice.setTag(goodsList.indexOf(goods));
        if (holder.editGoodsPrice.getTag() == (Integer)goodsList.indexOf(goods)) {
            holder.editGoodsPrice.setText(format.format(goods.getPrice()));
        }
        holder.editGoodsPrice.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);
                String priceStr = s.toString();
                if (TextUtils.isEmpty(priceStr)) {
                    priceStr = "0.00";
                    holder.editGoodsPrice.setText(priceStr);
                }
                if (priceStr.matches("0+//.00") && !priceStr.matches("[1-9]+0+//.00")) {
                    priceStr = "0.00";
                    holder.editGoodsPrice.setText(priceStr);
                }
                if (holder.editGoodsPrice.getTag() == (Integer)goodsList.indexOf(goods)) {
                    onPriceChangeListener.change(position, Double.valueOf(holder.editGoodsPrice.getText().toString()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return goodsList == null ? 0 : goodsList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_goods_name)
        TextView textGoodsName;
        @BindView(R.id.edit_goods_price)
        EditText editGoodsPrice;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnPriceChangeListener {
        void change(int position, double price);
    }
}
