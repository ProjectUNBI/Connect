package com.unbi.connect.activity

import android.content.Context
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.*
import com.google.gson.Gson
import com.unbi.connect.Userdata
import com.unbi.connect.bind
import com.unbi.connect.fragment.FragmentCustomActivity
import com.unbi.connect.fragment.FragmentLogView
import android.view.inputmethod.InputMethodManager
import com.unbi.connect.R


class MainActivity : BaseMainActivity() {
    private val focusschangelistener = View.OnFocusChangeListener { v, hasfocus ->
        val id = v.id
        if (!hasfocus) {
            return@OnFocusChangeListener;
        }

        when (id) {
            R.id.edit_port_value -> {
                but_save.isEnabled = true
            }
            R.id.edit_password -> {
                but_save.isEnabled = true
                edit_password.setTransformationMethod(null);
            }
        }

    }

    private val clicklistener = View.OnClickListener { view ->
        val id = view.id
        when (id) {
            R.id.tv_custom_view -> {
                mesagecapurer=startFragment(FragmentCustomActivity::class.java) as FragmentCustomActivity
            }
            R.id.tv_log_view -> startFragment(FragmentLogView::class.java)
//            R.id.tv_about -> startFragment(FragmentAbout::class)
            R.id.but_save -> {

                Userdata.instance.save(applicationContext, R.id.edit_password, edit_password.text.toString())
                val string = Gson().toJson(Userdata)

                val port = edit_port.text.toString().toInt()
                if (port < 1 || port > 65535) {
                    edit_port.setError("Port should be from 1-65535")
                    return@OnClickListener
                }
                if(Userdata.instance.ipport.port!=edit_port.text.toString().toInt()){
                    Userdata.instance.save(applicationContext, R.id.edit_port_value, port)
                    mService?.restartServer()
                }
                edit_password.setTransformationMethod(PasswordTransformationMethod())
                but_save.isEnabled = false
                Userdata.instance.isReadedfromSpref=false
                hidefocus(view)
//                Toast.makeText(applicationContext,"Please force stop and restart the app else tasker plugin will not working properly...",Toast.LENGTH_LONG).show()
            }
        }
    }

    private val tv_ip: TextView by bind(R.id.tv_ip_adress)
    private val edit_port: EditText by bind(R.id.edit_port_value)
    private val edit_password: EditText by bind(R.id.edit_password)
    private val switch_custom_activity: Switch by bind(R.id.switch_enable_custom_activity)
    private val tv_custom_activity: TextView by bind(R.id.tv_custom_view)
    private val tv_log: TextView by bind(R.id.tv_log_view)
//    private val tv_about: TextView by bind(R.id.tv_about)
    private val but_save: Button by bind(R.id.but_save)
    private val switch_toast: Switch by bind(R.id.switch_enable_toast)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Userdata.instance.readfromSpref(applicationContext)
        setContentView(com.unbi.connect.R.layout.activity_main)
        but_save.isEnabled = false
        switch_custom_activity.setOnCheckedChangeListener(checklistener)
        switch_toast.setOnCheckedChangeListener(checklistener)
        switch_custom_activity.isChecked = Userdata.instance.iscustomactivity
        switch_toast.isChecked = Userdata.instance.isToast
        tv_ip.setOnClickListener(clicklistener)
        tv_ip.setText(Userdata.instance.ipport.ip)
        edit_port.setOnFocusChangeListener(focusschangelistener)
        edit_password.setOnFocusChangeListener(focusschangelistener)
        tv_custom_activity.setOnClickListener(clicklistener)
        tv_log.setOnClickListener(clicklistener)
//        tv_about.setOnClickListener(clicklistener)
        but_save.setOnClickListener(clicklistener)
        edit_port.setText(Userdata.instance.ipport.port.toString())
        edit_password.setText(Userdata.instance.global_password)
        //frame layout
        fragmentLay=findViewById(R.id.fragment_main)
        fragmentLay?.visibility=View.GONE
    }

    private val checklistener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        Userdata.instance.save(applicationContext, view.id, isChecked)
    }


    private fun hidefocus(v: View?) {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (v != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
            }
        } catch (ignored: Exception) {
        }

        edit_port.setSelected(false)
        edit_port.setFocusable(false)
        edit_port.setFocusableInTouchMode(true)
        edit_password.setSelected(false)
        edit_password.setFocusable(false)
        edit_password.setFocusableInTouchMode(true)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

}







