package com.topon

object Const {
  const val DEBUG = true
  const val SCENARIO = "Scenario"
  const val USER_ID = "userID"
  const val USER_DATA = "media_ext"

  const val BANNER_POSITION_TOP = "top"
  const val BANNER_POSITION_BOTTOM = "bottom"
  const val BANNER_AD_SIZE = "banner_ad_size"

  const val X = "x"
  const val Y = "y"
  const val WIDTH = "width"
  const val HEIGHT = "height"
  const val BACKGROUND_COLOR = "backgroundColor"
  const val TEXT_COLOR = "textColor"
  const val TEXT_SIZE = "textSize"

  const val parent = "parent"
  const val icon = "icon"
  const val mainImage = "mainImage"
  const val title = "title"
  const val desc = "desc"
  const val adLogo = "adLogo"
  const val cta = "cta"

  object Interstitial {
    const val UseRewardedVideoAsInterstitial = "UseRewardedVideoAsInterstitial"
  }

  object Banner {
    const val AdaptiveType = "adaptive_type"
    const val AdaptiveOrientation = "adaptive_orientation"
    const val AdaptiveWidth = "adaptive_width"
    const val InlineAdaptiveOrientation = "inline_adaptive_orientation"
    const val InlineAdaptiveWidth = "inline_adaptive_width"
  }

  object RewardVideoCallback {
    const val LoadedCallbackKey = "ATRewardedVideoLoaded"
    const val LoadFailCallbackKey = "ATRewardedVideoLoadFail"
    const val PlayStartCallbackKey = "ATRewardedVideoPlayStart"
    const val PlayEndCallbackKey = "ATRewardedVideoPlayEnd"
    const val PlayFailCallbackKey = "ATRewardedVideoPlayFail"
    const val CloseCallbackKey = "ATRewardedVideoClose"
    const val ClickCallbackKey = "ATRewardedVideoClick"
    const val RewardCallbackKey = "ATRewardedVideoReward"
  }

  object InterstitialCallback {
    const val LoadedCallbackKey = "ATInterstitialLoaded"
    const val LoadFailCallbackKey = "ATInterstitialLoadFail"
    const val PlayStartCallbackKey = "ATInterstitialPlayStart"
    const val PlayEndCallbackKey = "ATInterstitialPlayEnd"
    const val PlayFailCallbackKey = "ATInterstitialPlayFail"
    const val CloseCallbackKey = "ATInterstitialClose"
    const val ClickCallbackKey = "ATInterstitialClick"
    const val ShowCallbackKey = "ATInterstitialAdShow"
  }

  object BannerCallback {
    const val LoadedCallbackKey = "ATBannerLoaded"
    const val LoadFailCallbackKey = "ATBannerLoadFail"
    const val CloseCallbackKey = "ATBannerCloseButtonTapped"
    const val ClickCallbackKey = "ATBannerClick"
    const val ShowCallbackKey = "ATBannerShow"
    const val RefreshCallbackKey = "ATBannerRefresh"
    const val RefreshFailCallbackKey = "ATBannerRefreshFail"
  }

  object NativeCallback {
    const val LoadedCallbackKey = "NativeLoaded"
    const val LoadFailCallbackKey = "NativeLoadFail"
    const val CloseCallbackKey = "NativeCloseButtonTapped"
    const val ClickCallbackKey = "NativeClick"
    const val ShowCallbackKey = "NativeShow"
    const val VideoStartKey = "NativeVideoStart"
    const val VideoEndKey = "NativeVideoEnd"
    const val VideoProgressKey = "NativeVideoProgress"
  }

  object CallbackKey {
    const val PlacementId = "placementId"
    const val ErrorMsg = "errorMsg"
    const val AdInfo = "adCallbackInfo"
  }
}
