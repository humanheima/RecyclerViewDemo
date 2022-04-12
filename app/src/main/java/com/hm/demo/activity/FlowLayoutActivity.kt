package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.model.TestBean
import com.mcxtzhang.layoutmanager.flow.HorizontalFlowLayoutManager
import java.util.*

class FlowLayoutActivity : AppCompatActivity() {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, FlowLayoutActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var rvUseFlowLayout: RecyclerView
    private lateinit var btnModifyData: Button

    private lateinit var mAdapter: InfiniteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flow_layout)
        rvUseFlowLayout = findViewById(R.id.rvUseFlowLayout)
        rvUseFlowLayout.layoutManager = HorizontalFlowLayoutManager(RecyclerView.HORIZONTAL, 3)
        mAdapter = InfiniteAdapter(this, getTestData())
        rvUseFlowLayout.adapter = mAdapter


        btnModifyData = findViewById(R.id.btnModifyData)

        btnModifyData.setOnClickListener {
            if (mAdapter != null) {
                val oldDataList = mAdapter.dataList
                val longDataList = getTestData()
                val shortDataList = getShortTestData()

                if (oldDataList.size == longDataList.size) {
                    mAdapter.dataList = shortDataList
                } else {
                    mAdapter.dataList = longDataList
                }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    fun getTestData(): List<TestBean> {
        val mDatas: MutableList<TestBean> = ArrayList()
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("杜兰特2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("扬尼斯阿德托昆博3", "艰难", R.drawable.pic_3))
        mDatas.add(TestBean("詹姆斯4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("哈登5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("欧文6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("格里芬7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("乔治8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("莱昂纳德9", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("曼恩10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("布克11", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("保罗12", "最后一个", R.drawable.pic_5))

        return mDatas
    }

    fun getShortTestData(): List<TestBean> {
        val mDatas: MutableList<TestBean> = ArrayList()
        mDatas.add(TestBean("维斯布鲁克", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("安东尼.戴维斯", "艰难", R.drawable.pic_3))
        mDatas.add(TestBean("易建联", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("姚明", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("詹姆斯", "测试", R.drawable.pic_5))
        return mDatas
    }

    class InfiniteAdapter(context: Context, val data: List<TestBean>?) : BaseAdapter<TestBean>(context, data) {
        private val colorArray = IntArray(4)
        override fun getItemCount(): Int {
            //return Int.MAX_VALUE
            return (dataList.size * 10)
            //return dataList.size
        }

        override fun getItemViewType(position: Int): Int {
            var position = position
            if (headView != null) {
                position = position - 1
            }
            val realSize = dataList.size
            val index = position % realSize
            return getHolderType(position, dataList[index])
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var position = position
            if (headView != null) {
                position = position - 1
            }
            val realSize = dataList.size
            val index = position % realSize
            if (index < realSize && holder !is SpecialViewHolder) {
                bindViewHolder(holder as BaseViewHolder, dataList[index], index)
            }
        }

        override fun bindViewHolder(holder: BaseViewHolder, t: TestBean, position: Int) {
            holder.setTextViewText(R.id.tv_name, t.name)
            val colorIndex = position % colorArray.size
            holder.setViewBg(R.id.tv_name, colorArray[colorIndex])
            holder.setOnItemClickListener(R.id.tv_name) { view, position ->
                val realSize = dataList.size
                val index = position % realSize
                val name = dataList[index]!!.desc
                Toast.makeText(context, name, Toast.LENGTH_SHORT).show()
            }
        }

        override fun getHolderType(position: Int, testBean: TestBean): Int {
            return R.layout.item_diff_staggered_layout
        }

        init {
            colorArray[0] = context.resources.getColor(R.color.color_FFF4F4)
            colorArray[1] = context.resources.getColor(R.color.color_FFF9E7)
            colorArray[2] = context.resources.getColor(R.color.color_EEFCF4)
            colorArray[3] = context.resources.getColor(R.color.color_EDF8FC)
        }
    }
}