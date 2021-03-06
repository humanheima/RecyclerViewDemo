package com.hm.demo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R

inline fun <reified T : Activity> Context.startAct() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

class MainActivity : AppCompatActivity() {

    private var sparseArray: SparseArray<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sparseArray = SparseArray(3)
        sparseArray!!.put(1, "first")
        sparseArray!!.put(2, "second")
        sparseArray!!.put(3, "third")
    }

    fun click(view: View) {
        when (view.id) {

            R.id.btn_base -> BaseRecyclerViewAdapterActivity.launch(this)
            R.id.btn_slide_delete -> SlideDeleteActivity.launch(this)
            R.id.btn_test_edittext -> TestEditTextActivity.launch(this)

            R.id.btn_scroll_to_center -> ScrollToCenterActivity.launch(this)

            R.id.btn_float_item -> FloatItemActivity.launch(this)
            R.id.btn_dividing_line -> DividingLineActivity.launch(this)
            R.id.btn_time_line -> RecyclerViewTimeLineActivity.launch(this)
            R.id.btnDiffUtil -> DiffUtilActivity.launch(this)
            R.id.btn_pull_refresh -> PullRefreshLoadMoreActivity.launch(this)
            R.id.btn_horizontal_load_more -> HorizontalLoadMoreActivity.launch(this)
            R.id.btn_test_checkBoxInRv -> RvCheckBoxActivity.launch(this)
            R.id.btn_recycler_view_countdown -> RvCountDownActivity.launch(this)
        }
    }

    fun launchHeadFootActivity(view: View) {
        HeadFootActivity.launch(this)
    }

    fun launchPopWindow(view: View) {
        RecyclerViewPopWindowActivity.launch(this)
    }

    fun testLayout(view: View) {
        LayoutActivity.launch(this)
    }

}
