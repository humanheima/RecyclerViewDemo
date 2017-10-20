package com.example.loadmoredemo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.loadmoredemo.R;
import com.example.loadmoredemo.Util.DateUtil;
import com.example.loadmoredemo.adapter.CountDownAdapter;
import com.example.loadmoredemo.model.CountDownModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 测试RecyclerView中每个item 倒计时的功能
 */
public class RvCountDownActivity extends AppCompatActivity {

    private static final String TAG = "RvCountDownActivity";
    @BindView(R.id.rv)
    RecyclerView rv;
    private List<CountDownModel> data;
    public static final int WHAT = 1;
    private CountDownAdapter adapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT:
                    //刷新数据
                    adapter.notifyData();
                    break;
            }
        }
    };
    private CountDownThread thread;

    public static void launch(Context context) {
        Intent starter = new Intent(context, RvCountDownActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_count_down);
        ButterKnife.bind(this);
        data = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            CountDownModel model = new CountDownModel();
            model.setCountTime(DateUtil.DAY_MILL_SECOND + i * DateUtil.HOUR_MILL_SECOND);
            model.setTime("5天3时3分50秒");
            data.add(model);
        }
        adapter=new CountDownAdapter(data);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
        thread = new CountDownThread(data);
        thread.start();
    }

    private class CountDownThread extends Thread {

        private List<CountDownModel> data;

        public CountDownThread(List<CountDownModel> data) {
            this.data = data;
        }

        @Override
        public void run() {

            while (!isInterrupted()) {
                try {
                    Thread.sleep(1000);
                    for (int i = 0; i < data.size(); i++) {
                        StringBuilder builder = new StringBuilder();
                        long countTime = data.get(i).getCountTime();
                        int days = (int) (countTime / DateUtil.DAY_MILL_SECOND);
                        int hours = (int) ((countTime - days * DateUtil.DAY_MILL_SECOND) / DateUtil.HOUR_MILL_SECOND);
                        int minutes = (int) ((countTime - days * DateUtil.DAY_MILL_SECOND - hours * DateUtil.HOUR_MILL_SECOND) / DateUtil.MINUTE_MILL_SECOND);
                        int seconds = (int) ((countTime - days * DateUtil.DAY_MILL_SECOND - hours * DateUtil.HOUR_MILL_SECOND - minutes * DateUtil.MINUTE_MILL_SECOND) / DateUtil.SECOND_MILL_SECOND);
                        builder.append(days).append("天").append(hours).append("时").append(minutes).append("分").append(seconds).append("秒");
                        data.get(i).setTime(builder.toString());
                        if (countTime > DateUtil.SECOND_MILL_SECOND) {
                            data.get(i).setCountTime(countTime - DateUtil.SECOND_MILL_SECOND);
                        }
                    }
                    Message message = handler.obtainMessage(WHAT);
                    handler.sendMessage(message);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "interrupted");
                    interrupt();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        thread.interrupt();
    }
}
