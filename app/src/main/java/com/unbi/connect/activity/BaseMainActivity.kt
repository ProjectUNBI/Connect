package com.unbi.connect.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.unbi.connect.NULL_WORD
import com.unbi.connect.R
import com.unbi.connect.STARTED_FROM
import com.unbi.connect.activity.ServiceToActivity.Companion.DEFAULT
import com.unbi.connect.activity.ServiceToActivity.Companion.LOG_DATA
import com.unbi.connect.fragment.BaseFragMent
import com.unbi.connect.fragment.LogUpdater
import com.unbi.connect.fragment.caturedMessag
import com.unbi.connect.messaging.MyMessage
import com.unbi.connect.service.TCPservice
import kotlin.reflect.KClass


open class BaseMainActivity : AppCompatActivity(), ServiceConnection, ServiceToActivity {

    private var mBound: Boolean = false//true whwn service is bounded
    var mService: TCPservice? = null
    var fragmentLay: FrameLayout? = null
    var mesagecapurer: caturedMessag? = null
    var logUpdater: LogUpdater? = null


    protected inline fun <reified T : BaseFragMent> startFragment(
        kClass: Class<out T>,
        bundle: Bundle? = null
    ): BaseFragMent? {

        /**
         * we are making the layout of fragment visible if it is Gone
         */
        if (fragmentLay?.visibility == View.GONE) {
            fragmentLay?.visibility = View.VISIBLE
        }
        //todo start the fragment here
        var xfragment: BaseFragMent? = null
        try {
            xfragment = kClass.newInstance()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        }
        if (xfragment == null) {
            return null
        }
        if (bundle != null) {
            xfragment.arguments = bundle
        }
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragment_main, xfragment as Fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()// todo  java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        return xfragment

    }


    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        val intent = Intent(this, TCPservice::class.java)
        intent.putExtra(STARTED_FROM, MainActivity::class.java.canonicalName)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
        startService(intent)
    }


    override fun onStop() {
        super.onStop()
        unbindService(this)
        mBound = false
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        mBound = false;
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as TCPservice.LocalBinder
        mService = binder.service as TCPservice
        mService?.servicToActivity = this
        mBound = true
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount <= 1) {
            if (fragmentLay?.visibility == View.VISIBLE) {
                fragmentLay?.visibility = View.GONE
            }
        }
        super.onBackPressed()
    }

    override fun sendtoActivity(type: Int, vararg any: Any) {
        when (type) {
            DEFAULT -> {
                /**
                 * the var arg is of MyMessage object type so we will pass it
                 * to the Fragment of Customactivity
                 */
                val message = any[0] as MyMessage
                if (message.tag == null || message.tag.equals(NULL_WORD)) {
                    return
                }
                mesagecapurer?.captured(message.tag)

            }
            LOG_DATA->{
                val logtype=any[0] as Int;
                val logmsg=any [1]as String
                logUpdater?.update(logtype,logmsg)
            }
        }

    }
}

interface ServiceToActivity {
    companion object {
        val DEFAULT: Int = 0
        val LOG_DATA: Int = 1
    }

    fun sendtoActivity(type: Int, vararg any: Any)
}



