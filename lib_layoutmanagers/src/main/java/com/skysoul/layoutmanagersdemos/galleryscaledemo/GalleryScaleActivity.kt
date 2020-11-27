package com.skysoul.layoutmanagersdemos.galleryscaledemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skysoul.layoutmanagers.R
import com.skysoul.layoutmanagers.gallerscale.GalleryScaleLayoutManager
import kotlinx.android.synthetic.main.scale_mainact.*

class GalleryScaleActivity : AppCompatActivity() {

    lateinit var layoutManager: GalleryScaleLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scale_mainact)

        toolbar.setNavigationOnClickListener { finish() }

        var titles = arrayListOf<String>()
        for(i in 0..50){
            titles.add("Hello$i")
        }
        var demoAdapter = GalleryScaleAdapter(titles)

        layoutManager = GalleryScaleLayoutManager()
        layoutManager.attach(main_recycle,12)
//        layoutManager.stepScale=1f
//        layoutManager.maskRate = 0f
        et_scalerate.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                try {
                    layoutManager.stepScale = et_scalerate.text.toString().toFloat()
                    demoAdapter.notifyDataSetChanged()
                }catch (e:Exception){

                }
            }
        }
        et_maskrate.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                try {
                    layoutManager.maskRate = et_maskrate.text.toString().toFloat()
                    demoAdapter.notifyDataSetChanged()
                }catch (e:Exception){

                }
            }
        }
        et_maxshow.setOnFocusChangeListener { v, hasFocus ->
            if(!hasFocus){
                try {
                    layoutManager.maxShowCount = et_maxshow.text.toString().toInt()
                    demoAdapter.notifyDataSetChanged()
                }catch (e:Exception){

                }
            }
        }




        demoAdapter.setOnItemClickListener { view, position ->
            main_tv_recycle_info_1.text = "click $position"
            main_recycle.smoothScrollToPosition(position)
        }
        btn_looper.setOnClickListener {
            layoutManager.isLooper = !layoutManager.isLooper
            demoAdapter.notifyDataSetChanged()
        }
        btn_circle.setOnClickListener {
            layoutManager.isCircle = !layoutManager.isCircle
            demoAdapter.notifyDataSetChanged()
        }
        main_recycle.adapter = demoAdapter
    }
}