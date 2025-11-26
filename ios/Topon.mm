#import "Topon.h"

#import <UIKit/UIKit.h>
#import <AnyThinkBanner/AnyThinkBanner.h>
#import <AnyThinkInterstitial/AnyThinkInterstitial.h>
#import <AnyThinkRewardedVideo/AnyThinkRewardedVideo.h>
#import <AnyThinkSDK/AnyThinkSDK.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <objc/message.h>

#import "NSDictionary+String.h"
#import "NSJSONSerialization+String.h"

static NSString *const kToponRewardedLoaded = @"ATRewardedVideoLoaded";
static NSString *const kToponRewardedLoadFail = @"ATRewardedVideoLoadFail";
static NSString *const kToponRewardedPlayStart = @"ATRewardedVideoPlayStart";
static NSString *const kToponRewardedPlayEnd = @"ATRewardedVideoPlayEnd";
static NSString *const kToponRewardedPlayFail = @"ATRewardedVideoPlayFail";
static NSString *const kToponRewardedClose = @"ATRewardedVideoClose";
static NSString *const kToponRewardedClick = @"ATRewardedVideoClick";
static NSString *const kToponRewardedReward = @"ATRewardedVideoReward";

static NSString *const kToponInterstitialLoaded = @"ATInterstitialLoaded";
static NSString *const kToponInterstitialLoadFail = @"ATInterstitialLoadFail";
static NSString *const kToponInterstitialShow = @"ATInterstitialAdShow";
static NSString *const kToponInterstitialPlayStart = @"ATInterstitialPlayStart";
static NSString *const kToponInterstitialPlayEnd = @"ATInterstitialPlayEnd";
static NSString *const kToponInterstitialPlayFail = @"ATInterstitialPlayFail";
static NSString *const kToponInterstitialClose = @"ATInterstitialClose";
static NSString *const kToponInterstitialClick = @"ATInterstitialClick";

static NSString *const kToponBannerLoaded = @"ATBannerLoaded";
static NSString *const kToponBannerLoadFail = @"ATBannerLoadFail";
static NSString *const kToponBannerClose = @"ATBannerCloseButtonTapped";
static NSString *const kToponBannerClick = @"ATBannerClick";
static NSString *const kToponBannerShow = @"ATBannerShow";
static NSString *const kToponBannerRefresh = @"ATBannerRefresh";
static NSString *const kToponBannerRefreshFail = @"ATBannerRefreshFail";

static NSString *const kToponBannerPositionTop = @"top";
static NSString *const kToponBannerPositionBottom = @"bottom";
static NSString *const kToponBannerAdaptiveWidthKey = @"adaptive_width";
static NSString *const kToponBannerAdaptiveOrientationKey = @"adaptive_orientation";

static NSString *const kToponInterstitialUseRewardedAsInterstitialKey = @"UseRewardedVideoAsInterstitial";

static NSDictionary *ToponParseJSONDictionary(NSString *jsonString) {
  if (jsonString.length == 0) {
    return nil;
  }
  id value = [NSJSONSerialization topon_JSONObjectWithString:jsonString
                                                    options:NSJSONReadingAllowFragments
                                                      error:nil];
  if ([value isKindOfClass:[NSDictionary class]]) {
    return (NSDictionary *)value;
  }
  return nil;
}

static UIViewController *ToponCurrentViewController(void) {
  UIViewController *controller = RCTPresentedViewController();
  if (controller != nil) {
    return controller;
  }
  UIWindow *keyWindow = nil;
  if (@available(iOS 13.0, *)) {
    for (UIWindowScene *scene in UIApplication.sharedApplication.connectedScenes) {
      if (scene.activationState == UISceneActivationStateForegroundActive) {
        for (UIWindow *window in scene.windows) {
          if (window.isKeyWindow) {
            keyWindow = window;
            break;
          }
        }
      }
      if (keyWindow != nil) {
        break;
      }
    }
  }
  keyWindow = keyWindow ?: UIApplication.sharedApplication.keyWindow;
  return keyWindow.rootViewController;
}

