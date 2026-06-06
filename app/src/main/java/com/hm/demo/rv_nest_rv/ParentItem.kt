package com.hm.demo.rv_nest_rv

// Data model used by ParentAdapter and RvNestRvVBActivity
data class ParentItem(
    val title: String,
    val children: List<String>
)

