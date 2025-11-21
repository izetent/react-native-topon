package com.topon

import android.app.Activity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.UiThreadUtil
import com.facebook.react.bridge.WritableMap
import org.json.JSONObject

open class BaseHelper(protected val module: ToponModule) {

  protected fun sendEvent(eventName: String, data: WritableMap) {
    module.sendEvent(eventName, data)
  }

  protected fun currentActivity(): Activity? = module.currentActivitySafe()

  protected fun reactContext(): ReactApplicationContext = module.reactContext()

  protected fun runOnUiThread(action: () -> Unit) {
    if (UiThreadUtil.isOnUiThread()) {
      action()
    } else {
      UiThreadUtil.runOnUiThread(action)
    }
  }

  protected fun fillMapFromJsonObject(
    localExtra: MutableMap<String, Any?>,
    jsonObject: JSONObject
  ) {
    val keys = jsonObject.keys()
    while (keys.hasNext()) {
      val key = keys.next()
      localExtra[key] = jsonObject.opt(key)
    }
  }
}
