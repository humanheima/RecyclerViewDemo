package com.hm.demo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;

import com.hm.demo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private SparseArray<String> sparseArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sparseArray = new SparseArray<>(3);
        sparseArray.put(1, "first");
        sparseArray.put(2, "second");
        sparseArray.put(3, "third");
    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_dividing_line:
                DividingLineActivity.launch(this);
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.btn_recycler_view_countdown)
    public void countDown() {
        RvCountDownActivity.launch(this);
    }

    @OnClick(R.id.btn_base)
    public void onBtnBaseClicked() {
        BaseRecyclerViewAdapterActivity.launch(this);
    }

    @OnClick(R.id.btn_diffutil)
    public void onBtnDiffutilClicked() {
        DiffUtilActivity.launch(this);
    }

    @OnClick(R.id.btn_pull_refresh)
    public void onBtnPullRefreshClicked() {
        PullRefreshLoadMoreActivity.launch(this);
    }

    @OnClick(R.id.btn_slide_delete)
    public void onBtnSlideDeleteClicked() {
        SlideDeleteActivity.launch(this);
    }

    @OnClick(R.id.btn_test_edittext)
    public void onBtnTestEdittextClicked() {
        TestEditTextActivity.launch(this);
    }

    @OnClick(R.id.btn_test_checkBoxInRv)
    public void testCheckBoxInRv() {
        RvCheckBoxActivity.launch(this);
    }

    void launchHeadFootActivity(View view) {
        HeadFootActivity.launch(this);
    }

}