static UIView *ToponContainerView(void) {
  UIViewController *controller = ToponCurrentViewController();
  return controller.view ?: UIApplication.sharedApplication.keyWindow.rootViewController.view;
}

static UIEdgeInsets ToponSafeAreaInsets(void) {
  UIView *view = ToponContainerView();
  if (@available(iOS 11.0, *)) {
    return view.safeAreaInsets;
  }
  return UIEdgeInsetsZero;
}

static CGRect ToponRectFromJSONString(NSString *rectJson) {
  CGRect rect = CGRectZero;
  NSDictionary *rectDict = ToponParseJSONDictionary(rectJson);
  if ([rectDict isKindOfClass:[NSDictionary class]]) {
    BOOL usesPixel = [rectDict[@"usesPixel"] boolValue];
    CGFloat scale = usesPixel ? UIScreen.mainScreen.nativeScale : 1.0f;
    rect = CGRectMake(
      [rectDict[@"x"] doubleValue] / scale,
      [rectDict[@"y"] doubleValue] / scale,
      [rectDict[@"width"] doubleValue] / scale,
      [rectDict[@"height"] doubleValue] / scale
    );
  }
  return rect;
}

static NSDictionary *ToponBannerExtraFromJSONString(NSString *settingsJson) {
  NSMutableDictionary *extra = [NSMutableDictionary dictionary];
  NSDictionary *settings = ToponParseJSONDictionary(settingsJson);
  if (![settings isKindOfClass:[NSDictionary class]]) {
    return extra.count > 0 ? extra : nil;
  }
  BOOL usesPixel = [settings[@"usesPixel"] boolValue];
  CGFloat scale = usesPixel ? UIScreen.mainScreen.nativeScale : 1.0f;
  NSNumber *width = settings[@"width"];
  NSNumber *height = settings[@"height"];
  if (width != nil && height != nil) {
    CGSize size = CGSizeMake(width.doubleValue / scale, height.doubleValue / scale);
    extra[kATAdLoadingExtraBannerAdSizeKey] = [NSValue valueWithCGSize:size];
  }
  NSNumber *adaptiveWidth = settings[kToponBannerAdaptiveWidthKey];
  NSNumber *adaptiveOrientation = settings[kToponBannerAdaptiveOrientationKey];
  if (adaptiveWidth != nil && adaptiveOrientation != nil) {
    // Reserved for AdMob adaptive banner; actual size calculation can be filled when needed.
  }
  if (extra[kATAdLoadingExtraBannerAdSizeKey] == nil) {
    extra[kATAdLoadingExtraBannerAdSizeKey] = [NSValue valueWithCGSize:CGSizeMake(320.0, 50.0)];
  }
  return extra;
}

static void ToponApplyBannerScenario(ATBannerView *bannerView, NSString *scenario) {
  if (scenario.length == 0 || bannerView == nil) {
    return;
  }
  SEL selector = NSSelectorFromString(@"setScenario:");
  if ([bannerView respondsToSelector:selector]) {
    void (*func)(id, SEL, NSString *) = (void (*)(id, SEL, NSString *))[bannerView methodForSelector:selector];
    func(bannerView, selector, scenario);
  }
}

static NSString *ToponJSONStringFromAdStatus(ATCheckLoadModel *model) {
  if (model == nil) {
    return @"";
  }
  NSMutableDictionary *status = [NSMutableDictionary dictionary];
  status[@"isLoading"] = @(model.isLoading);
  status[@"isReady"] = @(model.isReady);
  if (model.adOfferInfo != nil) {
    status[@"adInfo"] = model.adOfferInfo;
  }
  return [status topon_adInfoJSONString];
}

@interface Topon () <ATRewardedVideoDelegate, ATInterstitialDelegate, ATBannerDelegate>
@property(nonatomic, strong) NSMutableDictionary<NSString *, ATBannerView *> *bannerViews;
@property(nonatomic, strong) NSMutableSet<NSString *> *rewardedPlacements;
@property(nonatomic, strong) NSMutableSet<NSString *> *interstitialPlacements;
@property(nonatomic, strong) NSMutableSet<NSString *> *bannerPlacements;
@property(nonatomic, assign) NSInteger listenerCount;
@end

@implementation Topon

