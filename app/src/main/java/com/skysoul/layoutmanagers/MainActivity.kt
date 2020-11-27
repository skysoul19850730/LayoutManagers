package com.skysoul.layoutmanagers

import android.R.layout
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skysoul.layoutmanagersdemos.adapter.BaseRecyclerAdapter
import com.skysoul.layoutmanagersdemos.adapter.SmartViewHolder
import com.skysoul.layoutmanagersdemos.galleryscaledemo.GalleryScaleActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemClickListener {

    var classes: ArrayList<Class<*>?> = arrayListOf(GalleryScaleActivity::class.java)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        main_recycle.layoutManager = LinearLayoutManager(this).apply {
            orientation = RecyclerView.VERTICAL
        }
        main_recycle.adapter = object :
            BaseRecyclerAdapter<Class<*>?>(classes, layout.simple_list_item_2, this) {
            override fun onBindViewHolder(
                holder: SmartViewHolder,
                model: Class<*>?,
                position: Int
            ) {
                holder.text(android.R.id.text1, model?.simpleName)
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        startActivity(Intent(this,classes[position]))
    }


}