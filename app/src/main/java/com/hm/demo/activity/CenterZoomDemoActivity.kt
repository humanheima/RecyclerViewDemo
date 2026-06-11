package com.hm.demo.activity

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.adapter.CenterZoomAdapter
import com.hm.demo.decoration.HorizontalSpaceItemDecoration
import com.hm.demo.layoutmanager.CenterZoomLayoutManager
import com.hm.demo.model.RecommendCharacter
import com.hm.demo.model.RecommendCharacterList

/**
 * 中间缩放RecyclerView演示Activity
 */
class CenterZoomDemoActivity : AppCompatActivity() {

    private lateinit var rvCharacters: RecyclerView
    private lateinit var tvSelectedInfo: TextView
    private lateinit var adapter: CenterZoomAdapter
    private var characterList = kotlin.collections.emptyList<RecommendCharacter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_center_zoom_demo)

        initViews()
        setupRecyclerView()
        loadData()
    }

    private fun initViews() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvCharacters = findViewById(R.id.rv_characters)
        tvSelectedInfo = findViewById(R.id.tv_selected_info)
    }

    private fun setupRecyclerView() {
        // 创建自定义LayoutManager（启用循环效果）
        val layoutManager = CenterZoomLayoutManager(
            context = this,
            recyclerView = rvCharacters,
            maxScale = 1.3f,
            minScale = 1.0f,
            enableLoop = true
        )
        rvCharacters.layoutManager = layoutManager

        // 添加间距装饰器
        val spaceDecoration = HorizontalSpaceItemDecoration(32) // 使用固定数值代替资源
        rvCharacters.addItemDecoration(spaceDecoration)

        // 添加滚动监听器来更新选中信息
        rvCharacters.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateSelectedInfo()
                }
            }
        })

        // 确保RecyclerView支持居中对齐
        rvCharacters.clipToPadding = false
    }

    private fun loadData() {
        // 加载示例数据
        val sampleData = RecommendCharacterList.createSampleData()
        characterList = sampleData.characters

        // 重新创建适配器
        val layoutManager = rvCharacters.layoutManager as? CenterZoomLayoutManager
        adapter = CenterZoomAdapter(
            characters = characterList,
            enableLoop = true
        ) { character, realPosition ->
            onCharacterClick(character, realPosition)
        }
        rvCharacters.adapter = adapter

        // 设置真实item数量给LayoutManager
        layoutManager?.setRealItemCount(characterList.size)

        // 延迟一帧后滚动到中心位置，确保第一个item（凌意）居中显示
        rvCharacters.post {
            if (characterList.isNotEmpty()) {
                // 计算中心位置，让第一个item（realPosition=0）居中
                // 使用循环中间位置 + 0的偏移量
                val centerLoopPosition = (1000 / 2) * characterList.size + 0
                rvCharacters.scrollToPosition(centerLoopPosition)

                // 再次延迟确保滚动完成后更新信息
                rvCharacters.postDelayed({
                    // 强制让第一个item居中
                    val currentLayoutManager =
                        rvCharacters.layoutManager as? CenterZoomLayoutManager
                    currentLayoutManager?.let {
                        val smoothScroller = object :
                            androidx.recyclerview.widget.LinearSmoothScroller(this@CenterZoomDemoActivity) {
                            override fun calculateDtToFit(
                                viewStart: Int,
                                viewEnd: Int,
                                boxStart: Int,
                                boxEnd: Int,
                                snapPreference: Int
                            ): Int {
                                return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
                            }
                        }
                        smoothScroller.targetPosition = centerLoopPosition
                        it.startSmoothScroll(smoothScroller)
                    }

                    updateSelectedInfo()
                }, 200)
            }
        }
    }

    private fun onCharacterClick(character: RecommendCharacter, realPosition: Int) {
        Toast.makeText(this, "点击了: ${character.name} (位置: $realPosition)", Toast.LENGTH_SHORT)
            .show()

        // 滚动到点击的item（需要转换为循环位置）
        val layoutManager = rvCharacters.layoutManager as? CenterZoomLayoutManager
        if (layoutManager != null) {
            // 计算当前应该滚动到的循环位置
            val currentCenterPosition = layoutManager.getCurrentCenterPosition()
            val targetLoopPosition =
                calculateTargetLoopPosition(currentCenterPosition, realPosition)
            rvCharacters.smoothScrollToPosition(targetLoopPosition)
        }
    }

    /**
     * 计算目标循环位置，选择距离当前位置最近的循环位置
     */
    private fun calculateTargetLoopPosition(
        currentRealPosition: Int,
        targetRealPosition: Int
    ): Int {
        if (characterList.isEmpty()) return targetRealPosition

        val adapter = rvCharacters.adapter as? CenterZoomAdapter ?: return targetRealPosition
        val realItemCount = adapter.getRealItemCount()
        val loopMultiplier = 1000

        // 计算当前位置对应的循环位置基数
        val currentLoopBase = (loopMultiplier * realItemCount / 2 / realItemCount) * realItemCount

        // 计算三个可能的目标位置
        val option1 = currentLoopBase + targetRealPosition
        val option2 = option1 - realItemCount
        val option3 = option1 + realItemCount

        // 选择距离当前位置最近的选项
        val currentLoopPosition = currentLoopBase + currentRealPosition
        val distances = kotlin.collections.listOf(
            kotlin.math.abs(option1 - currentLoopPosition),
            kotlin.math.abs(option2 - currentLoopPosition),
            kotlin.math.abs(option3 - currentLoopPosition)
        )

        return when (distances.indexOf(distances.minOrNull())) {
            0 -> option1
            1 -> option2
            else -> option3
        }
    }

    private fun updateSelectedInfo() {
        val layoutManager = rvCharacters.layoutManager as? CenterZoomLayoutManager ?: return
        val centerRealPosition = layoutManager.getCurrentCenterPosition()

        if (centerRealPosition >= 0 && centerRealPosition < characterList.size) {
            val centerCharacter = characterList[centerRealPosition]
            tvSelectedInfo.text = buildString {
                append("当前选中: ${centerCharacter.name}\n")
                append("身份: ${centerCharacter.identity}\n")
                append("真实位置: $centerRealPosition\n")
                centerCharacter.tagList?.firstOrNull()?.tagName?.let { tag ->
                    append("标签: $tag")
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
