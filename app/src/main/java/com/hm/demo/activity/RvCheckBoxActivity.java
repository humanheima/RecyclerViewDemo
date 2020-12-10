package com.hm.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.hm.demo.adapter.CheckBoxAdapter;
import com.hm.demo.databinding.ActivityRvCheckBoxBinding;
import com.hm.demo.model.CheckBoxModel;

import java.util.ArrayList;
import java.util.List;

public class RvCheckBoxActivity extends AppCompatActivity {

    private List<CheckBoxModel> data;
    private CheckBoxAdapter adapter;

    private ActivityRvCheckBoxBinding binding;

    public static void launch(Context context) {
        Intent starter = new Intent(context, RvCheckBoxActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRvCheckBoxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgFirst.setSelected(true);
        binding.imgSecond.setSelected(false);
        data = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            if (i == 0) {
                data.add(new CheckBoxModel("description:" + i, true));
            } else {
                data.add(new CheckBoxModel("description:" + i, false));
            }
        }
        adapter = new CheckBoxAdapter(this, data);
        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(adapter);
    }
}
