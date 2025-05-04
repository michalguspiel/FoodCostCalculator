package com.erdees.foodcostcalc.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.erdees.foodcostcalc.databinding.ProductAdCustomLayoutBinding
import com.erdees.foodcostcalc.utils.MyDispatchers
import com.erdees.foodcostcalc.utils.ads.populateNativeAdView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun Ad(
    adUnitId: String,
    modifier: Modifier = Modifier,
    onAdFailedToLoad: () -> Unit
) {
    val dispatchers : MyDispatchers = koinInject()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    val nativeAdState = remember { mutableStateOf<NativeAd?>(null) }
    val isActivityDestroyed = remember { mutableStateOf(false) }
    val latestOnAdFailedToLoad by rememberUpdatedState(onAdFailedToLoad)

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY || event == Lifecycle.Event.ON_STOP) {
                isActivityDestroyed.value = true
                nativeAdState.value?.destroy()
                nativeAdState.value = null
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            nativeAdState.value?.destroy()
        }
    }

    // Load Ad off the main thread
    LaunchedEffect(adUnitId) {
        withContext(dispatchers.ioDispatcher) {
            val videoOptions = VideoOptions.Builder().build()
            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

            val adLoader = AdLoader.Builder(context, adUnitId)
                .forNativeAd { nativeAd ->
                    if (isActivityDestroyed.value) {
                        nativeAd.destroy()
                        return@forNativeAd
                    }
                    nativeAdState.value?.destroy()
                    nativeAdState.value = nativeAd
                }
                .withNativeAdOptions(adOptions)
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        latestOnAdFailedToLoad()
                        Timber.e( "Ad failed to load: $error")
                    }

                    override fun onAdLoaded() {
                        if (isActivityDestroyed.value) {
                            nativeAdState.value?.destroy()
                            nativeAdState.value = null
                            return
                        }
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    AndroidViewBinding(
        modifier = modifier,
        factory = ProductAdCustomLayoutBinding::inflate,
        onReset = {
            nativeAdState.value?.destroy()
            nativeAdState.value = null
        },
        onRelease = {
            nativeAdState.value?.destroy()
            nativeAdState.value = null
        }
    ) {
        nativeAdState.value?.let { nativeAd ->
            populateNativeAdView(nativeAd, this.root)
        }
    }
}