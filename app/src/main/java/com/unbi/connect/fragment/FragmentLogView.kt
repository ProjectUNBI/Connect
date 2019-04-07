package com.unbi.connect.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.unbi.connect.*
import kotlinx.android.synthetic.main.frag_logger.*

class FragmentLogView : BaseFragMent(), LogUpdater {


    lateinit var but_log_on: Button
    lateinit var but_log_off: Button
    lateinit var but_clear: Button
    lateinit var tv_log: TextView

    private val clicklistener = View.OnClickListener { view ->
        val id = view.id
        when (id) {
            R.id.but_log_toggle_on -> {
                ApplicationInstance.instance.isLogging = true
                but_log_on.visibility = GONE
                but_log_off.visibility = VISIBLE
            }
            R.id.but_log_toggle_off -> {
                ApplicationInstance.instance.isLogging = false
                but_log_on.visibility = VISIBLE
                but_log_off.visibility = GONE
            }
            R.id.but_log_clear -> {
                TODO("Clear the Log")
            }

        }


    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        but_log_on = rootView.findViewById(R.id.but_log_toggle_on)
        but_log_off = rootView.findViewById(R.id.but_log_toggle_off)
        but_clear = rootView.findViewById(R.id.but_log_clear)
        but_log_on.setOnClickListener(clicklistener)
        but_log_off.setOnClickListener(clicklistener)
        but_clear.setOnClickListener(clicklistener)
        if (ApplicationInstance.instance.isLogging) {
            but_log_on.visibility = GONE
            but_log_off.visibility = VISIBLE
        } else {
            but_log_on.visibility = VISIBLE
            but_log_off.visibility = GONE
        }
        tv_log = rootView.findViewById(R.id.tv_log_message)


        return rootView
    }


    override fun getLayout(): Int {
        return R.layout.frag_logger
    }

    override fun update(int: Int, msg: String) {
        val tag: String
        when (int) {
            LOG_TYPE_SUCCESS -> {
                tag = "SUCCESS: "
            }
            LOG_TYPE_WARNING -> {
                tag = "WARNING: "
            }
            LOG_TYPE_NORMAL -> {
                tag = "LOG: "
            }
            LOG_TYPE_ERROR -> {
                tag = "ERROR: "
            }
            else->{
                tag = "LOG: "
            }

        }//end of when
        val string="\n\n"+tag+msg
        tv_log.append(string)
    }
}

interface LogUpdater {
    fun update(int: Int, msg: String)
}