- (instancetype)init {
  self = [super init];
  if (self != nil) {
    _bannerViews = [NSMutableDictionary dictionary];
    _rewardedPlacements = [NSMutableSet set];
    _interstitialPlacements = [NSMutableSet set];
    _bannerPlacements = [NSMutableSet set];
  }
  return self;
}

+ (BOOL)requiresMainQueueSetup {
  return YES;
}

- (dispatch_queue_t)methodQueue {
  return dispatch_get_main_queue();
}

- (NSArray<NSString *> *)supportedEvents {
  return @[
    kToponRewardedLoaded,
    kToponRewardedLoadFail,
    kToponRewardedPlayStart,
    kToponRewardedPlayEnd,
    kToponRewardedPlayFail,
    kToponRewardedClose,
    kToponRewardedClick,
    kToponRewardedReward,
    kToponInterstitialLoaded,
    kToponInterstitialLoadFail,
    kToponInterstitialShow,
    kToponInterstitialPlayStart,
    kToponInterstitialPlayEnd,
    kToponInterstitialPlayFail,
    kToponInterstitialClose,
    kToponInterstitialClick,
    kToponBannerLoaded,
    kToponBannerLoadFail,
    kToponBannerClose,
    kToponBannerClick,
    kToponBannerShow,
    kToponBannerRefresh,
    kToponBannerRefreshFail
  ];
}

- (void)addListener:(NSString *)eventType {
  [super addListener:eventType];
  self.listenerCount += 1;
}

- (void)removeListeners:(double)count {
  [super removeListeners:count];
  self.listenerCount = MAX(0, self.listenerCount - (NSInteger)count);
}

- (void)emitEvent:(NSString *)eventName body:(NSDictionary *)body {
  if (self.listenerCount <= 0 || eventName.length == 0) {
    return;
  }
  [self sendEventWithName:eventName body:body ?: @{}];
}

- (void)emitEvent:(NSString *)eventName
      placementId:(NSString *)placementId
            extra:(NSDictionary *)extra
            error:(NSString *)errorMessage {
  NSMutableDictionary *payload = [NSMutableDictionary dictionary];
  payload[kToponCallbackPlacementIdKey] = placementId ?: @"";
  NSString *extraString = [extra topon_jsonString];
  if (extraString.length > 0) {
    payload[kToponCallbackExtraKey] = extraString;
  }
  if (errorMessage.length > 0) {
    payload[kToponCallbackErrorKey] = errorMessage;
  }
  [self emitEvent:eventName body:payload];
}

#pragma mark - SDK level APIs

- (void)init:(NSString *)appId appKey:(NSString *)appKey {
  if (![appId isKindOfClass:[NSString class]] || ![appKey isKindOfClass:[NSString class]]) {
    RCTLogError(@"[Topon] Invalid appId/appKey.");
    return;
  }
  NSError *error = nil;
  BOOL success = [[ATAPI sharedInstance] startWithAppID:appId appKey:appKey error:&error];
  if (!success && error != nil) {
    RCTLogError(@"[Topon] Failed to init SDK: %@", error);
  }
}

- (void)getSDKVersionName:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve([[ATAPI sharedInstance] version] ?: @"");
}

- (void)isCnSDK:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  BOOL isCn = NO;
  ATAPI *api = [ATAPI sharedInstance];
  SEL selector = NSSelectorFromString(@"isCnSDK");
  if ([api respondsToSelector:selector]) {
    BOOL (*func)(id, SEL) = (BOOL (*)(id, SEL))[api methodForSelector:selector];
    isCn = func(api, selector);
  } else {
    selector = NSSelectorFromString(@"isCN");
    if ([api respondsToSelector:selector]) {
      BOOL (*func)(id, SEL) = (BOOL (*)(id, SEL))[api methodForSelector:selector];
      isCn = func(api, selector);
    } else if ([[api class] respondsToSelector:selector]) {
      BOOL (*func)(id, SEL) = (BOOL (*)(id, SEL))[[api class] methodForSelector:selector];
      isCn = func([api class], selector);
    }
  }
  if (!isCn) {
    @try {
      id area = [api valueForKey:@"area"];
      if ([area isKindOfClass:[NSString class]]) {
        NSString *areaString = [(NSString *)area lowercaseString];
        isCn = [areaString containsString:@"cn"];
      }
    } @catch (__unused NSException *exception) {
    }
  }
  resolve(@(isCn));
}

