package com.unbi.connect.activity

import android.support.v7.app.AppCompatActivity
import com.unbi.connect.fragment.BaseFragMent
import kotlin.reflect.KClass

open class BaseMainActivity :AppCompatActivity(){
    protected inline fun <reified T: BaseFragMent> startFragment(kClass: KClass<T>) {

    }

}
