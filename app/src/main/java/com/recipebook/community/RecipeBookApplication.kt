package com.recipebook.community

import android.app.Application
import com.google.android.gms.ads.MobileAds

class RecipeBookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { /* initialization complete callback, optional */ }
    }
}