- (void)setExcludeMyOfferPkgList:(NSArray *)packages {
  if (![packages isKindOfClass:[NSArray class]]) {
    return;
  }
  [[ATAPI sharedInstance] setExludeAppleIdArray:packages];
}

- (void)initCustomMap:(NSString *)customMapJson {
  NSDictionary *customData = ToponParseJSONDictionary(customMapJson);
  if ([customData isKindOfClass:[NSDictionary class]]) {
    [[ATAPI sharedInstance] setCustomData:customData];
  }
}

- (void)setPlacementCustomMap:(NSString *)placementId customMapJson:(NSString *)customMapJson {
  NSDictionary *customData = ToponParseJSONDictionary(customMapJson);
  if (placementId.length == 0 || ![customData isKindOfClass:[NSDictionary class]]) {
    return;
  }
  [[ATAPI sharedInstance] setCustomData:customData forPlacementID:placementId];
}

- (void)setGDPRLevel:(double)level {
  NSInteger gdprLevel = (NSInteger)level;
  [[ATAPI sharedInstance] setDataConsentSet:2 - gdprLevel consentString:nil];
}

- (void)getGDPRLevel:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve(@(2 - [[ATAPI sharedInstance] dataConsentSet]));
}

- (void)getUserLocation:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  [[ATAPI sharedInstance] getUserLocationWithCallback:^(ATUserLocation location) {
    NSInteger result = (location == 1) ? 1 : 2;
    resolve(@(result));
  }];
}

- (void)showGDPRAuth {
  UIViewController *controller = ToponCurrentViewController();
  if (controller == nil) {
    RCTLogWarn(@"[Topon] Unable to present GDPR auth without root view controller.");
    return;
  }
  [[ATAPI sharedInstance] presentDataConsentDialogInViewController:controller
                                        loadingFailureCallback:^(NSError *error) {
                                          RCTLogError(@"[Topon] Failed to load GDPR dialog: %@", error);
                                        }
                                               dismissalCallback:^{}];
}

- (void)setLogDebug:(BOOL)isDebug {
  [ATAPI setLogEnabled:isDebug];
}

- (void)deniedUploadDeviceInfo:(NSArray *)keys {
  if (![keys isKindOfClass:[NSArray class]]) {
    return;
  }
  [[ATAPI sharedInstance] setDeniedUploadInfoArray:keys];
}

#pragma mark - Rewarded Video

- (void)rewardedLoadAd:(NSString *)placementId settingsJson:(NSString *)settingsJson {
  if (placementId.length > 0) {
    [self.rewardedPlacements addObject:placementId];
  }
  NSMutableDictionary *extra = nil;
  NSDictionary *settings = ToponParseJSONDictionary(settingsJson);
  if ([settings isKindOfClass:[NSDictionary class]]) {
    extra = [settings mutableCopy];
    if (![extra[kATAdLoadingExtraMediaExtraKey] isKindOfClass:[NSDictionary class]]) {
      [extra removeObjectForKey:kATAdLoadingExtraMediaExtraKey];
    }
  }
  [[ATAdManager sharedManager] loadADWithPlacementID:placementId
                                               extra:extra
                                            delegate:self];
}

- (void)rewardedShowAd:(NSString *)placementId {
  [self rewardedShowAdInScenario:placementId scenario:nil];
}

- (void)rewardedShowAdInScenario:(NSString *)placementId scenario:(NSString *)scenario {
  UIViewController *controller = ToponCurrentViewController();
  if (controller == nil) {
    [self emitEvent:kToponRewardedPlayFail
        placementId:placementId
              extra:nil
              error:@"No active UIViewController to present rewarded video."];
    return;
  }
  if (scenario.length > 0) {
    [[ATAdManager sharedManager] showRewardedVideoWithPlacementID:placementId
                                                            scene:scenario
                                               inViewController:controller
                                                       delegate:self];
  } else {
    [[ATAdManager sharedManager] showRewardedVideoWithPlacementID:placementId
                                               inViewController:controller
                                                       delegate:self];
  }
}

