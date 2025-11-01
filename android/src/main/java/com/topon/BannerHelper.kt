package com.topon

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.FrameLayout
import com.anythink.banner.api.ATBannerListener
import com.anythink.banner.api.ATBannerView
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATAdStatusInfo
import com.anythink.core.api.ATSDK
import com.anythink.core.api.AdError
import com.facebook.react.bridge.Arguments
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class BannerHelper(module: ToponModule) : BaseHelper(module) {

  private var placementId: String = ""
  private var isReady: Boolean = false
  private var bannerView: ATBannerView? = null

  fun initBanner(targetPlacementId: String) {
    placementId = targetPlacementId
    MsgTools.printMsg("initBanner: $placementId")
    val context = currentActivity() ?: reactContext()
    bannerView = ATBannerView(context).apply {
      placementId = targetPlacementId
      setBannerAdListener(object : ATBannerListener {
        override fun onBannerLoaded() {
          isReady = true
          MsgTools.printMsg("onBannerLoaded: $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          sendEvent(Const.BannerCallback.LoadedCallbackKey, map)
        }

        override fun onBannerFailed(adError: AdError) {
          isReady = false
          MsgTools.printMsg("onBannerFailed: $placementId, ${adError.fullErrorInfo}")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
          sendEvent(Const.BannerCallback.LoadFailCallbackKey, map)
        }

        override fun onBannerClicked(adInfo: ATAdInfo) {
          MsgTools.printMsg("onBannerClicked: $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
          sendEvent(Const.BannerCallback.ClickCallbackKey, map)
        }

        override fun onBannerShow(adInfo: ATAdInfo) {
          isReady = false
          MsgTools.printMsg("onBannerShow: $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
          sendEvent(Const.BannerCallback.ShowCallbackKey, map)
        }

        override fun onBannerClose(adInfo: ATAdInfo) {
          isReady = false
          MsgTools.printMsg("onBannerClose: $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
          sendEvent(Const.BannerCallback.CloseCallbackKey, map)
        }

        override fun onBannerAutoRefreshed(adInfo: ATAdInfo) {
          MsgTools.printMsg("onBannerAutoRefreshed: $placementId")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.AdInfo, adInfo.toString())
          sendEvent(Const.BannerCallback.RefreshCallbackKey, map)
        }

        override fun onBannerAutoRefreshFail(adError: AdError) {
          isReady = false
          MsgTools.printMsg("onBannerAutoRefreshFail: $placementId, ${adError.fullErrorInfo}")
          val map = Arguments.createMap()
          map.putString(Const.CallbackKey.PlacementId, placementId)
          map.putString(Const.CallbackKey.ErrorMsg, adError.fullErrorInfo)
          sendEvent(Const.BannerCallback.RefreshFailCallbackKey, map)
        }
      })
    }
  }

  fun loadBanner(placementId: String, settingsJson: String) {
    MsgTools.printMsg("loadBanner: $placementId, settings: $settingsJson")
    runOnUiThread {
      if (bannerView == null || this.placementId != placementId) {
        initBanner(placementId)
      }

      val banner = bannerView ?: return@runOnUiThread

      if (!TextUtils.isEmpty(settingsJson)) {
        try {
          val jsonObject = JSONObject(settingsJson)
          var width = 0
          var height = 0
          if (jsonObject.has(Const.WIDTH)) {
            width = jsonObject.optInt(Const.WIDTH)
          }
          if (jsonObject.has(Const.HEIGHT)) {
            height = jsonObject.optInt(Const.HEIGHT)
          }
          val params = banner.layoutParams
          if (params == null) {
            banner.layoutParams = FrameLayout.LayoutParams(width, height)
          } else {
            params.width = width
            params.height = height
            banner.layoutParams = params
          }

          var adaptiveOrientation = 0
          var adaptiveWidth = 0
          if (jsonObject.has(Const.Banner.AdaptiveOrientation)) {
            adaptiveOrientation = jsonObject.optInt(Const.Banner.AdaptiveOrientation)
          }
          if (jsonObject.has(Const.Banner.AdaptiveWidth)) {
            adaptiveWidth = jsonObject.optInt(Const.Banner.AdaptiveWidth)
          }
          if (!jsonObject.has(Const.Banner.AdaptiveType)) {
            jsonObject.put(Const.Banner.AdaptiveType, 0)
          }
          jsonObject.put(Const.Banner.InlineAdaptiveOrientation, adaptiveOrientation)
          jsonObject.put(Const.Banner.InlineAdaptiveWidth, adaptiveWidth)

          val localExtra = HashMap<String, Any?>()
          fillMapFromJsonObject(localExtra, jsonObject)

          if (ATSDK.isNetworkLogDebug()) {
            MsgTools.printMsg("Banner localExtra: ${jsonObject.toString()}")
          }

          banner.setLocalExtra(localExtra)
        } catch (e: JSONException) {
          e.printStackTrace()
        }
      }

      banner.loadAd()
    }
  }

  fun showBannerWithRect(rectJson: String, scenario: String) {
    MsgTools.printMsg("showBannerWithRect: $placementId, rect: $rectJson, scenario: $scenario")
    if (rectJson.isEmpty()) {
      MsgTools.printMsg("showBannerWithRect error without rect, placementId: $placementId")
      return
    }
    try {
      val jsonObject = JSONObject(rectJson)
      val x = jsonObject.optInt(Const.X, 0)
      val y = jsonObject.optInt(Const.Y, 0)
      val width = jsonObject.optInt(Const.WIDTH, 0)
      val height = jsonObject.optInt(Const.HEIGHT, 0)

      runOnUiThread {
        val banner = bannerView
        val activity = currentActivity()
        if (banner != null && activity != null) {
          val layoutParams = FrameLayout.LayoutParams(width, height)
          layoutParams.leftMargin = x
          layoutParams.topMargin = y
          val parent: ViewParent? = banner.parent
          if (parent is ViewGroup) {
            parent.removeView(banner)
          }
          if (scenario.isNotEmpty()) {
            banner.setScenario(scenario)
          }
          activity.addContentView(banner, layoutParams)
        } else {
          MsgTools.printMsg("showBannerWithRect error, you must call loadBanner first, placementId: $placementId")
        }
      }
    } catch (e: JSONException) {
      MsgTools.printMsg("showBannerWithRect error: ${e.message}")
    }
  }

  fun showBannerWithPosition(position: String, scenario: String) {
    MsgTools.printMsg("showBannerWithPosition: $placementId, position: $position, scenario: $scenario")
    runOnUiThread {
      val banner = bannerView
      val activity = currentActivity()
      if (banner != null && activity != null) {
        val width = banner.layoutParams?.width ?: 0
        val height = banner.layoutParams?.height ?: 0
        val layoutParams = FrameLayout.LayoutParams(width, height)
        layoutParams.gravity = if (Const.BANNER_POSITION_TOP == position) {
          Gravity.CENTER_HORIZONTAL or Gravity.TOP
        } else {
          Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        }
        val parent: ViewParent? = banner.parent
        if (parent is ViewGroup) {
          parent.removeView(banner)
        }
        if (scenario.isNotEmpty()) {
          banner.setScenario(scenario)
        }
        activity.addContentView(banner, layoutParams)
      } else {
        MsgTools.printMsg("showBannerWithPosition error, you must call loadBanner first, placementId: $placementId")
      }
    }
  }

  fun reshowBanner() {
    MsgTools.printMsg("reshowBanner: $placementId")
    runOnUiThread {
      bannerView?.visibility = View.VISIBLE
    }
  }

  fun hideBanner() {
    MsgTools.printMsg("hideBanner: $placementId")
    runOnUiThread {
      bannerView?.visibility = View.GONE
    }
  }

  fun removeBanner() {
    MsgTools.printMsg("removeBanner: $placementId")
    runOnUiThread {
      val banner = bannerView
      val parent = banner?.parent
      if (banner != null && parent is ViewGroup) {
        parent.removeView(banner)
      } else {
        MsgTools.printMsg("removeBanner no banner need to be removed, placementId: $placementId")
      }
    }
  }

  fun isAdReady(): Boolean {
    MsgTools.printMsg("banner isAdReady: $placementId：$isReady")
    return isReady
  }

  fun checkAdStatus(): String {
    MsgTools.printMsg("banner checkAdStatus: $placementId")
    return try {
      val statusInfo: ATAdStatusInfo? = bannerView?.checkAdStatus()
      if (statusInfo != null) {
        val jsonObject = JSONObject()
        jsonObject.put("isLoading", statusInfo.isLoading)
        jsonObject.put("isReady", statusInfo.isReady)
        jsonObject.put("adInfo", statusInfo.getATTopAdInfo())
        val result = jsonObject.toString()
        MsgTools.printMsg("banner checkAdStatus: $placementId, $result")
        result
      } else {
        MsgTools.printMsg("banner checkAdStatus error, you must call loadBanner first $placementId")
        ""
      }
    } catch (t: Throwable) {
      MsgTools.printMsg("banner checkAdStatus Throwable: ${t.message}")
      ""
    }
  }
}
