package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.Util.Images
import com.hm.demo.model.TestBean
import org.cchao.carousel.CarouselView

/**
 * Created by p_dmweidu on 2023/6/24
 * Desc: 测试 ContactAdapter
 * 参考链接：https://juejin.cn/post/7064856244125138952
 * [ConcatAdapter和GridLayoutManager同时使用的问题](https://www.jianshu.com/p/9c1f5105e32a)
 */

class ConcatAdapterActivity : AppCompatActivity() {


    private var concatAdapter: ConcatAdapter? = null

    companion object {

        private const val TAG = "ConcatAdapterActivity"

        fun launch(context: Context) {
            val starter = Intent(context, ConcatAdapterActivity::class.java)
            context.startActivity(starter)
        }
    }

    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_concat_adapter)
        recyclerView = findViewById(R.id.rv_contact_adapter)


        val headAdapter = HeadAdapter(this)
        val contentAdapter = ContentAdapter(getTestData())
        val contentAdapter1 = ContentTwoAdapter(getTestData())
        concatAdapter = ConcatAdapter(headAdapter, contentAdapter, contentAdapter1)
        recyclerView?.adapter = concatAdapter

        val gridLayoutManager = GridLayoutManager(this, 2).apply {

            configSingleViewSpan { position ->
                val isHeadAdapter = concatAdapter?.getAdapterByItemPosition(position) is HeadAdapter
                Log.i(TAG, "onCreate: is HeadAdapter = $isHeadAdapter")
                isHeadAdapter
            }
        }

        recyclerView?.layoutManager = gridLayoutManager
    }

    class HeadAdapter(val context: Context) : RecyclerView.Adapter<HeadAdapter.VH>() {

        private val TAG = "ConcatAdapterActivity"

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context).inflate(R.layout.head_layout, parent, false)
            )

        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            with(holder) {
                // warning：2023/6/24: Banner定义的有问题，后续改进
                val bannerTag = banner?.tag
                if (bannerTag == null) {
                    Log.i(TAG, "onBindViewHolder: bannerTag=null")
                    bindBanner(itemView, banner)
                } else {
                    Log.i(TAG, "onBindViewHolder: bannerTag=$bannerTag")
                }
            }
        }

        override fun getItemCount(): Int {
            return 1
        }

        private fun bindBanner(view: View, banner: CarouselView? = null) {
            banner?.setTag("banner has init")
            val multiTitles: MutableList<String>?
            val multiImgs: MutableList<String>?
            multiTitles = arrayListOf()
            multiImgs = arrayListOf()
            multiTitles.add("当春乃发生")
            multiTitles.add("随风潜入夜")
            multiTitles.add("润物细无声")
            multiImgs.add(Images.imageThumbUrls[0])
            multiImgs.add(Images.imageThumbUrls[1])
            multiImgs.add(Images.imageThumbUrls[2])
//            banner?.setOnBannerClickListener { position ->
//                Toast.makeText(
//                    context, "position=$position", Toast.LENGTH_SHORT
//                ).show()
//            }
//            banner?.setImages(multiImgs)?.setImageLoader(GlideImageLoader())?.setTitles(multiTitles)
//                ?.setAbortAnimation(false)?.isAutoPlay(true)?.start()
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            var banner: CarouselView? = null

            init {
                banner = view.findViewById(R.id.simple_banner)
            }
        }
    }

    class ContentAdapter(val datas: List<TestBean>) : RecyclerView.Adapter<ContentAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_concat_first, parent, false)
            )

        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            with(holder) {
                tv1?.text = datas[position].name
                tv2?.text = datas[position].desc
                iv?.setImageResource(datas[position].picture)
            }
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun getItemViewType(position: Int): Int {
            return R.layout.item_concat_first
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            var tv1: TextView? = null
            var tv2: TextView? = null
            var iv: ImageView? = null

            init {
                tv1 = view.findViewById(R.id.tv1)
                tv2 = view.findViewById(R.id.tv2)
                iv = view.findViewById(R.id.iv)
            }
        }
    }


    class ContentTwoAdapter(val datas: List<TestBean>) :
        RecyclerView.Adapter<ContentTwoAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context).inflate(R.layout.item_concat_two, parent, false)
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            with(holder) {
                tv1?.text = datas[position].name
                tv2?.text = datas[position].desc
                iv?.setImageResource(datas[position].picture)
            }
        }

        override fun getItemCount(): Int {
            return datas.size
        }

        override fun getItemViewType(position: Int): Int {
            return R.layout.item_concat_two
        }

        class VH(view: View) : RecyclerView.ViewHolder(view) {
            var tv1: TextView? = null
            var tv2: TextView? = null
            var iv: ImageView? = null

            init {
                tv1 = view.findViewById(R.id.tv1)
                tv2 = view.findViewById(R.id.tv2)
                iv = view.findViewById(R.id.iv)
            }
        }
    }

    private fun getTestData(): List<TestBean> {
        val mDatas: MutableList<TestBean> = ArrayList()
        mDatas.add(TestBean("dumingwei1", "Android", R.drawable.pic))
        mDatas.add(TestBean("dumingwei2", "Java", R.drawable.pic_2))
        mDatas.add(TestBean("dumingwei3", "beiguo", R.drawable.pic_3))
        mDatas.add(TestBean("dumingwei4", "产品", R.drawable.pic_4))
        mDatas.add(TestBean("dumingwei10", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei5", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei6", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei7", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei8", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "测试", R.drawable.pic_5))
        mDatas.add(TestBean("dumingwei20", "最后一个", R.drawable.pic_5))
        return mDatas
    }

}

fun GridLayoutManager.configSingleViewSpan(range: (position: Int) -> Boolean) {

    val TAG = "configSingleViewSpan"
    val oldSpanSizeLookup = spanSizeLookup
    val oldSpanCount = spanCount

    spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            if (range(position)) {
                Log.i(TAG, "getSpanSize: spanCount =$oldSpanCount")
                return oldSpanCount
            } else {
                val spanSize = oldSpanSizeLookup.getSpanSize(position)
                Log.i(TAG, "getSpanSize: spanSize = $spanSize")
                return spanSize
            }
        }
    }
}

/**
 * @param position 在 ConcatAdapter 中的位置
 */
fun ConcatAdapter.getAdapterByItemPosition(position: Int): RecyclerView.Adapter<out RecyclerView.ViewHolder>? {
    var pos = position
    val adapters = adapters
    for (adapter in adapters) {
        when {
            //大于当前 adapter 的 item 数量，则减去当前 adapter 的 item 数量，然后在下一个适配器中查找
            pos >= adapter.itemCount -> {
                pos -= adapter.itemCount
            }

            pos < 0 -> return null
            else -> return adapter
        }
    }
    return null
}
