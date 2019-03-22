package com.unbi.connect.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import com.unbi.connect.STARTED_FROM
import com.unbi.connect.fragment.BaseFragMent
import com.unbi.connect.service.TCPservice
import kotlin.reflect.KClass


open class BaseMainActivity : AppCompatActivity(), ServiceConnection {
    private var mBound: Boolean = false//true whwn service is bounded
    var mService: TCPservice? = null

    protected inline fun <reified T : BaseFragMent> startFragment(kClass: KClass<T>) {

        //todo start the fragment here
    }


    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        val intent = Intent(this, TCPservice::class.java)
        intent.putExtra(STARTED_FROM,MainActivity::class.java.canonicalName)
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
        mBound = true
    }

}




