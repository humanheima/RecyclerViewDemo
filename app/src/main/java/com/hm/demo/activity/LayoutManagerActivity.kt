package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.hm.demo.adapter.TestLayoutManagerAdapter
import com.hm.demo.databinding.ActivityLayoutBinding
import com.hm.demo.layoutmanager.CopyStaggeredGridLayoutManager
import com.hm.demo.model.CheckBoxModel
import kotlin.random.Random

/**
 * Created by dumingwei on 2020/6/9
 *
 * Desc: 测试RecyclerView的各种 layout manager
 */
class LayoutManagerActivity : BaseRecyclerViewAdapterActivity() {

    private var adapter: TestLayoutManagerAdapter? = null

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, LayoutManagerActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val dataList = arrayListOf<String>().apply {
        add("Hello StaggeredGridLayoutManager")
        add(
            "Hello Gird雄州雾列，俊采星驰。台隍枕夷夏之交，宾主尽东南之美。\" +\n" +
                    "                        \"都督阎公之雅望，棨戟遥临；宇文新州之懿范，襜帷暂驻。\" +\n" +
                    "                        \"十旬休假，胜友如云；千里逢迎，高朋满座。\" +\n" +
                    "                        \"腾蛟起凤，孟学士之词宗；紫电青霜，王将军之武库。\" +\n" +
                    "                        \"家君作宰，路出名区；童子何知，躬逢胜饯"
        )
        add("　披绣闼，俯雕甍，山原旷其盈视，川泽纡其骇瞩。")
        add("闾阎扑地，钟鸣鼎食之家；舸舰弥津，青雀黄龙之舳。云销雨霁，彩彻区明。落霞与孤鹜齐飞，秋水共长天一色。")
        add("渔舟唱晚，响穷彭蠡之滨，雁阵惊寒，声断衡阳之浦。遥襟甫畅，逸兴遄飞。")
        add("爽籁发而清风生，纤歌凝而白云遏。睢园绿竹，气凌彭泽之樽；邺水朱华，光照临川之笔。四美具，二难并。穷睇眄于中天，极娱游于暇日。天高地迥，觉宇宙之无穷；兴尽悲来，识盈虚之有数。望长安于日下，目吴会于云间。地势极而南溟深，天柱高而北辰远。关山难越，谁悲失路之人？萍水相逢，尽是他乡之客。怀帝阍而不见，奉宣室以何年？")
        add("LayoutManagerActivity")
        add(
            "时维九月，序属三秋。潦水尽而寒潭清，烟光凝而暮山紫。俨骖騑于上路，访风景于崇阿。临帝子之长洲，" +
                    "得天人之旧馆。层峦耸翠，上出重霄；飞阁流丹，下临无地" +
                    "。鹤汀凫渚，穷岛屿之萦回；桂殿兰宫，即冈峦之体势。（天人 一作：仙人；层峦 一作：层台；即冈 一作：" +
                    "列冈；飞阁流丹 一作：飞阁翔丹）"
        )
        add("　披绣闼，俯雕甍，山原旷其盈视，川泽纡其骇瞩。")
        add("闾阎扑地，钟鸣鼎食之家；舸舰弥津，青雀黄龙之舳。云销雨霁，彩彻区明。落霞与孤鹜齐飞，秋水共长天一色。")
    }

    private lateinit var binding: ActivityLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val listOf = arrayListOf<CheckBoxModel>()
        for (i in 0..100) {
            val index = Random.nextInt(0, dataList.size)
            val text = "${dataList[index]} $i"
            listOf.add(CheckBoxModel(text, false))
        }

        adapter = TestLayoutManagerAdapter(this, listOf)

        //binding.rvLayout.layoutManager = GridLayoutManager(this, 2)
        binding.rvLayout.layoutManager =
            CopyStaggeredGridLayoutManager(2, StaggeredGridLayoutManager.HORIZONTAL)
        binding.rvLayout.adapter = adapter

        binding.btnScrollWithOffset.setOnClickListener {

        }

    }

}
