import NativeTopon from './NativeTopon';

export type PlacementId = string;

export type JsonSerializable =
  | string
  | number
  | boolean
  | null
  | undefined
  | JsonSerializable[]
  | { [key: string]: JsonSerializable };

export type BannerPosition = 'top' | 'bottom';

export interface BannerRectangle {
  x?: number;
  y?: number;
  width?: number;
  height?: number;
  [key: string]: JsonSerializable;
}

export interface RewardedVideoSettings {
  userID?: string;
  media_ext?: string;
  [key: string]: JsonSerializable;
}

export interface InterstitialSettings {
  UseRewardedVideoAsInterstitial?: boolean;
  [key: string]: JsonSerializable;
}

export interface BannerSettings extends BannerRectangle {
  adaptive_type?: number;
  adaptive_orientation?: number;
  adaptive_width?: number;
  [key: string]: JsonSerializable;
}

export interface ToponAdStatus {
  isLoading: boolean;
  isReady: boolean;
  adInfo?: string;
}

export interface ToponErrorPayload {
  placementId: string;
  errorMsg: string;
}

export interface ToponAdInfoPayload {
  placementId: string;
  adCallbackInfo?: string;
}

export type ToponRewardedEventPayload =
  | ToponAdInfoPayload
  | ToponErrorPayload
  | { placementId: string };

export type ToponInterstitialEventPayload = ToponRewardedEventPayload;
export type ToponBannerEventPayload = ToponRewardedEventPayload;

const ensureJsonString = (input: JsonSerializable): string => {
  if (typeof input === 'string') {
    return input;
  }

  if (input == null) {
    return '';
  }

  try {
    return JSON.stringify(input);
  } catch (error) {
    console.warn(
      '[react-native-topon]',
      'Failed to serialize value to JSON:',
      error
    );
    return '';
  }
};

export const SDK = {
  init: (appId: string, appKey: string) => NativeTopon.init(appId, appKey),
  getSDKVersionName: () => NativeTopon.getSDKVersionName(),
  isCnSDK: () => NativeTopon.isCnSDK(),
  setExcludeMyOfferPkgList: (packages: string[]) =>
    NativeTopon.setExcludeMyOfferPkgList(packages),
  initCustomMap: (customMap: JsonSerializable) =>
    NativeTopon.initCustomMap(ensureJsonString(customMap)),
  setPlacementCustomMap: (
    placementId: PlacementId,
    customMap: JsonSerializable
  ) =>
    NativeTopon.setPlacementCustomMap(placementId, ensureJsonString(customMap)),
  setGDPRLevel: (level: number) => NativeTopon.setGDPRLevel(level),
  getGDPRLevel: () => NativeTopon.getGDPRLevel(),
  getUserLocation: () => NativeTopon.getUserLocation(),
  showGDPRAuth: () => NativeTopon.showGDPRAuth(),
  setLogDebug: (isDebug: boolean) => NativeTopon.setLogDebug(isDebug),
  deniedUploadDeviceInfo: (keys: string[]) =>
    NativeTopon.deniedUploadDeviceInfo(keys),
};

export const RewardedVideo = {
  loadAd: (placementId: PlacementId, settings?: RewardedVideoSettings) =>
    NativeTopon.rewardedLoadAd(placementId, ensureJsonString(settings ?? {})),
  showAd: (placementId: PlacementId) => NativeTopon.rewardedShowAd(placementId),
  showAdInScenario: (placementId: PlacementId, scenario: string) =>
    NativeTopon.rewardedShowAdInScenario(placementId, scenario),
  hasAdReady: (placementId: PlacementId) =>
    NativeTopon.rewardedHasAdReady(placementId),
  checkAdStatus: async (
    placementId: PlacementId
  ): Promise<ToponAdStatus | null> => {
    const result = await NativeTopon.rewardedCheckAdStatus(placementId);
    try {
      return JSON.parse(result) as ToponAdStatus;
    } catch {
      return null;
    }
  },
};

