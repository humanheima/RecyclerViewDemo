package com.hm.demo.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.hm.demo.R

class MainActivity : AppCompatActivity() {

    private var sparseArray: SparseArray<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        sparseArray = SparseArray(3)
        sparseArray!!.put(1, "first")
        sparseArray!!.put(2, "second")
        sparseArray!!.put(3, "third")
    }

    fun click(view: View) {
        when (view.id) {
            R.id.btn_dividing_line -> DividingLineActivity.launch(this)
            R.id.btn_time_line -> RecyclerViewTimeLineActivity.launch(this)
            else -> {
            }
        }
    }

    @OnClick(R.id.btn_recycler_view_countdown)
    fun countDown() {
        RvCountDownActivity.launch(this)
    }

    @OnClick(R.id.btn_base)
    fun onBtnBaseClicked() {
        BaseRecyclerViewAdapterActivity.launch(this)
    }

    @OnClick(R.id.btn_diffutil)
    fun onBtnDiffutilClicked() {
        DiffUtilActivity.launch(this)
    }

    @OnClick(R.id.btn_pull_refresh)
    fun onBtnPullRefreshClicked() {
        PullRefreshLoadMoreActivity.launch(this)
    }

    @OnClick(R.id.btn_slide_delete)
    fun onBtnSlideDeleteClicked() {
        SlideDeleteActivity.launch(this)
    }

    @OnClick(R.id.btn_test_edittext)
    fun onBtnTestEdittextClicked() {
        TestEditTextActivity.launch(this)
    }

    @OnClick(R.id.btn_test_checkBoxInRv)
    fun testCheckBoxInRv() {
        RvCheckBoxActivity.launch(this)
    }

    fun launchHeadFootActivity(view: View) {
        HeadFootActivity.launch(this)
    }

    fun launchPopwindow(view: View) {
        RecyclerViewPopWindowActivity.launch(this)
    }

    fun testLayout(view: View) {
        LayoutActivity.launch(this)
    }

}
