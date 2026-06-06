package com.hm.demo.model

import java.io.Serializable

/** Created by dumingwei on 2017/10/10. */
class CheckBoxModel : Serializable {
    @JvmField var description: String
    var isChecked: Boolean
    var drawableResId = 0
    var imageUrl: String? = null

    constructor(description: String, checked: Boolean) {
        this.description = description
        isChecked = checked
    }

    constructor(description: String, checked: Boolean, drawableResId: Int) {
        this.description = description
        isChecked = checked
        this.drawableResId = drawableResId
    }

    override fun toString(): String {
        return "CheckBoxModel{" +
                "description='" +
                description +
                '\'' +
                ", checked=" +
                isChecked +
                '}'
    }
}