- (void)rewardedHasAdReady:(NSString *)placementId
                   resolve:(RCTPromiseResolveBlock)resolve
                    reject:(RCTPromiseRejectBlock)reject {
  BOOL ready = [[ATAdManager sharedManager] rewardedVideoReadyForPlacementID:placementId];
  resolve(@(ready));
}

- (void)rewardedCheckAdStatus:(NSString *)placementId
                      resolve:(RCTPromiseResolveBlock)resolve
                       reject:(RCTPromiseRejectBlock)reject {
  ATCheckLoadModel *model = [[ATAdManager sharedManager] checkRewardedVideoLoadStatusForPlacementID:placementId];
  resolve(ToponJSONStringFromAdStatus(model));
}

#pragma mark ATRewardedVideoDelegate

- (void)didFailToLoadADWithPlacementID:(NSString *)placementID error:(NSError *)error {
  NSString *message = error.localizedDescription ?: @"";
  if ([self.rewardedPlacements containsObject:placementID]) {
    [self emitEvent:kToponRewardedLoadFail placementId:placementID extra:nil error:message];
  } else if ([self.interstitialPlacements containsObject:placementID]) {
    [self emitEvent:kToponInterstitialLoadFail placementId:placementID extra:nil error:message];
  } else if ([self.bannerPlacements containsObject:placementID]) {
    [self emitEvent:kToponBannerLoadFail placementId:placementID extra:nil error:message];
  }
}

- (void)didFinishLoadingADWithPlacementID:(NSString *)placementID {
  if ([self.rewardedPlacements containsObject:placementID]) {
    [self emitEvent:kToponRewardedLoaded placementId:placementID extra:nil error:nil];
  } else if ([self.interstitialPlacements containsObject:placementID]) {
    [self emitEvent:kToponInterstitialLoaded placementId:placementID extra:nil error:nil];
  } else if ([self.bannerPlacements containsObject:placementID]) {
    [self emitEvent:kToponBannerLoaded placementId:placementID extra:nil error:nil];
  }
}

- (void)rewardedVideoDidStartPlayingForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedPlayStart placementId:placementID extra:extra error:nil];
}

- (void)rewardedVideoDidEndPlayingForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedPlayEnd placementId:placementID extra:extra error:nil];
}

- (void)rewardedVideoDidFailToPlayForPlacementID:(NSString *)placementID
                                           error:(NSError *)error
                                           extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedPlayFail
      placementId:placementID
            extra:extra
            error:error.localizedDescription];
}

- (void)rewardedVideoDidCloseForPlacementID:(NSString *)placementID
                                   rewarded:(BOOL)rewarded
                                      extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedClose placementId:placementID extra:extra error:nil];
}

- (void)rewardedVideoDidClickForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedClick placementId:placementID extra:extra error:nil];
}

- (void)rewardedVideoDidRewardSuccessForPlacemenID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponRewardedReward placementId:placementID extra:extra error:nil];
}

#pragma mark - Interstitial

- (void)interstitialLoadAd:(NSString *)placementId settingsJson:(NSString *)settingsJson {
  if (placementId.length > 0) {
    [self.interstitialPlacements addObject:placementId];
  }
  NSDictionary *settings = ToponParseJSONDictionary(settingsJson);
  NSDictionary *extra = nil;
  NSNumber *useRewarded = settings[kToponInterstitialUseRewardedAsInterstitialKey];
  if ([useRewarded isKindOfClass:[NSNumber class]]) {
    extra = @{kATInterstitialExtraUsesRewardedVideo: @([useRewarded boolValue])};
  }
  [[ATAdManager sharedManager] loadADWithPlacementID:placementId
                                               extra:extra
                                            delegate:self];
}

- (void)interstitialShowAd:(NSString *)placementId {
  [self interstitialShowAdInScenario:placementId scenario:nil];
}

