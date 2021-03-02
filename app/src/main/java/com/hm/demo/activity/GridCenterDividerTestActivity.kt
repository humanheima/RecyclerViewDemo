package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.base.BaseAdapter
import com.hm.demo.base.BaseViewHolder
import com.hm.demo.databinding.ActivityGridCenterDividerTestBinding
import com.hm.demo.widget.GridItemDecoration

/**
 * Created by dumingwei on 2021/3/2
 *
 * Desc: 测试使用GridLayoutManager的时候，左右对称的分割线
 *
 */
class GridCenterDividerTestActivity : AppCompatActivity() {


    private lateinit var binding: ActivityGridCenterDividerTestBinding
    private lateinit var rv: RecyclerView

    private var arrayList: ArrayList<String> = arrayListOf()

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, GridCenterDividerTestActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGridCenterDividerTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        rv = binding.rv
        rv.layoutManager = GridLayoutManager(this, 3)

        for (i in 0..20) {
            arrayList.add("string$i")
        }
        rv.adapter = object : BaseAdapter<String>(this, arrayList) {

            override fun bindViewHolder(holder: BaseViewHolder, t: String?, position: Int) {
                holder.setTextViewText(R.id.tvName, t)
            }

            override fun getHolderType(position: Int, t: String?): Int {
                return R.layout.item_horizontal_load_morel
            }
        }

        //当RecyclerView的宽为wrap_content的时候才能正常起作用，宽为match_parent的时候，不一定好使。
        rv.addItemDecoration(GridItemDecoration(this, 20, 20))


    }
}