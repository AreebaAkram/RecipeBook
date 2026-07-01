package com.recipebook.community.data

object AdConfig {

    // ⚠️ TEST IDS — safe to ship while developing, Google's official test units.
    // Replace with your REAL ad unit IDs from admob.google.com before release.
    const val BANNER_AD_UNIT_ID    = "ca-app-pub-4485632034904156/8973022362"   // test banner
    const val REWARDED_AD_UNIT_ID  = "ca-app-pub-4485632034904156/7944182588"   // test rewarded

    // TODO: once you have a real AdMob account, replace above with:
    // const val BANNER_AD_UNIT_ID   = "ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
    // const val REWARDED_AD_UNIT_ID = "ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ"

    // Extension point for later — Amazon APS bidding price points etc.
    // const val AMAZON_APS_APP_KEY = ""
}