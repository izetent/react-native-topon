package com.topon

import android.app.Activity
import android.text.TextUtils
import com.anythink.core.api.ATSDK
import com.anythink.core.api.NetTrafficeCallback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule

@ReactModule(name = ToponModule.NAME)
class ToponModule(reactContext: ReactApplicationContext) :
  NativeToponSpec(reactContext) {

  private val rewardedHelpers = mutableMapOf<String, RewardedVideoHelper>()
  private val interstitialHelpers = mutableMapOf<String, InterstitialHelper>()
  private val bannerHelpers = mutableMapOf<String, BannerHelper>()
  private var listenerCount: Int = 0

  override fun getName(): String = NAME

  internal fun currentActivitySafe(): Activity? = getCurrentActivity()

  internal fun reactContext(): ReactApplicationContext = reactApplicationContext

  override fun addListener(eventType: String) {
    listenerCount += 1
  }

  override fun removeListeners(count: Double) {
    listenerCount = (listenerCount - count.toInt()).coerceAtLeast(0)
  }

  fun sendEvent(eventName: String, data: WritableMap) {
    if (listenerCount <= 0) {
      return
    }
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, data)
  }

  override fun init(appId: String, appKey: String) {
    MsgTools.printMsg("initSDK: $appId:$appKey")
    ATSDK.init(reactApplicationContext, appId, appKey)
  }

  override fun getSDKVersionName(promise: Promise) {
    val sdkVersionName = ATSDK.getSDKVersionName()
    MsgTools.printMsg("getSDKVersionName: $sdkVersionName")
    promise.resolve(sdkVersionName)
  }

  override fun isCnSDK(promise: Promise) {
    val isCn = ATSDK.isCnSDK()
    MsgTools.printMsg("isCnSDK: $isCn")
    promise.resolve(isCn)
  }

  override fun setExcludeMyOfferPkgList(packages: ReadableArray) {
    val packageList = packages.toArrayList().mapNotNull { it as? String }
    if (packageList.isNotEmpty()) {
      packageList.forEach { MsgTools.printMsg("exclude MyOffer: $it") }
      ATSDK.setExcludePackageList(packageList)
    }
  }

  override fun initCustomMap(customMapJson: String) {
    MsgTools.printMsg("initCustomMap: $customMapJson")
    if (!TextUtils.isEmpty(customMapJson)) {
      val map = CommonUtil.jsonStringToMap(customMapJson)
      ATSDK.initCustomMap(map)
    }
  }

  override fun setPlacementCustomMap(placementId: String, customMapJson: String) {
    MsgTools.printMsg("setPlacementCustomMap: $placementId:$customMapJson")
    if (!TextUtils.isEmpty(customMapJson)) {
      val map = CommonUtil.jsonStringToMap(customMapJson)
      ATSDK.initPlacementCustomMap(placementId, map)
    }
  }

  override fun setGDPRLevel(level: Double) {
    val gdprLevel = level.toInt()
    MsgTools.printMsg("setGDPRLevel: $gdprLevel")
    ATSDK.setGDPRUploadDataLevel(reactApplicationContext, gdprLevel)
  }

  override fun getGDPRLevel(promise: Promise) {
    val gdprLevel = ATSDK.getGDPRDataLevel(reactApplicationContext)
    MsgTools.printMsg("getGDPRLevel: $gdprLevel")
    promise.resolve(gdprLevel)
  }

  override fun getUserLocation(promise: Promise) {
    MsgTools.printMsg("getUserLocation")
    ATSDK.checkIsEuTraffic(reactApplicationContext, object : NetTrafficeCallback {
      override fun onResultCallback(result: Boolean) {
        MsgTools.printMsg("getUserLocation - onResultCallback: $result")
        promise.resolve(if (result) 1 else 2)
      }

      override fun onErrorCallback(error: String) {
        MsgTools.printMsg("getUserLocation - onErrorCallback: $error")
        promise.resolve(2)
      }
    })
  }

  override fun showGDPRAuth() {
    MsgTools.printMsg("showGDPRAuth")
    currentActivitySafe()?.runOnUiThread {
      ATSDK.showGdprAuth(reactApplicationContext)
    }
  }

  override fun setLogDebug(isDebug: Boolean) {
    MsgTools.setLogDebug(isDebug)
    MsgTools.printMsg("setLogDebug: $isDebug")
    ATSDK.setNetworkLogDebug(isDebug)
  }

  override fun deniedUploadDeviceInfo(keys: ReadableArray) {
    val keyList = keys.toArrayList().mapNotNull { it as? String }
    if (keyList.isNotEmpty()) {
      keyList.forEach { MsgTools.printMsg("deniedUploadDeviceInfo: $it") }
      ATSDK.deniedUploadDeviceInfo(*keyList.toTypedArray())
    }
  }

  override fun rewardedLoadAd(placementId: String, settingsJson: String) {
    rewardedHelper(placementId).loadRewardedVideo(placementId, settingsJson)
  }

  override fun rewardedShowAd(placementId: String) {
    rewardedHelper(placementId).showVideo("")
  }

  override fun rewardedShowAdInScenario(placementId: String, scenario: String) {
    rewardedHelper(placementId).showVideo(scenario)
  }

  override fun rewardedHasAdReady(placementId: String, promise: Promise) {
    promise.resolve(rewardedHelper(placementId).isAdReady())
  }

  override fun rewardedCheckAdStatus(placementId: String, promise: Promise) {
    promise.resolve(rewardedHelper(placementId).checkAdStatus())
  }

  override fun interstitialLoadAd(placementId: String, settingsJson: String) {
    interstitialHelper(placementId).loadInterstitial(placementId, settingsJson)
  }

  override fun interstitialShowAd(placementId: String) {
    interstitialHelper(placementId).showInterstitial("")
  }

  override fun interstitialShowAdInScenario(placementId: String, scenario: String) {
    interstitialHelper(placementId).showInterstitial(scenario)
  }

  override fun interstitialHasAdReady(placementId: String, promise: Promise) {
    promise.resolve(interstitialHelper(placementId).isAdReady())
  }

  override fun interstitialCheckAdStatus(placementId: String, promise: Promise) {
    promise.resolve(interstitialHelper(placementId).checkAdStatus())
  }

  override fun bannerLoadAd(placementId: String, settingsJson: String) {
    bannerHelper(placementId).loadBanner(placementId, settingsJson)
  }

  override fun bannerShowAdInRectangle(placementId: String, rectJson: String) {
    bannerHelper(placementId).showBannerWithRect(rectJson, "")
  }

  override fun bannerShowAdInPosition(placementId: String, position: String) {
    bannerHelper(placementId).showBannerWithPosition(position, "")
  }

  override fun bannerShowAdInRectangleAndScenario(
    placementId: String,
    rectJson: String,
    scenario: String
  ) {
    bannerHelper(placementId).showBannerWithRect(rectJson, scenario)
  }

  override fun bannerShowAdInPositionAndScenario(
    placementId: String,
    position: String,
    scenario: String
  ) {
    bannerHelper(placementId).showBannerWithPosition(position, scenario)
  }

  override fun bannerHideAd(placementId: String) {
    bannerHelper(placementId).hideBanner()
  }

  override fun bannerReShowAd(placementId: String) {
    bannerHelper(placementId).reshowBanner()
  }

  override fun bannerRemoveAd(placementId: String) {
    bannerHelper(placementId).removeBanner()
  }

  override fun bannerHasAdReady(placementId: String, promise: Promise) {
    promise.resolve(bannerHelper(placementId).isAdReady())
  }

  override fun bannerCheckAdStatus(placementId: String, promise: Promise) {
    promise.resolve(bannerHelper(placementId).checkAdStatus())
  }

  private fun rewardedHelper(placementId: String): RewardedVideoHelper =
    rewardedHelpers.getOrPut(placementId) { RewardedVideoHelper(this) }

  private fun interstitialHelper(placementId: String): InterstitialHelper =
    interstitialHelpers.getOrPut(placementId) { InterstitialHelper(this) }

  private fun bannerHelper(placementId: String): BannerHelper =
    bannerHelpers.getOrPut(placementId) { BannerHelper(this) }

  companion object {
    const val NAME = "Topon"
  }
}
