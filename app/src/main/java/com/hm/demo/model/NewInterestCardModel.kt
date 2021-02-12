package com.hm.demo.model

import java.io.Serializable

/**
 * Created by dumingwei on 2020/12/9
 *
 * Desc:
 */
class NewInterestCardModel : Serializable {

    var code: String? = null
    var categoryName: String? = null
    var subCategories: MutableList<NewInterestCardModel>? = null
    var bgColor: String? = null
    var selected: Boolean = false
}