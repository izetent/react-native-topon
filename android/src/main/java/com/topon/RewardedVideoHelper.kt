package com.topon

import com.anythink.core.api.ATAdConst
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATAdStatusInfo
import com.anythink.core.api.AdError
import com.anythink.rewardvideo.api.ATRewardVideoAd
import com.anythink.rewardvideo.api.ATRewardVideoListener
import com.facebook.react.bridge.Arguments
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class RewardedVideoHelper(module: ToponModule) : BaseHelper(module) {

  private var rewardVideoAd: ATRewardVideoAd? = null
  private var placementId: String = ""
  private var isReady: Boolean = false

  private fun ensureRewardVideoAd(initPlacementId: String) {
    if (rewardVideoAd == null || placementId != initPlacementId) {
      placementId = initPlacementId
      MsgTools.printMsg("initRewardedVideo placementId: $placementId")
      val context = currentActivity() ?: reactContext()
      rewardVideoAd = ATRewardVideoAd(context, placementId).apply {
        setAdListener(object : ATRewardVideoListener {
          override fun onRewardedVideoAdLoaded() {
            MsgTools.printMsg("onRewardedVideoAdLoaded: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            sendEvent(Const.RewardVideoCallback.LoadedCallbackKey, map)
          }

          override fun onRewardedVideoAdFailed(adError: AdError) {
            MsgTools.printMsg(
              "onRewardedVideoAdFailed: $placementId, ${adError.fullErrorInfo}"
            )
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
            sendEvent(Const.RewardVideoCallback.LoadFailCallbackKey, map)
          }

          override fun onRewardedVideoAdPlayStart(adInfo: ATAdInfo) {
            MsgTools.printMsg("onRewardedVideoAdPlayStart: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.PlayStartCallbackKey, map)
          }

          override fun onRewardedVideoAdPlayEnd(adInfo: ATAdInfo) {
            MsgTools.printMsg("onRewardedVideoAdPlayEnd: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.PlayEndCallbackKey, map)
          }

          override fun onRewardedVideoAdPlayFailed(adError: AdError, adInfo: ATAdInfo) {
            MsgTools.printMsg(
              "onRewardedVideoAdPlayFailed: $placementId, ${adError.fullErrorInfo}"
            )
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.PlayFailCallbackKey, map)
          }

          override fun onRewardedVideoAdClosed(adInfo: ATAdInfo) {
            MsgTools.printMsg("onRewardedVideoAdClosed: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.CloseCallbackKey, map)
          }

          override fun onRewardedVideoAdPlayClicked(adInfo: ATAdInfo) {
            MsgTools.printMsg("onRewardedVideoAdPlayClicked: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.ClickCallbackKey, map)
          }

          override fun onReward(adInfo: ATAdInfo) {
            MsgTools.printMsg("onReward: $placementId")
            val map = Arguments.createMap()
            map.putString(Const.CallbackKey.PlacementId, placementId)
            map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
            sendEvent(Const.RewardVideoCallback.RewardCallbackKey, map)
          }
        })
      }
    }
  }

  fun loadRewardedVideo(placementId: String, settingsJson: String) {
    MsgTools.printMsg("loadRewardedVideo: $placementId, settings: $settingsJson")
    runOnUiThread {
      ensureRewardVideoAd(placementId)
      val ad = rewardVideoAd ?: return@runOnUiThread

      if (settingsJson.isNotEmpty()) {
        try {
          val jsonObject = JSONObject(settingsJson)
          val userId = jsonObject.optString(Const.USER_ID, "")
          val userData = jsonObject.optString(Const.USER_DATA, "")
          val localExtra = HashMap<String, Any?>()
          localExtra[ATAdConst.KEY.USER_ID] = userId
          localExtra[ATAdConst.KEY.USER_CUSTOM_DATA] = userData
          fillMapFromJsonObject(localExtra, jsonObject)
          ad.setLocalExtra(localExtra)
        } catch (e: JSONException) {
          e.printStackTrace()
        }
      }

      ad.load()
    }
  }

  fun showVideo(scenario: String) {
    MsgTools.printMsg("showRewardedVideo: $placementId, scenario: $scenario")
    runOnUiThread {
      val ad = rewardVideoAd
      if (ad != null) {
        isReady = false
        ad.show(currentActivity(), scenario)
      } else {
        MsgTools.printMsg("showVideo error, you must call loadRewardVideo first $placementId")
        val map = Arguments.createMap()
        map.putString(Const.CallbackKey.PlacementId, placementId)
        map.putString(Const.CallbackKey.ErrorMsg, "you must call loadRewardVideo first")
        sendEvent(Const.RewardVideoCallback.LoadFailCallbackKey, map)
      }
    }
  }

  fun isAdReady(): Boolean {
    MsgTools.printMsg("rewarded isAdReady: $placementId")
    return try {
      rewardVideoAd?.isAdReady ?: run {
        MsgTools.printMsg("rewarded isAdReady error, you must call loadRewardedVideo first $placementId")
        isReady
      }
    } catch (t: Throwable) {
      MsgTools.printMsg("rewarded isAdReady Throwable: ${t.message}")
      isReady
    }
  }

  fun checkAdStatus(): String {
    MsgTools.printMsg("rewarded checkAdStatus: $placementId")
    return try {
      val statusInfo: ATAdStatusInfo? = rewardVideoAd?.checkAdStatus()
      if (statusInfo != null) {
        val jsonObject = JSONObject()
        jsonObject.put("isLoading", statusInfo.isLoading)
        jsonObject.put("isReady", statusInfo.isReady)
        jsonObject.put("adInfo", statusInfo.getATTopAdInfo())
        val result = jsonObject.toString()
        MsgTools.printMsg("rewarded checkAdStatus: $placementId, $result")
        result
      } else {
        MsgTools.printMsg("rewarded checkAdStatus error, you must call loadRewardedVideo first $placementId")
        ""
      }
    } catch (t: Throwable) {
      MsgTools.printMsg("rewarded checkAdStatus Throwable: ${t.message}")
      ""
    }
  }
}
