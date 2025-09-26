package com.hm.demo.adapter

import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hm.demo.R
import com.hm.demo.model.CheckBoxModel


/**
 * Created by dumingwei on 2017/10/10.
 */
class TestLayoutManagerAdapter(
    private val context: Context,
    private val data: MutableList<CheckBoxModel>?
) : RecyclerView.Adapter<TestLayoutManagerAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "TestAnimatorAdapterAdap"
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
        holder.checkBox.isSelected = model.isChecked
        holder.textDescription.text = model.description
        Log.i(TAG, "onBindViewHolder: position = $position holder = $holder model = $model")
        holder.tvInnerText.text = "豫章故郡，洪都新府。星分翼轸，" +
                "地接衡庐。襟三江而带五湖，控蛮荆而引瓯越。" +
                "物华天宝，龙光射牛斗之墟；人杰地灵，" +
                "徐孺下陈蕃之榻。雄州雾列，俊采星驰。台隍枕夷夏之交，" +
                "宾主尽东南之美。都督阎公之雅望，棨戟遥临；宇文新州之懿范" +
                "，襜帷暂驻。十旬休暇，胜友如云；千里逢迎，高朋满座。腾蛟起凤，" +
                "孟学士之词宗；紫电青霜，王将军之武库。家君作宰，路出名区；童子何知，躬逢胜饯。"

        //holder.tvInnerText.movementMethod = ScrollingMovementMethod();

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
        var tvInnerText: TextView = itemView.findViewById(R.id.tv_scroll_text)

        init {
            //nestScrollView.isNestedScrollingEnabled = true
        }

    }

}
