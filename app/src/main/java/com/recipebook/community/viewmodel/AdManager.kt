package com.recipebook.community.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.recipebook.community.data.AdConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class RewardedAdState {
    object NotLoaded : RewardedAdState()
    object Loading   : RewardedAdState()
    object Ready     : RewardedAdState()
    object Showing   : RewardedAdState()
    data class Failed(val message: String) : RewardedAdState()
}

class AdManager : ViewModel() {

    private var rewardedAd: RewardedAd? = null

    private val _state = MutableStateFlow<RewardedAdState>(RewardedAdState.NotLoaded)
    val state: StateFlow<RewardedAdState> = _state

    /** Call this ahead of time (e.g. when AddRecipeScreen opens) so the ad is ready when needed. */
    fun loadRewardedAd(context: Context) {
        if (_state.value is RewardedAdState.Loading || _state.value is RewardedAdState.Ready) return
        _state.value = RewardedAdState.Loading

        RewardedAd.load(
            context,
            AdConfig.REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _state.value = RewardedAdState.Ready
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    _state.value = RewardedAdState.Failed(error.message)
                    Log.w("AdManager", "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    /** Shows the ad if ready. onReward fires only if the user watches to completion. */
    fun showRewardedAd(
        activity: Activity,
        onReward: () -> Unit,
        onDismissedWithoutReward: () -> Unit = {}
    ) {
        val ad = rewardedAd
        if (ad == null) {
            _state.value = RewardedAdState.Failed("Ad not ready yet")
            return
        }

        var earnedReward = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                _state.value = RewardedAdState.NotLoaded
                if (!earnedReward) onDismissedWithoutReward()
                // Preload the next one immediately for next time
                loadRewardedAd(activity)
            }
            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                _state.value = RewardedAdState.Failed(error.message)
            }
            override fun onAdShowedFullScreenContent() {
                _state.value = RewardedAdState.Showing
            }
        }

        ad.show(activity) { rewardItem ->
            earnedReward = true
            onReward()
        }
    }
}