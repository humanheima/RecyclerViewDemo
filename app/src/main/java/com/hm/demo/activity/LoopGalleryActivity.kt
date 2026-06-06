package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.hm.demo.R
import com.hm.demo.adapter.TestLayoutManagerAdapter
import com.hm.demo.databinding.ActivityLoopGalleryBinding
import com.hm.demo.model.CheckBoxModel
import com.mcxtzhang.layoutmanager.gallery.BaseLoopGallery
import com.hm.demo.model.RecommendCharacterList

/**
 * Created by dumingwei on 2020/6/9
 *
 * Desc: 测试RecyclerView的各种 layout manager
 */
class LoopGalleryActivity : BaseRecyclerViewAdapterActivity() {

    private var adapter: TestLayoutManagerAdapter? = null

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, LoopGalleryActivity::class.java)
            context.startActivity(intent)
        }
    }


    private lateinit var binding: ActivityLoopGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoopGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        testLinearLayoutManager()
    }

    private fun testLinearLayoutManager() {
        val arrayList = arrayListOf<CheckBoxModel>()
        for (i in 0 until 10) {
            //从 RecommendCharacterList.createSampleData() 循环取值，androidAvatarBack 赋值给 imageUrl
            val recommendCharacterList = RecommendCharacterList.createSampleData()
            val element = CheckBoxModel("Hello$i", false)
            element.drawableResId = R.mipmap.ic_launcher
            element.imageUrl = recommendCharacterList.characters[i % recommendCharacterList.characters.size].androidAvatarBack ?: ""
            arrayList.add(element)
        }
        binding.rvLayout.setDatasAndLayoutId(
            arrayList,
            R.layout.item_gallery_image, object : BaseLoopGallery.BindDataListener<CheckBoxModel> {
                override fun onBindData(
                    holder: com.mcxtzhang.commonadapter.rv.ViewHolder?,
                    data: CheckBoxModel
                ) {
                    Glide.with(this@LoopGalleryActivity).load(data.imageUrl).into(holder!!.getView<ImageView>(R.id.iv_image))
                    //holder?.setImageResource(R.id.iv_image, data.drawableResId)
                    holder?.setText(R.id.tv_description, data.description)
                }
            }
        )
    }

}
