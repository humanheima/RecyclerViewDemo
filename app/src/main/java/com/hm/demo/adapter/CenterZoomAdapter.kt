package com.hm.demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.RecommendCharacter

/**
 * 支持中间缩放效果和循环滑动的RecyclerView适配器
 */
class CenterZoomAdapter(
    private val characters: List<RecommendCharacter>,
    private val enableLoop: Boolean = true,
    private val onItemClick: ((RecommendCharacter, Int) -> Unit)? = null
) : RecyclerView.Adapter<CenterZoomAdapter.ViewHolder>() {

    // 用于实现循环效果的虚拟item数量倍数
    private val loopMultiplier = 1000

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_center_zoom_character, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val realPosition = getRealPosition(position)
        holder.bind(characters[realPosition], realPosition)
    }

    override fun getItemCount(): Int {
        return if (enableLoop && characters.isNotEmpty()) {
            loopMultiplier * characters.size
        } else {
            characters.size
        }
    }

    /**
     * 获取真实的position
     */
    private fun getRealPosition(position: Int): Int {
        return if (enableLoop && characters.isNotEmpty()) {
            position % characters.size
        } else position
    }

    /**
     * 获取真实数据的数量
     */
    fun getRealItemCount(): Int = characters.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvIdentity: TextView = itemView.findViewById(R.id.tv_identity)
        private val tvTag: TextView = itemView.findViewById(R.id.tv_tag)

        fun bind(character: RecommendCharacter, realPosition: Int) {
            tvName.text = character.name ?: "未知角色"
            tvIdentity.text = character.identity ?: ""
            
            // 显示第一个标签
            val firstTag = character.tagList?.firstOrNull()?.tagName
            tvTag.text = firstTag ?: ""
            tvTag.visibility = if (firstTag.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            // 这里可以使用图片加载库如Glide加载头像
            // Glide.with(itemView.context).load(character.avatar).into(ivAvatar)
            
            // 设置点击事件
            itemView.setOnClickListener {
                onItemClick?.invoke(character, realPosition)
            }
        }
    }
}