package com.unbi.connect.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.*
import com.unbi.connect.R
import com.unbi.connect.Userdata
import com.unbi.connect.bind
import com.unbi.connect.fragment.FragmentAbout
import com.unbi.connect.fragment.FragmentCustomActivity
import com.unbi.connect.fragment.FragmentLogView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseMainActivity() {

    private val clicklistener = View.OnClickListener { view ->
        val id=view.id
        when (id) {
            R.id.edit_port_value ->{
                but_save.isEnabled=true
            }
            R.id.tv_custom_view -> startFragment(FragmentCustomActivity::class)
            R.id.tv_log_view -> startFragment(FragmentLogView::class)
            R.id.tv_about -> startFragment(FragmentAbout::class)
            R.id.but_save -> {
                Userdata.save(applicationContext,R.id.edit_port_value,edit_port.text.toString())
                Userdata.save(applicationContext,R.id.edit_password,edit_password.toString())
            }


        }
    }

    private val tv_ip: TextView by bind(R.id.tv_ip_adress, clicklistener)
    private val edit_port: EditText by bind(R.id.edit_port_value, clicklistener)
    private val edit_password: EditText by bind(R.id.edit_password, clicklistener)
    private val switch_custom_activity: Switch by bind(R.id.switch_enable_custom_activity, clicklistener)
    private val tv_custom_activity: TextView by bind(R.id.tv_custom_view, clicklistener)
    private val tv_log: TextView by bind(R.id.tv_log_view, clicklistener)
    private val tv_about: TextView by bind(R.id.tv_about, clicklistener)
    private val but_save: Button by bind(R.id.but_save, clicklistener)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        but_save.isEnabled = false
        switch_custom_activity.setOnCheckedChangeListener(checklistener)
    }

    private val checklistener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        Userdata.save(applicationContext, view.id, isChecked)
    }



}



private fun Userdata.save(applicationContext: Context?, id: Int, value: Any) {
    when (id) {

        R.id.edit_port_value -> port = value as Int
        R.id.edit_password -> global_password = value as String
        R.id.switch_enable_custom_activity -> iscustomactivity = value as Boolean

    }







    if (applicationContext == null) return
    val editor = applicationContext.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit()
    editor.putString("name", "Elena")
    editor.putInt("idName", 12)
    editor.apply()

}
