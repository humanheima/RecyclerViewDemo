package com.hm.demo.test_payload

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.hm.demo.R
import com.hm.demo.databinding.ActivityPayloadTestBinding

/**
 * Created by p_dmweidu on 2024/11/9
 * Desc: 测试Payload的使用
 */
class PayloadTestActivity : AppCompatActivity() {


    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, PayloadTestActivity::class.java)
            context.startActivity(starter)
        }
    }

    private lateinit var binding: ActivityPayloadTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayloadTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpUI()

    }

    private lateinit var adapter: PayloadTestAdapter
    private fun setUpUI() {
        val list = arrayListOf<PayloadBean>()
        //for (i in 0..100) {
        for (i in 0 until 1) {
            val bean = PayloadBean(R.drawable.pic_2, "马斯克$i", "特朗普$i")
            list.add(bean)
        }
        adapter = PayloadTestAdapter(list)
        binding.rvTestPayload.adapter = adapter
        binding.rvTestPayload.layoutManager = LinearLayoutManager(this)


        binding.btnTestPayload.setOnClickListener {
            adapter.updateItemTextPayload(0, "马斯克，下一届话事人")
        }

        binding.btnTestNormalNotifyItemChanged.setOnClickListener {
            adapter.updateItemText(0, "马斯克，下一届话事人哈哈")
        }

    }
}