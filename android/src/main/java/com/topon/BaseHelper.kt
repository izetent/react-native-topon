package com.topon

import android.app.Activity
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import org.json.JSONObject

open class BaseHelper(protected val module: ToponModule) {

  protected fun sendEvent(eventName: String, data: WritableMap) {
    module.sendEvent(eventName, data)
  }

  protected fun currentActivity(): Activity? = module.currentActivitySafe()

  protected fun reactContext(): ReactApplicationContext = module.reactContext()

  protected fun runOnUiThread(action: () -> Unit) {
    val activity = currentActivity()
    if (activity != null) {
      activity.runOnUiThread(action)
    } else {
      MsgTools.printMsg("runOnUiThread: current activity is null.")
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
