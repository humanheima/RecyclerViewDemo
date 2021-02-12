package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.hm.demo.R;
import com.hm.demo.base.BaseActivity;
import com.hm.demo.base.BaseAdapter;
import com.hm.demo.base.BaseViewHolder;
import com.hm.demo.interfaces.OnItemClickListener;
import com.hm.demo.model.TestBean;

import java.util.List;

/**
 * 点击RecyclerView的每个item出现popwindow
 */
public class RecyclerViewPopWindowActivity extends BaseActivity {

    //打开关闭弹出框
    protected final int OPEN_POP = 0;
    protected final int HIDE_POP = 1;

    private List<TestBean> testData;
    private MyPopWindow popWindow;
    RecyclerView rv;
    private TextView tvDelete;

    public static void launch(Context context) {
        Intent starter = new Intent(context, RecyclerViewPopWindowActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int bindLayout() {
        return R.layout.activity_recycler_view_pop_window;
    }

    @Override
    protected void initData() {
        testData = getTestData();
        initPop();
        rv = findViewById(R.id.rv_popwindow);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new BaseAdapter<TestBean>(this, testData) {

            @Override
            public int getHolderType(int position, TestBean testBean) {
                return R.layout.item_slide_delete;
            }

            @Override
            public void bindViewHolder(BaseViewHolder holder, final TestBean testBean, int position) {
                holder.setTextViewText(R.id.tv1, testBean.getDesc());
                holder.setTextViewText(R.id.tv2, testBean.getName());
                holder.setImageViewResource(R.id.iv, testBean.getPicture());
                holder.setOnItemClickListener(R.id.tv2, new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        testData.remove(position);
                        notifyItemRemoved(position);
                    }
                });
                holder.setOnItemClickListener(R.id.tv1, new OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onItemClick(View view, int position) {
                        int[] location = new int[2];
                        view.getLocationInWindow(location);
                        Log.d("TAG", "点击的item的高度:" + view.getHeight() + "x值:" + location[0] + "y值" + location[1]);

                        //popWindow.showAsDropDown(view, 0, 0, Gravity.CENTER);
                        popWindow.show(position, view, 0, 0);
                        //popWindow.showAtLocation(view, Gravity.TOP,0,10);
                        //Toast.makeText(RecyclerViewPopWindowActivity.this, "onItemClick position=" + position, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void initPop() {
        if (popWindow == null) {
            View convertView = LayoutInflater.from(this).inflate(R.layout.item_delete, null);
            tvDelete = convertView.findViewById(R.id.tv_delete);
            tvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(RecyclerViewPopWindowActivity.this, "onItemClick position=" + popWindow.getPosition(), Toast.LENGTH_SHORT).show();
                    popWindow.dismiss();
                }
            });
            popWindow = new MyPopWindow(convertView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popWindow.setFocusable(true);
            popWindow.setBackgroundDrawable(new ColorDrawable());
        }
    }


    class MyPopWindow extends PopupWindow {


        int position;

        public int getPosition() {
            return position;
        }

        public MyPopWindow(View contentView, int width, int height, boolean focusable) {
            super(contentView, width, height, focusable);
        }

        public void show(int position, View anchor, int xoff, int yoff) {
            this.position = position;
            showAsDropDown(anchor, xoff, yoff);
        }

        @Override
        public void showAsDropDown(View anchor) {
            setWindow(OPEN_POP);
            super.showAsDropDown(anchor);
        }

        @Override
        public void showAtLocation(View parent, int gravity, int x, int y) {
            setWindow(OPEN_POP);
            super.showAtLocation(parent, gravity, x, y);
        }

        @Override
        public void dismiss() {
            setWindow(HIDE_POP);
            super.dismiss();
        }
    }

    public void setWindow(int type) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        if (type == OPEN_POP) {
            lp.alpha = 0.7f;
        } else {
            lp.alpha = 1.0f;
        }
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

}
