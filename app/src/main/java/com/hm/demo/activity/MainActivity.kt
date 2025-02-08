package com.hm.demo.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hm.demo.R
import com.hm.demo.databinding.ActivityMainBinding
import com.hm.demo.rv_nest_rv.RvNestRvActivity
import com.hm.demo.test_diff.MyDiffUtilActivity
import com.hm.demo.test_payload.PayloadTestActivity

inline fun <reified T : Activity> Context.startAct() {
    val intent = Intent(this, T::class.java)
    startActivity(intent)
}

class MainActivity : AppCompatActivity() {

    private lateinit var sparseArray: SparseArray<String>

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sparseArray = SparseArray(3)
        sparseArray.put(1, "first")
        sparseArray.put(2, "second")
        sparseArray.put(3, "third")

        binding.btnTestPayload.setOnClickListener {
            PayloadTestActivity.launch(this)
        }

    }

    fun click(view: View) {
        when (view.id) {
            R.id.test_drag_sort -> {
                DragSortActivity.launch(this)
            }
            R.id.testPathLayoutManager -> {
                PathLayoutManagerActivity.launch(this)
            }
            R.id.btn_test_rv_nest_rv -> RvNestRvActivity.launch(this)
            R.id.btn_test_recycler_animation -> TestRecyclerAnimationActivity.launch(this)
            R.id.btn_test_recycler_scroll_relate -> RecyclerScrollRangeRelateActivity.launch(this)

            R.id.btnDiffUtil -> DiffUtilActivity.launch(this)
            R.id.btnDiffUtilNew -> MyDiffUtilActivity.launch(this)

            R.id.btn_test_item_view_space -> ItemViewSpaceActivity.launch(this)
            R.id.btn_test_concat_adapter -> ConcatAdapterActivity.launch(this)


            R.id.btn_test_computing_a_layout_or_scrolling ->
                RvIsComputingALayoutOrScrollingActivity.launch(
                    this
                )

            R.id.btn_scroll_to_position -> ScrollToPositionActivity.launch(this)

            R.id.btn_test_recycler_theory -> RecyclerTheoryActivity.launch(this)
            R.id.btn_test_flow_layout -> FlowLayoutActivity.launch(this)
            R.id.btn_test_staggered_layout -> StaggeredGridLayoutManagerActivity.launch(this)

            R.id.btn_center_item_decoration -> GridCenterDividerTestActivity.launch(this)
            R.id.btn_base -> BaseRecyclerViewAdapterActivity.launch(this)
            R.id.btn_slide_delete -> SlideDeleteActivity.launch(this)
            R.id.btn_test_edittext -> TestEditTextActivity.launch(this)

            R.id.btn_scroll_to_center -> ScrollToCenterActivity.launch(this)

            R.id.btn_float_item -> FloatItemActivity.launch(this)
            R.id.btn_dividing_line -> DividingLineActivity.launch(this)
            R.id.btn_time_line -> RecyclerViewTimeLineActivity.launch(this)
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
        LayoutManagerActivity.launch(this)
    }

    fun testLoopGallery(view: View) {
        LoopGalleryActivity.launch(this)
    }

}
