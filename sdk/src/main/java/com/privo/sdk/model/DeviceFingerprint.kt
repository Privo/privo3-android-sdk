package com.privo.sdk.model

import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import com.squareup.moshi.JsonClass
import android.view.WindowManager

@JsonClass(generateAdapter = true)
data class DeviceInfo(
    val isMobile: Boolean,
    val devModel: String,
    val devVendor: String,
    val osName: String,
    val osVersion: String,
    val screenWidth: Int,
    val screenHeight: Int,
    val colorDepth: Int,
    val pixelRatio: Double
)

@JsonClass(generateAdapter = true)
data class BrowserInfo(
    val name: String,
    val version: String,
    val plugins: Array<String>
)
@JsonClass(generateAdapter = true)
class GPU()
@JsonClass(generateAdapter = true)
data class Fonts(val fonts: Array<String>)

@JsonClass(generateAdapter = true)
class DeviceFingerprint(
    val idForVendor: String,
    val devInfo: DeviceInfo,
    val browserInfo: BrowserInfo,
    val gpu: GPU,
    val fonts: Fonts
)

@JsonClass(generateAdapter = true)
data class DeviceFingerprintResponse(val id: String, val exp: Int)

class DeviceFingerprintBuilder (context: Context) {
    private val deviceFingerprint: DeviceFingerprint

    init {
        val metrics = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metrics)

        val devInfo = DeviceInfo(
            true,
            Build.DEVICE,
            Build.MANUFACTURER,
            Build.VERSION.BASE_OS.ifEmpty { "Android"},
            Build.VERSION.RELEASE,
            metrics.widthPixels,
            metrics.heightPixels,
            32,
            metrics.density.toDouble()
        )
        val browserInfo = BrowserInfo("AndroidWebView", "1", emptyArray())
        val gpu = GPU()
        val fonts = Fonts(emptyArray())
        val idForVendor = DeviceIdentifier(context).getIdentifier()

        this.deviceFingerprint = DeviceFingerprint(idForVendor,devInfo,browserInfo,gpu, fonts)
    }
    fun build() = deviceFingerprint
}
