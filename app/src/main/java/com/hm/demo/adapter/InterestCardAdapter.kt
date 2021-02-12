package com.hm.demo.adapter

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.hm.demo.R
import com.hm.demo.model.NewInterestCardModel

/**
 * Created by dumingwei on 2020/12/9.
 *
 * Desc:
 */
class InterestCardAdapter(
        val context: Context,
        val mInterestImageViewWith: Int,
        val interestList: ArrayList<NewInterestCardModel>
) : androidx.recyclerview.widget.RecyclerView.Adapter<InterestCardAdapter.VH>() {

    var onSelectedInterface: OnSelectedInterface? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_item_custom_card, parent, false)
        val vh = VH(view)
        //动态改变宽高比
        val layoutParams = vh.rlCustomImage.layoutParams
        layoutParams.width = mInterestImageViewWith
        layoutParams.height = mInterestImageViewWith * 70 / 106
        vh.rlCustomImage.layoutParams = layoutParams
        return vh
    }

    override fun getItemCount(): Int {
        return interestList.size
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val model = interestList[position]
        if (model.bgColor == null) {
            holder.tvName.setTextColor(Color.BLACK)

            holder.ivCustomCardCover.background = (ContextCompat.getDrawable(context, R.drawable.radius_8_stroke_cccccc))
        } else {
            holder.tvName.setTextColor(Color.WHITE)
            holder.ivCustomCardCover.setBackgroundColor(Color.parseColor(model.bgColor))
        }
        holder.tvName.text = model.categoryName

        holder.ivCustomCardTag.isSelected = model.selected

        holder.itemView.setOnClickListener {
            model.selected = !model.selected
            onSelectedInterface?.onSelected(position, model)
        }
    }

    class VH(mItemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(mItemView) {
        val rlCustomImage: RelativeLayout = itemView.findViewById(R.id.main_rl_custom_image)
        val ivCustomCardCover: ImageView = itemView.findViewById(R.id.main_custom_card_cover)
        val tvName: TextView = itemView.findViewById(R.id.mainTvName)
        val ivCustomCardTag: ImageView = itemView.findViewById(R.id.main_custom_card_tag)
    }

    interface OnSelectedInterface {

        fun onSelected(position: Int, model: NewInterestCardModel)

    }


}