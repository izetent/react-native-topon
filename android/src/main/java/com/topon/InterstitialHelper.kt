package com.topon

import android.text.TextUtils
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATAdStatusInfo
import com.anythink.core.api.AdError
import com.anythink.interstitial.api.ATInterstitial
import com.anythink.interstitial.api.ATInterstitialListener
import com.facebook.react.bridge.Arguments
import org.json.JSONObject
import java.util.HashMap

class InterstitialHelper(module: ToponModule) : BaseHelper(module) {

  private var interstitialAd: ATInterstitial? = null
  private var placementId: String = ""
  private var isReady: Boolean = false

  private fun ensureInterstitial(initPlacementId: String) {
    if (interstitialAd == null || placementId != initPlacementId) {
      placementId = initPlacementId
      MsgTools.printMsg("initInterstitial: $placementId")
      val context = currentActivity() ?: reactContext()
      interstitialAd = ATInterstitial(context, placementId).apply {
        setAdListener(object : ATInterstitialListener {
          override fun onInterstitialAdLoaded() {
            MsgTools.printMsg("onInterstitialAdLoaded: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            sendEvent(Const.InterstitialCallback.LoadedCallbackKey, map)
          }

          override fun onInterstitialAdLoadFail(adError: AdError) {
            MsgTools.printMsg(
              "onInterstitialAdLoadFail: $placementId, ${adError.fullErrorInfo}"
            )
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
            sendEvent(Const.InterstitialCallback.LoadFailCallbackKey, map)
          }

          override fun onInterstitialAdClicked(adInfo: ATAdInfo) {
            MsgTools.printMsg("onInterstitialAdClicked: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.InterstitialCallback.ClickCallbackKey, map)
          }

          override fun onInterstitialAdShow(adInfo: ATAdInfo) {
            MsgTools.printMsg("onInterstitialAdShow: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.InterstitialCallback.ShowCallbackKey, map)
          }

          override fun onInterstitialAdClose(adInfo: ATAdInfo) {
            MsgTools.printMsg("onInterstitialAdClose: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.InterstitialCallback.CloseCallbackKey, map)
          }

          override fun onInterstitialAdVideoStart(adInfo: ATAdInfo) {
            MsgTools.printMsg("onInterstitialAdVideoStart: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.InterstitialCallback.PlayStartCallbackKey, map)
          }

          override fun onInterstitialAdVideoEnd(adInfo: ATAdInfo) {
            MsgTools.printMsg("onInterstitialAdVideoEnd: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.InterstitialCallback.PlayEndCallbackKey, map)
          }

          override fun onInterstitialAdVideoError(adError: AdError) {
            MsgTools.printMsg(
              "onInterstitialAdVideoError: $placementId, ${adError.fullErrorInfo}"
            )
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
            sendEvent(Const.InterstitialCallback.PlayFailCallbackKey, map)
          }
        })
      }
    }
  }

  fun loadInterstitial(placementId: String, settingsJson: String) {
    MsgTools.printMsg("loadInterstitial: $placementId, settings: $settingsJson")
    runOnUiThread {
      ensureInterstitial(placementId)
      val ad = interstitialAd ?: return@runOnUiThread

      if (!TextUtils.isEmpty(settingsJson)) {
        try {
          val jsonObject = JSONObject(settingsJson)
          val localExtra = HashMap<String, Any?>()
          if (jsonObject.has(Const.Interstitial.UseRewardedVideoAsInterstitial) &&
            jsonObject.optBoolean(Const.Interstitial.UseRewardedVideoAsInterstitial)
          ) {
            localExtra["is_use_rewarded_video_as_interstitial"] = true
          }
          fillMapFromJsonObject(localExtra, jsonObject)
          ad.setLocalExtra(localExtra)
        } catch (t: Throwable) {
          t.printStackTrace()
        }
      }

      ad.load()
    }
  }

  fun showInterstitial(scenario: String) {
    MsgTools.printMsg("showInterstitial: $placementId, scenario: $scenario")
    runOnUiThread {
      val ad = interstitialAd
      if (ad != null) {
        val activity = currentActivity()
        if (activity != null) {
          isReady = false
          ad.show(activity, scenario)
        } else {
          MsgTools.printMsg("showInterstitial error, current activity is null $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.ErrorMsg, "current activity is null")
          sendEvent(Const.InterstitialCallback.PlayFailCallbackKey, map)
        }
      } else {
        MsgTools.printMsg("showInterstitial error, you must call loadInterstitial first $placementId")
        val map = Arguments.createMap()
        map.putString(Const.CallbackKey.PlacementId, placementId)
        map.putString(Const.CallbackKey.ErrorMsg, "you must call loadInterstitial first")
        sendEvent(Const.InterstitialCallback.LoadFailCallbackKey, map)
      }
    }
  }

  fun isAdReady(): Boolean {
    MsgTools.printMsg("interstitial isAdReady: $placementId")
    return try {
      interstitialAd?.isAdReady ?: run {
        MsgTools.printMsg("interstitial isAdReady error, you must call loadInterstitial first $placementId")
        isReady
      }
    } catch (t: Throwable) {
      MsgTools.printMsg("interstitial isAdReady Throwable: ${t.message}")
      isReady
    }
  }

  fun checkAdStatus(): String {
    MsgTools.printMsg("interstitial checkAdStatus: $placementId")
    return try {
      val statusInfo: ATAdStatusInfo? = interstitialAd?.checkAdStatus()
      if (statusInfo != null) {
        val jsonObject = JSONObject()
        jsonObject.put("isLoading", statusInfo.isLoading)
        jsonObject.put("isReady", statusInfo.isReady)
        jsonObject.put("adInfo", statusInfo.getATTopAdInfo())
        val result = jsonObject.toString()
        MsgTools.printMsg("interstitial checkAdStatus: $placementId, $result")
        result
      } else {
        MsgTools.printMsg("interstitial checkAdStatus error, you must call loadInterstitial first $placementId")
        ""
      }
    } catch (t: Throwable) {
      MsgTools.printMsg("interstitial checkAdStatus Throwable: ${t.message}")
      ""
    }
  }
}
