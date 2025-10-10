package com.hm.demo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.CheckBoxModel
import com.hm.demo.widget.ExpandableTextView


/**
 * Created by dumingwei on 2017/10/10.
 */
class TestLayoutManagerAdapter(
    private val context: Context,
    private val data: MutableList<CheckBoxModel>?
) : RecyclerView.Adapter<TestLayoutManagerAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "TestAnimatorAdapterAdap"

        // 定义payload标识，用于区分"只更新选中状态"的刷新
        const val PAYLOAD_CHECKED: String = "payload_checked"
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.item_test_layout_manager, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data!![position]

        // 2. 关键：根据数据模型设置选中状态（避免复用导致的状态混乱）
        // 先移除监听器再设置状态，防止触发不必要的回调

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.isChecked = model.isChecked

        holder.textDescription.text = model.description
        Log.i(TAG, "onBindViewHolder: position = $position holder = $holder model = $model")
        holder.tvInnerText.text = "豫章故郡，洪都新府。星分翼轸，" +
                "地接衡庐。襟三江而带五湖，控蛮荆而引瓯越。" +
                "物华天宝，龙光射牛斗之墟；人杰地灵，" +
                "徐孺下陈蕃之榻。雄州雾列，俊采星驰。台隍枕夷夏之交，" +
                "宾主尽东南之美。都督阎公之雅望，棨戟遥临；宇文新州之懿范" +
                "，襜帷暂驻。十旬休暇，胜友如云；千里逢迎，高朋满座。腾蛟起凤，" +
                "孟学士之词宗；紫电青霜，王将军之武库。家君作宰，路出名区；童子何知，躬逢胜饯。"

        setCheckStatus(model, holder)

        holder.checkBox.setOnClickListener {
            model.isChecked = model.isChecked.not()
            holder.checkBox.post {
                notifyItemChanged(position, PAYLOAD_CHECKED)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            // 无payload：正常绑定整个Item（首次加载或全量刷新）
            Log.d(TAG, "onBindViewHolder: 调用两个参数的 onBindViewHolder position = $position")
            onBindViewHolder(holder, position)
        } else {
            Log.d(TAG, "onBindViewHolder: 使用payload 更新 position = $position")
            // 有payload：只处理选中状态更新
            for (payload in payloads) {
                if (PAYLOAD_CHECKED == payload) {
                    // 从数据模型获取最新状态，更新CheckBox
                    val model = data?.get(position)
                    if (model != null) {
                        holder.checkBox.isChecked = model.isChecked
                        setCheckStatus(model, holder)
                    }
                }
            }
        }
    }

    private fun setCheckStatus(
        model: CheckBoxModel,
        holder: ViewHolder
    ) {
        if (model.isChecked) {
            holder.tvInnerText.setExpanded()
        } else {
            holder.tvInnerText.setCollapsed()
        }
    }


    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    fun getDataList(): MutableList<CheckBoxModel>? {
        return data
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var checkBox: CheckBox = itemView.findViewById(R.id.check_box)
        var textDescription: TextView = itemView.findViewById(R.id.text_description)
        var tvInnerText: ExpandableTextView = itemView.findViewById(R.id.tv_scroll_text)
    }

}
