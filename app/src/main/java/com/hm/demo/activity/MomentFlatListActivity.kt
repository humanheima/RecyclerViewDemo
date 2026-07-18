package com.hm.demo.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R

/**
 * Created by dumingwei on 2026/7/18
 * Desc: 演示朋友圈列表平铺方案，使用单 RecyclerView + 多 ItemType + DiffUtil
 */
class MomentFlatListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MomentFlatAdapter
    private var moments: List<Moment> = emptyList()
    private var nextCommentId = 1000L

    companion object {
        fun launch(context: Context) {
            context.startActivity(Intent(context, MomentFlatListActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moment_flat_list)
        recyclerView = findViewById(R.id.rv_moment_list)
        adapter = MomentFlatAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        moments = buildSeedData()
        adapter.submitList(buildFlatList(moments))

        findViewById<View>(R.id.btn_add_comment).setOnClickListener {
            addCommentForFirstMoment()
        }
    }

    private fun addCommentForFirstMoment() {
        val firstMoment = moments.firstOrNull() ?: return
        val appendedComment = Comment(
            id = nextCommentId++,
            momentId = firstMoment.id,
            userName = "新用户",
            content = "这是新增评论，使用 DiffUtil 无闪烁刷新"
        )
        val updatedMoments = moments.map { moment ->
            if (moment.id == firstMoment.id) {
                moment.copy(commentList = moment.commentList + appendedComment)
            } else {
                moment
            }
        }
        moments = updatedMoments
        adapter.submitList(buildFlatList(updatedMoments))
    }

    private fun buildFlatList(momentList: List<Moment>): List<ListItem> {
        return buildList {
            momentList.forEach { moment ->
                add(ListItem.MomentItem(moment))
                moment.commentList.forEach { comment ->
                    add(ListItem.CommentItem(comment))
                }
            }
        }
    }

    private fun buildSeedData(): List<Moment> {
        return listOf(
            Moment(
                id = 1L,
                userName = "张三",
                content = "今天把 RecyclerView 多级列表改成平铺了，滑动明显更稳。",
                pics = listOf("pic_1", "pic_2"),
                commentList = listOf(
                    Comment(11L, 1L, "李四", "这个方案很实用"),
                    Comment(12L, 1L, "王五", "DiffUtil 配合起来非常丝滑")
                )
            ),
            Moment(
                id = 2L,
                userName = "小明",
                content = "单 RecyclerView + 多 Type，维护成本比嵌套 RV 低很多。",
                pics = emptyList(),
                commentList = listOf(
                    Comment(21L, 2L, "小红", "后续做分页也方便")
                )
            ),
            Moment(
                id = 3L,
                userName = "产品同学",
                content = "点赞、评论、删除都在同一条链路里处理，联调快了不少。",
                pics = emptyList(),
                commentList = emptyList()
            )
        )
    }
}

data class Moment(
    val id: Long,
    val userName: String,
    val content: String,
    val pics: List<String>,
    val commentList: List<Comment>
)

data class Comment(
    val id: Long,
    val momentId: Long,
    val userName: String,
    val content: String
)

sealed class ListItem {
    data class MomentItem(val moment: Moment) : ListItem()
    data class CommentItem(val comment: Comment) : ListItem()
}

private class MomentFlatAdapter :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(MomentItemDiffCallback()) {

    companion object {
        private const val TYPE_MOMENT = 0
        private const val TYPE_COMMENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.MomentItem -> TYPE_MOMENT
            is ListItem.CommentItem -> TYPE_COMMENT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_MOMENT -> MomentViewHolder(
                inflater.inflate(R.layout.item_moment_post, parent, false)
            )

            else -> CommentViewHolder(
                inflater.inflate(R.layout.item_moment_comment, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.MomentItem -> (holder as MomentViewHolder).bind(item.moment)
            is ListItem.CommentItem -> (holder as CommentViewHolder).bind(item.comment)
        }
    }

    private class MomentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tv_moment_user_name)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_moment_content)
        private val tvMeta: TextView = itemView.findViewById(R.id.tv_moment_meta)

        fun bind(moment: Moment) {
            tvUserName.text = moment.userName
            tvContent.text = moment.content
            tvMeta.text = "图片 ${moment.pics.size} 张 · 评论 ${moment.commentList.size} 条"
        }
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvComment: TextView = itemView.findViewById(R.id.tv_moment_comment)

        fun bind(comment: Comment) {
            tvComment.text = "${comment.userName}: ${comment.content}"
        }
    }
}

private class MomentItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return when {
            oldItem is ListItem.MomentItem && newItem is ListItem.MomentItem ->
                oldItem.moment.id == newItem.moment.id

            oldItem is ListItem.CommentItem && newItem is ListItem.CommentItem ->
                oldItem.comment.id == newItem.comment.id

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}
