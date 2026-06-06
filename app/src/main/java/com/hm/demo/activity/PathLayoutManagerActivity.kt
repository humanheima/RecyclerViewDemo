package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.Util.ScreenUtil
import com.hm.demo.adapter.TestPathLayoutManagerAdapter
import com.hm.demo.databinding.ActivityPathLayoutManagerBinding
import com.hm.demo.model.CheckBoxModel
import com.hm.demo.pathlayoutmanager.PathLayoutManager
import kotlin.math.asin

/**
 * Created by p_dmweidu on 2024/11/21
 * Desc:
 */
class PathLayoutManagerActivity : BaseRecyclerViewAdapterActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, PathLayoutManagerActivity::class.java)
            context.startActivity(intent)
        }
    }


    private lateinit var binding: ActivityPathLayoutManagerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPathLayoutManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        testPathLayoutManager()
    }

    /**
     * 调整角度，使其在0 ~ 360之间
     *
     * @param rotation 当前角度
     * @return 调整后的角度
     */
    private fun fixAngle(rotation: Float): Float {
        var rotation = rotation
        val angle = 360f
        if (rotation < 0) {
            rotation += angle
        }
        if (rotation > angle) {
            rotation %= angle
        }
        return rotation
    }


    private var mLayoutManager: PathLayoutManager? = null

    private fun testPathLayoutManager() {
        val path = Path()
        val length: Int = ScreenUtil.dpToPx(this, 205) //两个item中心弧长
        val lengthSwipe = 10f //length对应的夹角，单位是角度
        val radius = (length * 180f / Math.PI / lengthSwipe).toFloat() //根据弧长和弧度求出半径
        val swipeHalf: Float =
            fixAngle((asin((ScreenUtil.getScreenWidth(this) / 2f / radius)) * 180f / Math.PI).toFloat())
        val startAngle = 90 + swipeHalf

        val x: Float = ScreenUtil.getScreenWidth(this) / 2f

        //100 RecyclerView高度一半
        val y: Float = ScreenUtil.dpToPx(this, 100) - radius

        val oval = RectF(x - radius, y - radius, x + radius, y + radius)
        path.addArc(oval, startAngle, -1 * swipeHalf * 2)


        //使用path创建mLayoutManager
        mLayoutManager =
            PathLayoutManager(path, ScreenUtil.dpToPx(this, 105), RecyclerView.HORIZONTAL)
        mLayoutManager?.setItemDirectionFixed(false) // 保持垂直


        //        mLayoutManager.setItemScaleRatio(0.9f, 0, 1f, 0.5f, 0.9f, 1f);
        mLayoutManager?.setScrollMode(PathLayoutManager.SCROLL_MODE_LOOP)
        mLayoutManager?.setAutoSelect(true)
        mLayoutManager?.setItemDirectionFixed(true)

        binding.rvLayout.layoutManager = mLayoutManager

        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 30) {
            arrayList.add(CheckBoxModel("Hello$i", false))
        }
        val dpToPx10 = ScreenUtil.dpToPx(this, 10)
        binding.rvLayout.adapter = TestPathLayoutManagerAdapter(
            this,
            arrayList
        )
    }

}
