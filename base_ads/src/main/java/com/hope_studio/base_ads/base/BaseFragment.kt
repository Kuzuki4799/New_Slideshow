package com.hope_studio.base_ads.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.FragmentUtils

abstract class BaseFragment : Fragment() {

    abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    val tAG: String get() = BaseFragment::class.java.simpleName

    fun addFragment(fragment: Fragment, @IdRes frameId: Int) {
        try {
            FragmentUtils.add(childFragmentManager, fragment, frameId)
        } catch (ignored: Exception) {
        }
    }

    fun replaceFragment(fragment: Fragment, @IdRes frameId: Int) {
        try {
            FragmentUtils.replace(childFragmentManager, fragment, frameId, true)
        } catch (ignored: Exception) {
        }
    }

    fun removeFragment(fragment: Fragment) {
        try {
            FragmentUtils.remove(fragment)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun baseActivity(): BaseActivity {
        return activity as BaseActivity
    }
}