- (void)interstitialShowAdInScenario:(NSString *)placementId scenario:(NSString *)scenario {
  UIViewController *controller = ToponCurrentViewController();
  if (controller == nil) {
    [self emitEvent:kToponInterstitialPlayFail
        placementId:placementId
              extra:nil
              error:@"No active UIViewController to present interstitial."];
    return;
  }
  if (scenario.length > 0) {
    [[ATAdManager sharedManager] showInterstitialWithPlacementID:placementId
                                                           scene:scenario
                                              inViewController:controller
                                                      delegate:self];
  } else {
    [[ATAdManager sharedManager] showInterstitialWithPlacementID:placementId
                                              inViewController:controller
                                                      delegate:self];
  }
}

- (void)interstitialHasAdReady:(NSString *)placementId
                       resolve:(RCTPromiseResolveBlock)resolve
                        reject:(RCTPromiseRejectBlock)reject {
  BOOL ready = [[ATAdManager sharedManager] interstitialReadyForPlacementID:placementId];
  resolve(@(ready));
}

- (void)interstitialCheckAdStatus:(NSString *)placementId
                          resolve:(RCTPromiseResolveBlock)resolve
                           reject:(RCTPromiseRejectBlock)reject {
  ATCheckLoadModel *model = [[ATAdManager sharedManager] checkInterstitialLoadStatusForPlacementID:placementId];
  resolve(ToponJSONStringFromAdStatus(model));
}

#pragma mark ATInterstitialDelegate

- (void)interstitialDidShowForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialShow placementId:placementID extra:extra error:nil];
}

- (void)interstitialDidClickForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialClick placementId:placementID extra:extra error:nil];
}

- (void)interstitialDidCloseForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialClose placementId:placementID extra:extra error:nil];
}

- (void)interstitialDidStartPlayingVideoForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialPlayStart placementId:placementID extra:extra error:nil];
}

- (void)interstitialDidEndPlayingVideoForPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialPlayEnd placementId:placementID extra:extra error:nil];
}

- (void)interstitialDidFailToPlayVideoForPlacementID:(NSString *)placementID
                                               error:(NSError *)error
                                               extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialPlayFail placementId:placementID extra:extra error:error.localizedDescription];
}

- (void)interstitialFailedToShowForPlacementID:(NSString *)placementID
                                         error:(NSError *)error
                                         extra:(NSDictionary *)extra {
  [self emitEvent:kToponInterstitialPlayFail placementId:placementID extra:extra error:error.localizedDescription];
}

#pragma mark - Banner

- (void)bannerLoadAd:(NSString *)placementId settingsJson:(NSString *)settingsJson {
  if (placementId.length > 0) {
    [self.bannerPlacements addObject:placementId];
  }
  NSDictionary *extra = ToponBannerExtraFromJSONString(settingsJson);
  [[ATAdManager sharedManager] loadADWithPlacementID:placementId
                                               extra:extra
                                            delegate:self];
}

- (void)bannerShowAdInRectangle:(NSString *)placementId rectJson:(NSString *)rectJson {
  [self bannerShowAdInRectangleAndScenario:placementId rectJson:rectJson scenario:nil];
}

- (void)bannerShowAdInPosition:(NSString *)placementId position:(NSString *)position {
  [self bannerShowAdInPositionAndScenario:placementId position:position scenario:nil];
}

- (void)bannerShowAdInRectangleAndScenario:(NSString *)placementId
                                  rectJson:(NSString *)rectJson
                                  scenario:(NSString *)scenario {
  dispatch_async(dispatch_get_main_queue(), ^{
    ATBannerView *bannerView = [[ATAdManager sharedManager] retrieveBannerViewForPlacementID:placementId];
    if (bannerView == nil) {
      RCTLogWarn(@"[Topon] Banner view is nil for placement %@", placementId);
      return;
    }
    UIView *container = ToponContainerView();
    if (container == nil) {
      RCTLogWarn(@"[Topon] Unable to find a container view for banner placement %@", placementId);
      return;
    }
    ToponApplyBannerScenario(bannerView, scenario);
    bannerView.delegate = self;
    bannerView.frame = ToponRectFromJSONString(rectJson);
    [self.bannerViews[placementId] removeFromSuperview];
    self.bannerViews[placementId] = bannerView;
    [container addSubview:bannerView];
  });
}

