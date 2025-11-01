import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  addListener(eventType: string): void;
  removeListeners(count: number): void;
  init(appId: string, appKey: string): void;
  getSDKVersionName(): Promise<string>;
  isCnSDK(): Promise<boolean>;
  setExcludeMyOfferPkgList(packages: string[]): void;
  initCustomMap(customMapJson: string): void;
  setPlacementCustomMap(placementId: string, customMapJson: string): void;
  setGDPRLevel(level: number): void;
  getGDPRLevel(): Promise<number>;
  getUserLocation(): Promise<number>;
  showGDPRAuth(): void;
  setLogDebug(isDebug: boolean): void;
  deniedUploadDeviceInfo(keys: string[]): void;

  rewardedLoadAd(placementId: string, settingsJson: string): void;
  rewardedShowAd(placementId: string): void;
  rewardedShowAdInScenario(placementId: string, scenario: string): void;
  rewardedHasAdReady(placementId: string): Promise<boolean>;
  rewardedCheckAdStatus(placementId: string): Promise<string>;

  interstitialLoadAd(placementId: string, settingsJson: string): void;
  interstitialShowAd(placementId: string): void;
  interstitialShowAdInScenario(placementId: string, scenario: string): void;
  interstitialHasAdReady(placementId: string): Promise<boolean>;
  interstitialCheckAdStatus(placementId: string): Promise<string>;

  bannerLoadAd(placementId: string, settingsJson: string): void;
  bannerShowAdInRectangle(placementId: string, rectJson: string): void;
  bannerShowAdInPosition(placementId: string, position: string): void;
  bannerShowAdInRectangleAndScenario(
    placementId: string,
    rectJson: string,
    scenario: string
  ): void;
  bannerShowAdInPositionAndScenario(
    placementId: string,
    position: string,
    scenario: string
  ): void;
  bannerHideAd(placementId: string): void;
  bannerReShowAd(placementId: string): void;
  bannerRemoveAd(placementId: string): void;
  bannerHasAdReady(placementId: string): Promise<boolean>;
  bannerCheckAdStatus(placementId: string): Promise<string>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Topon');
