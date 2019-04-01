package com.unbi.connect.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.annotation.NonNull
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.unbi.connect.R


abstract class BaseFragMent:Fragment(){
    lateinit var rootView:View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView=inflater.inflate(getLayout(), container, false)
//        findViewElements(rootView)
        return rootView
    }

    /**
     * @param the root view
     * you have to find all the view element in this
     */

//    abstract fun findViewElements(rootView: View?)

    /**
     * @return return the layout ingt value
     */
    abstract fun getLayout(): Int


}