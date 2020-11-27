package com.skysoul.layoutmanagers.gallerscale

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Listen for changes to the selected item
 *
 * @author skysoul
 */
interface OnItemSelectedListener {
    /**
     * @param recyclerView The RecyclerView which item view belong to.
     * @param item         The current selected view
     * @param position     The current selected view's position
     */
    fun onItemSelected(recyclerView: RecyclerView?, item: View?, position: Int)
}