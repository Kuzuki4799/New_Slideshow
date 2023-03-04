package com.hope_studio.base_ads.utils

import android.content.Context
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import androidx.recyclerview.widget.RecyclerView

object RecyclerUtils {

    fun layoutLinear(context: Context?, recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun layoutLinearHorizontal(context: Context?, recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun layoutGird(context: Context, count: Int, dipSpace: Int, recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, count)
        recyclerView.addItemDecoration(GridUtils(count, PixelUtil.dpToPx(context, dipSpace), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun layoutGirdDefault(context: Context?, count: Int, recyclerView: RecyclerView) {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, count)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun layoutGirdInterval(
        context: Context, count: Int, interval: Int, dipSpace: Int, recyclerView: RecyclerView
    ) {
        recyclerView.setHasFixedSize(true)
        val mLayoutManager = GridLayoutManager(context, count)
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if ((position + 1) % (interval + 1) == 0) {
                    return count
                }
                return 1
            }
        }
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(
            GridAdUtils(count, interval, PixelUtil.dpToPx(context, dipSpace), true)
        )
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun layoutGirdDefaultInterval(
        context: Context, count: Int, interval: Int, dipSpace: Int, recyclerView: RecyclerView
    ) {
        recyclerView.setHasFixedSize(true)
        val mLayoutManager = GridLayoutManager(context, count)
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if ((position + 1) % (interval + 1) == 0) {
                    return count
                }
                return 1
            }
        }
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(GridUtils(count, PixelUtil.dpToPx(context, dipSpace), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
    }

    fun getLayoutLinear(context: Context?, recyclerView: RecyclerView): LinearLayoutManager {
        recyclerView.setHasFixedSize(true)
        val layoutManager =  LinearLayoutManager(context)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        return layoutManager
    }

    fun getLayoutGirdDefaultInterval(
        context: Context, count: Int, interval: Int, recyclerView: RecyclerView
    ): GridLayoutManager {
        recyclerView.setHasFixedSize(true)
        val mLayoutManager = GridLayoutManager(context, count)
        mLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if ((position + 1) % (interval + 1) == 0) {
                    return count
                }
                return 1
            }
        }
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.isNestedScrollingEnabled = false
        return mLayoutManager
    }

    fun layoutStaggeredGirdDefault(orientation: Int, count: Int, recyclerView: RecyclerView) {
        val layoutManager = StaggeredGridLayoutManager(count, orientation)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = null
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
//                if(newState == RecyclerView.SCROLL_STATE_IDLE){
//                    recyclerView.invalidateItemDecorations()
//                }
                (recyclerView.layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
            }
        })
        recyclerView.setHasFixedSize(true)
    }

    fun layoutStaggeredGird(
        context: Context, orientation: Int, count: Int, dipSpace: Int, recyclerView: RecyclerView
    ) {
        val layoutManager = StaggeredGridLayoutManager(count, orientation)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(GridUtils(count, PixelUtil.dpToPx(context, dipSpace), true))
        recyclerView.itemAnimator = null
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                (recyclerView.layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
            }
        })
        recyclerView.setHasFixedSize(true)
        (recyclerView.layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments()
    }
}