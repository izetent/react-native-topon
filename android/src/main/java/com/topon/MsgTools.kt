package com.topon

import android.util.Log

object MsgTools {
  private const val TAG = "ATReactNativeBridge"

  @Volatile
  private var debug: Boolean = true

  fun printMsg(message: String) {
    if (debug) {
      Log.d(TAG, message)
    }
  }

  fun setLogDebug(isDebug: Boolean) {
    debug = isDebug
  }
}
