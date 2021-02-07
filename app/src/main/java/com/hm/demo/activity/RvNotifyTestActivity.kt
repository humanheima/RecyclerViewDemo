package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import com.hm.demo.Util.ScreenUtil
import com.hm.demo.adapter.InterestCardAdapter
import com.hm.demo.databinding.ActivityRvNotifyTestBinding
import com.hm.demo.model.NewInterestCardModel

/**
 * Created by dumingwei on 2020/12/9
 *
 * Desc: 测试RecyclerView的局部刷新操作
 */
class RvNotifyTestActivity : AppCompatActivity() {

    private val interestBgColorList: ArrayList<String> = ArrayList(12)

    private val flatInterestList: ArrayList<NewInterestCardModel> = arrayListOf()
    private val parentInterestList: ArrayList<NewInterestCardModel> = arrayListOf()

    private lateinit var mInterestAdapter: InterestCardAdapter

    private var itemWidth = 0

    private lateinit var binding: ActivityRvNotifyTestBinding

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, RvNotifyTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val TAG: String = "RvNotifyTestActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvNotifyTestBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        itemWidth = (ScreenUtil.getScreenWidth(this) - ScreenUtil.dpToPx(this, 30)) / 3

        initInterestColorList()
        initData()
        initRvCategory()
    }

    private fun initData() {
        for (i in 0..11) {
            val model = NewInterestCardModel()
            model.bgColor = interestBgColorList[i]
            model.categoryName = "第${i}个分类"
            model.code = "code $i"
            val arrayList = arrayListOf<NewInterestCardModel>()
            for (j in 0..3) {
                val childModel = NewInterestCardModel()
                childModel.categoryName = "子第${i}个分类"
                childModel.code = "code $i child $j"
                arrayList.add(childModel)
            }
            model.subCategories = arrayList
            parentInterestList.add(model)
        }
        flatInterestList.addAll(parentInterestList)
    }


    private fun initInterestColorList() {
        interestBgColorList.add("#FF7894B4")
        interestBgColorList.add("#FF70BE9D")
        interestBgColorList.add("#FFBE7878")

        interestBgColorList.add("#FF8A96B5")
        interestBgColorList.add("#FFC5987D")
        interestBgColorList.add("#FFB69074")

        interestBgColorList.add("#FF9B8D68")
        interestBgColorList.add("#FF6885A7")
        interestBgColorList.add("#FF9070B0")

        interestBgColorList.add("#FF9B8D68")
        interestBgColorList.add("#FF6885A7")
        interestBgColorList.add("#FF9070B0")

    }

    private fun initRvCategory() {
        mInterestAdapter = InterestCardAdapter(this, itemWidth, flatInterestList)
        mInterestAdapter.onSelectedInterface = object : InterestCardAdapter.OnSelectedInterface {

            override fun onSelected(position: Int, model: NewInterestCardModel) {
                Log.i(TAG, "onSelected: position = $position")
                val subCategories: List<NewInterestCardModel>? = model.subCategories
                mInterestAdapter.notifyItemChanged(position)
                if (!subCategories.isNullOrEmpty()) {
                    //添加或者移除的开始位置
                    val startInsertPosition = position + 1
                    val size = subCategories.size
                    if (model.selected) { //选中，如果有子级兴趣，则添加到适配器中
                        var childNotInFlatList = true
                        for (subCategory in subCategories) {
                            if (flatInterestList.contains(subCategory)) {
                                //至少有一个子级兴趣被选中
                                childNotInFlatList = false
                                break
                            }
                        }
                        //已经添加过了，就不再添加了
                        if (childNotInFlatList) {
                            for (i in 0 until size) {
                                flatInterestList.add(startInsertPosition + i, subCategories[i])
                            }
                            mInterestAdapter.notifyItemRangeInserted(startInsertPosition, size)
                            //mInterestAdapter.notifyItemRangeChanged(startInsertPosition + size, flatInterestList.size - startInsertPosition - size)
                            mInterestAdapter.notifyItemRangeChanged(startInsertPosition, flatInterestList.size - startInsertPosition)
                        }
                    } else { //取消选中，如果有子级兴趣，且没有一个子级兴趣被选中，则全部移除。
                        var noChildSelected = true
                        for (subCategory in subCategories) {
                            if (subCategory.selected) {
                                //至少有一个子级兴趣被选中
                                noChildSelected = false
                                break
                            }
                        }
                        if (noChildSelected) {
                            flatInterestList.removeAll(subCategories)
                            mInterestAdapter.notifyItemRangeRemoved(startInsertPosition, size)
                            //mInterestAdapter.notifyItemRangeChanged(startInsertPosition + size, flatInterestList.size - startInsertPosition - size)
                            mInterestAdapter.notifyItemRangeChanged(startInsertPosition, flatInterestList.size - startInsertPosition)
                        }
                    }
                }
            }
        }
        binding.rv.adapter = mInterestAdapter
        binding.rv.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        val defaultItemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
        defaultItemAnimator.addDuration = 500
        defaultItemAnimator.moveDuration = 500
        defaultItemAnimator.removeDuration = 500
        defaultItemAnimator.changeDuration = 500
        binding.rv.itemAnimator = defaultItemAnimator
    }

}