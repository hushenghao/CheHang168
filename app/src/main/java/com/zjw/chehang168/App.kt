package com.zjw.chehang168

import android.app.Application
import com.bangcle.comapiprotect.CheckCodeUtil

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        CheckCodeUtil.getInstance(this)
            .updateServiceTime(System.currentTimeMillis())
    }
}