- (void)bannerShowAdInPositionAndScenario:(NSString *)placementId
                                 position:(NSString *)position
                                 scenario:(NSString *)scenario {
  dispatch_async(dispatch_get_main_queue(), ^{
    ATBannerView *bannerView = [[ATAdManager sharedManager] retrieveBannerViewForPlacementID:placementId];
    if (bannerView == nil) {
      RCTLogWarn(@"[Topon] Banner view is nil for placement %@", placementId);
      return;
    }
    UIView *container = ToponContainerView();
    if (container == nil) {
      RCTLogWarn(@"[Topon] Unable to find a container view for banner placement %@", placementId);
      return;
    }
    ToponApplyBannerScenario(bannerView, scenario);
    bannerView.delegate = self;
    CGSize viewSize = bannerView.bounds.size;
    CGRect bounds = container.bounds;
    CGFloat x = (CGRectGetWidth(bounds) - viewSize.width) / 2.0;
    UIEdgeInsets safeInsets = ToponSafeAreaInsets();
    CGFloat y = safeInsets.bottom;
    if ([position isEqualToString:kToponBannerPositionTop]) {
      y = safeInsets.top;
    } else if ([position isEqualToString:kToponBannerPositionBottom]) {
      y = CGRectGetHeight(bounds) - safeInsets.bottom - viewSize.height;
    } else {
      y = CGRectGetHeight(bounds) - safeInsets.bottom - viewSize.height;
    }
    bannerView.frame = CGRectMake(x, y, viewSize.width, viewSize.height);
    [self.bannerViews[placementId] removeFromSuperview];
    self.bannerViews[placementId] = bannerView;
    [container addSubview:bannerView];
  });
}

- (void)bannerHideAd:(NSString *)placementId {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.bannerViews[placementId].hidden = YES;
  });
}

- (void)bannerReShowAd:(NSString *)placementId {
  dispatch_async(dispatch_get_main_queue(), ^{
    self.bannerViews[placementId].hidden = NO;
  });
}

- (void)bannerRemoveAd:(NSString *)placementId {
  dispatch_async(dispatch_get_main_queue(), ^{
    [self.bannerViews[placementId] removeFromSuperview];
    [self.bannerViews removeObjectForKey:placementId];
  });
}

- (void)bannerHasAdReady:(NSString *)placementId
                 resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject {
  BOOL ready = [[ATAdManager sharedManager] bannerAdReadyForPlacementID:placementId];
  resolve(@(ready));
}

- (void)bannerCheckAdStatus:(NSString *)placementId
                    resolve:(RCTPromiseResolveBlock)resolve
                     reject:(RCTPromiseRejectBlock)reject {
  ATCheckLoadModel *model = [[ATAdManager sharedManager] checkBannerLoadStatusForPlacementID:placementId];
  resolve(ToponJSONStringFromAdStatus(model));
}

#pragma mark ATBannerDelegate

- (void)bannerView:(ATBannerView *)bannerView didClickWithPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponBannerClick placementId:placementID extra:extra error:nil];
}

- (void)bannerView:(ATBannerView *)bannerView didShowAdWithPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponBannerShow placementId:placementID extra:extra error:nil];
}

- (void)bannerView:(ATBannerView *)bannerView didTapCloseButtonWithPlacementID:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponBannerClose placementId:placementID extra:extra error:nil];
}

- (void)bannerView:(ATBannerView *)bannerView didAutoRefreshWithPlacement:(NSString *)placementID extra:(NSDictionary *)extra {
  [self emitEvent:kToponBannerRefresh placementId:placementID extra:extra error:nil];
}

- (void)bannerView:(ATBannerView *)bannerView failedToAutoRefreshWithPlacementID:(NSString *)placementID error:(NSError *)error {
  [self emitEvent:kToponBannerRefreshFail placementId:placementID extra:nil error:error.localizedDescription];
}

#pragma mark - Turbo module bootstrap

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
  (const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeToponSpecJSI>(params);
}

+ (NSString *)moduleName {
  return @"Topon";
}

@end
