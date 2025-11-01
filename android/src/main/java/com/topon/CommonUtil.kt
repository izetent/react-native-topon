package com.topon

import android.content.Context
import org.json.JSONObject

object CommonUtil {
  fun dip2px(context: Context, dipValue: Float): Int {
    val scale = context.resources.displayMetrics.density
    return (dipValue * scale + 0.5f).toInt()
  }

  fun jsonStringToMap(jsonString: String?): Map<String, Any?> {
    if (jsonString.isNullOrEmpty()) {
      return emptyMap()
    }
    return try {
      val jsonObject = JSONObject(jsonString)
      val iterator = jsonObject.keys()
      val map = mutableMapOf<String, Any?>()
      while (iterator.hasNext()) {
        val key = iterator.next()
        map[key] = jsonObject.opt(key)
      }
      map
    } catch (t: Throwable) {
      t.printStackTrace()
      emptyMap()
    }
  }
}
