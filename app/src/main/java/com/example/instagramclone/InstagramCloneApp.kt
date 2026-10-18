package com.example.instagramclone

import android.app.Application
import com.example.instagramclone.data.local.TokenStore

class InstagramCloneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