export const Interstitial = {
  loadAd: (placementId: PlacementId, settings?: InterstitialSettings) =>
    NativeTopon.interstitialLoadAd(
      placementId,
      ensureJsonString(settings ?? {})
    ),
  showAd: (placementId: PlacementId) =>
    NativeTopon.interstitialShowAd(placementId),
  showAdInScenario: (placementId: PlacementId, scenario: string) =>
    NativeTopon.interstitialShowAdInScenario(placementId, scenario),
  hasAdReady: (placementId: PlacementId) =>
    NativeTopon.interstitialHasAdReady(placementId),
  checkAdStatus: async (
    placementId: PlacementId
  ): Promise<ToponAdStatus | null> => {
    const result = await NativeTopon.interstitialCheckAdStatus(placementId);
    try {
      return JSON.parse(result) as ToponAdStatus;
    } catch {
      return null;
    }
  },
};

export const Banner = {
  loadAd: (placementId: PlacementId, settings?: BannerSettings) =>
    NativeTopon.bannerLoadAd(placementId, ensureJsonString(settings ?? {})),
  showAdInRectangle: (placementId: PlacementId, rect: BannerRectangle) =>
    NativeTopon.bannerShowAdInRectangle(placementId, ensureJsonString(rect)),
  showAdInPosition: (placementId: PlacementId, position: BannerPosition) =>
    NativeTopon.bannerShowAdInPosition(placementId, position),
  showAdInRectangleAndScenario: (
    placementId: PlacementId,
    rect: BannerRectangle,
    scenario: string
  ) =>
    NativeTopon.bannerShowAdInRectangleAndScenario(
      placementId,
      ensureJsonString(rect),
      scenario
    ),
  showAdInPositionAndScenario: (
    placementId: PlacementId,
    position: BannerPosition,
    scenario: string
  ) =>
    NativeTopon.bannerShowAdInPositionAndScenario(
      placementId,
      position,
      scenario
    ),
  hideAd: (placementId: PlacementId) => NativeTopon.bannerHideAd(placementId),
  reShowAd: (placementId: PlacementId) =>
    NativeTopon.bannerReShowAd(placementId),
  removeAd: (placementId: PlacementId) =>
    NativeTopon.bannerRemoveAd(placementId),
  hasAdReady: (placementId: PlacementId) =>
    NativeTopon.bannerHasAdReady(placementId),
  checkAdStatus: async (
    placementId: PlacementId
  ): Promise<ToponAdStatus | null> => {
    const result = await NativeTopon.bannerCheckAdStatus(placementId);
    try {
      return JSON.parse(result) as ToponAdStatus;
    } catch {
      return null;
    }
  },
};

export const ToponEvents = {
  RewardedVideo: {
    Loaded: 'ATRewardedVideoLoaded',
    LoadFail: 'ATRewardedVideoLoadFail',
    PlayStart: 'ATRewardedVideoPlayStart',
    PlayEnd: 'ATRewardedVideoPlayEnd',
    PlayFail: 'ATRewardedVideoPlayFail',
    Close: 'ATRewardedVideoClose',
    Click: 'ATRewardedVideoClick',
    Reward: 'ATRewardedVideoReward',
  } as const,
  Interstitial: {
    Loaded: 'ATInterstitialLoaded',
    LoadFail: 'ATInterstitialLoadFail',
    PlayStart: 'ATInterstitialPlayStart',
    PlayEnd: 'ATInterstitialPlayEnd',
    PlayFail: 'ATInterstitialPlayFail',
    Close: 'ATInterstitialClose',
    Click: 'ATInterstitialClick',
    Show: 'ATInterstitialAdShow',
  } as const,
  Banner: {
    Loaded: 'ATBannerLoaded',
    LoadFail: 'ATBannerLoadFail',
    Close: 'ATBannerCloseButtonTapped',
    Click: 'ATBannerClick',
    Show: 'ATBannerShow',
    Refresh: 'ATBannerRefresh',
    RefreshFail: 'ATBannerRefreshFail',
  } as const,
};

export type RewardedEventName =
  (typeof ToponEvents.RewardedVideo)[keyof typeof ToponEvents.RewardedVideo];
export type InterstitialEventName =
  (typeof ToponEvents.Interstitial)[keyof typeof ToponEvents.Interstitial];
export type BannerEventName =
  (typeof ToponEvents.Banner)[keyof typeof ToponEvents.Banner];

export const NativeModule = NativeTopon;

export default {
  SDK,
  RewardedVideo,
  Interstitial,
  Banner,
  Events: ToponEvents,
  NativeModule,
};
