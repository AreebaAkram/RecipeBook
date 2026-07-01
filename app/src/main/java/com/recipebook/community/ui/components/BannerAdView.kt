package com.recipebook.community.ui.components


import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.recipebook.community.data.AdConfig

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth().height(60.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdConfig.BANNER_AD_UNIT_ID
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}