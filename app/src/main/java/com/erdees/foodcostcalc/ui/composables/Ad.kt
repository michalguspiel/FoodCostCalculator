package com.erdees.foodcostcalc.ui.composables

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.erdees.foodcostcalc.databinding.ProductAdCustomLayoutBinding
import com.erdees.foodcostcalc.utils.ads.populateNativeAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.java.KoinJavaComponent.inject

@Composable
fun Ad(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    var currentNativeAd: NativeAd? = null
    val context = LocalContext.current
    val lifeCycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifeCycleOwner.lifecycle

    val isActivityDestroyed = remember { mutableStateOf(false) }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_STOP) {
                isActivityDestroyed.value = true
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isActivityDestroyed.value) {
        if (isActivityDestroyed.value) {
            currentNativeAd?.destroy()
            return@LaunchedEffect
        }
    }

    AndroidViewBinding(
        modifier = modifier,
        factory = ProductAdCustomLayoutBinding::inflate,
        onReset = {
            currentNativeAd?.destroy()
        },
        onRelease = {}
    ) {
        val videoOptions = VideoOptions.Builder()
            .build()
        val adOptions = NativeAdOptions.Builder()
            .setVideoOptions(videoOptions)
            .build()

        lateinit var adLoader: AdLoader

        adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { nativeAd ->
                currentNativeAd?.destroy()
                currentNativeAd = nativeAd

                if (!adLoader.isLoading) {
                    populateNativeAdView(nativeAd, this.root)
                }
            }
            .withNativeAdOptions(adOptions)
            .withAdListener(object : AdListener() {
                val firebaseAnalytics: FirebaseAnalytics by inject(FirebaseAnalytics::class.java)
                override fun onAdImpression() {
                    Log.i("Ad", "Ad impression")
                    super.onAdImpression()
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    firebaseAnalytics.logEvent("ad_failed_to_load", null)
                    Log.i(
                        "Ad",
                        "Failed to load ad: ${p0.message}, ${p0.code}"
                    )
                    super.onAdFailedToLoad(p0)
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
}