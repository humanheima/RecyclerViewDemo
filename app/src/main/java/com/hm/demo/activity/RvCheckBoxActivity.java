package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import com.hm.demo.R;
import com.hm.demo.adapter.CheckBoxAdapter;
import com.hm.demo.model.CheckBoxModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RvCheckBoxActivity extends AppCompatActivity {


    @BindView(R.id.img_first)
    ImageView imgFirst;
    @BindView(R.id.img_second)
    ImageView imgSecond;
    @BindView(R.id.rv)
    RecyclerView rv;
    private List<CheckBoxModel> data;
    private CheckBoxAdapter adapter;

    public static void launch(Context context) {
        Intent starter = new Intent(context, RvCheckBoxActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rv_check_box);
        ButterKnife.bind(this);
        imgFirst.setSelected(true);
        imgSecond.setSelected(false);
        data = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            if (i == 0) {
                data.add(new CheckBoxModel("description:" + i, true));
            } else {
                data.add(new CheckBoxModel("description:" + i, false));
            }
        }
        adapter = new CheckBoxAdapter(this, data);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }
}
