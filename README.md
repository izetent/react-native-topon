# react-native-topon

React Native TurboModule 封装的 TopOn (Anythink) SDK 接入方案，支持激励视频、插屏、Banner 等广告能力。

## 环境要求

- Node.js ≥ 20（示例工程使用 Yarn 3）
- React Native ≥ 0.81（TurboModule 新架构）
- Android：AGP 8.7.x、compileSdk 36、minSdk 24

## 安装

```sh
yarn add react-native-topon
```

安装后需要运行一次 Codegen 生成 TurboModule 绑定：

```sh
yarn react-native codegen
```


## 快速开始

TopOn 模块分为两个层面：

- **SDK**：负责初始化、GDPR、日志开关等全局配置。
- **RewardedVideo / Interstitial / Banner**：提供各广告位的加载、展示、状态查询方法。

```ts
import Topon, {
  SDK,
  RewardedVideo,
  Interstitial,
  Banner,
  ToponEvents,
} from 'react-native-topon';
import { NativeEventEmitter } from 'react-native';

// SDK 初始化
SDK.init('your-app-id', 'your-app-key');

// 订阅事件
const emitter = new NativeEventEmitter(Topon.NativeModule);
const subscription = emitter.addListener(ToponEvents.RewardedVideo.Loaded, payload => {
  console.log('Rewarded loaded', payload.placementId);
});

// 激励广告示例
RewardedVideo.loadAd('placement-id', { userID: 'user-1' });
const isReady = await RewardedVideo.hasAdReady('placement-id');
if (isReady) {
  RewardedVideo.showAd('placement-id');
}

const status = await RewardedVideo.checkAdStatus('placement-id');
console.log('Rewarded status', status?.isReady);

// 组件卸载时移除监听
subscription.remove();
```

详细 API 请查看 `src/index.tsx` 中的导出定义，或参考下方表格。

## API 总览

### SDK

| 方法 | 说明 |
| --- | --- |
| `init(appId, appKey)` | 初始化 SDK，**必须**在所有广告调用前执行一次 |
| `setLogDebug(isDebug)` | 打开/关闭原生日志输出，便于调试 |
| `getSDKVersionName()` | 返回当前 TopOn SDK 版本号 |
| `isCnSDK()` | 判断是否为国内 SDK |
| `setExcludeMyOfferPkgList(packages)` | 排除 MyOffer 包名黑名单 |
| `initCustomMap(customMap)` / `setPlacementCustomMap(placementId, map)` | 设置全局或广告位级别的扩展参数 |
| `setGDPRLevel(level)` / `getGDPRLevel()` / `getUserLocation()` / `showGDPRAuth()` | GDPR 相关 API |
| `deniedUploadDeviceInfo(keys)` | 拒绝上传指定设备信息字段 |

### RewardedVideo / Interstitial

| 方法 | 说明 |
| --- | --- |
| `loadAd(placementId, settings?)` | 加载广告，`settings` 可包含 `userID`、`media_ext`、`custom_rule` 等 TopOn 字段 |
| `showAd(placementId)` / `showAdInScenario(placementId, scenario)` | 展示广告，可按需指定 `scenario` |
| `hasAdReady(placementId)` | 返回该广告位当前是否可播放 |
| `checkAdStatus(placementId)` | 返回 `ToponAdStatus`（`isLoading`、`isReady`、`adInfo`），便于诊断 |

> 建议在收到 `Loaded` 事件后再调用 `hasAdReady` 或 `checkAdStatus`，避免频繁轮询。

### Banner

| 方法 | 说明 |
| --- | --- |
| `loadAd(placementId, settings?)` | 加载 Banner；`settings` 支持 `width`、`height`、`adaptive_type` 等键 |
| `showAdInRectangle(placementId, rect)` | 以 `{ x, y, width, height }` 指定展示区域 |
| `showAdInPosition(placementId, position)` | `position` 取值 `top`/`bottom` |
| `showAdInRectangleAndScenario` / `showAdInPositionAndScenario` | 在指定场景中展示 |
| `hideAd` / `reShowAd` / `removeAd` | 控制 Banner 显示、隐藏与彻底移除 |
| `hasAdReady` / `checkAdStatus` | 查询加载状态信息 |

## 事件监听

TopOn 模块通过 `ToponEvents` 常量暴露所有回调，配合 `NativeEventEmitter` 使用即可：

```ts
import { NativeEventEmitter } from 'react-native';
import Topon, { ToponEvents } from 'react-native-topon';

const emitter = new NativeEventEmitter(Topon.NativeModule);
const subscriptions = [
  emitter.addListener(ToponEvents.RewardedVideo.Loaded, payload => {
    console.log('激励加载成功', payload.placementId);
  }),
  emitter.addListener(ToponEvents.RewardedVideo.LoadFail, payload => {
    console.warn('激励加载失败', payload.errorMsg);
  }),
  emitter.addListener(ToponEvents.RewardedVideo.Close, payload => {
    console.log('激励关闭', payload.placementId);
  }),
];

// 组件卸载时清理
return () => subscriptions.forEach(sub => sub.remove());
```

可监听的事件名称：

- **RewardedVideo**：`Loaded`、`LoadFail`、`PlayStart`、`PlayEnd`、`PlayFail`、`Close`、`Click`、`Reward`
- **Interstitial**：`Loaded`、`LoadFail`、`PlayStart`、`PlayEnd`、`PlayFail`、`Close`、`Click`、`Show`
- **Banner**：`Loaded`、`LoadFail`、`Close`、`Click`、`Show`、`Refresh`、`RefreshFail`

事件回调默认携带 `{ placementId, adCallbackInfo? }`；失败类事件额外包含 `errorMsg`，具体类型定义请参考 `ToponRewardedEventPayload` 等 TypeScript 类型。

## 示例工程

仓库提供了一个最小示例（`example` workspace）。运行前先在 `example/src/App.tsx` 填写自身的 AppId / AppKey 以及广告位 ID：

```ts
SDK.init('your-app-id', 'your-app-key');

RewardedVideo.loadAd('reward-placement-id', {
  userID: 'demo-user',
  media_ext: 'demo',
});

Banner.loadAd('banner-placement-id', {
  width: 320,
  height: 50,
  adaptive_type: 0,
});

// 插屏广告需要真实的插屏位 ID
// Interstitial.loadAd('interstitial-placement-id');
```

然后依次执行：

```sh
yarn install
yarn example              # 启动 Metro
yarn run android --port=4321
```

应用会在界面上输出各广告位的事件日志。若出现 `Invalid placement`，请检查后台状态与参数是否一致。

## Android 集成说明

### 1. 仓库地址

TopOn 官方依赖托管在 `overseas_sdk` 仓库，宿主 App 的 `android/build.gradle`（Project 级）需要额外声明：

```gradle
allprojects {
  repositories {
    maven { url "https://jfrog.anythinktech.com/artifactory/overseas_sdk" }
    maven { url "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea" } // 如使用 Mintegral
    flatDir { dirs "$rootDir/libs" } // 若需手动放置本地 AAR
  }
}
```

### 2. 依赖管理

由于仓库样例处于离线环境，库模块默认引用了 `reactnative_sdk_output/Android/bridge_android/library/anythink_reactnativejs_bridge.aar`。在真实项目中请改为使用 TopOn 官方 Maven 依赖，例如：

```gradle
dependencies {
  implementation "com.anythink.sdk:core-tpn:6.5.10"
  implementation "com.anythink.sdk:banner-tpn:6.5.10"
  implementation "com.anythink.sdk:interstitial-tpn:6.5.10"
  implementation "com.anythink.sdk:rewardedvideo-tpn:6.5.10"
  implementation "com.anythink.sdk:nativead-tpn:6.5.10"
  implementation "com.anythink.sdk:splash-tpn:6.5.10"
  implementation "com.anythink.sdk:tramini-plugin-tpn:6.5.10"

  // 广告网络适配器（示例）
  implementation "com.anythink.sdk:adapter-tpn-admob:6.5.10"
  implementation "com.google.android.gms:play-services-ads:24.4.0"
  implementation "com.anythink.sdk:adapter-tpn-facebook:6.5.10"
  implementation "com.facebook.android:audience-network-sdk:6.20.0"
  // ... 根据后台配置决定
}
```

> ⚠️ 请严格与 TopOn 后台配置保持一致；离线开发时，可将官方 SDK AAR 放入 `android/libs` 并通过 `implementation files('libs/xxx.aar')` 引用。

如果集成 AdMob，请为宿主应用添加 `com.google.android.gms.ads.APPLICATION_ID` 元信息。调试阶段可使用 Google 提供的测试 ID：

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-3940256099942544~3347511713" />
```

> 生产包请务必替换为自己的 AdMob App ID。

### 3. Manifest 配置

库模块会自动合并以下节点，如宿主 App 有自定义 `application`，请确保未被覆盖：

```xml
<uses-library
    android:name="org.apache.http.legacy"
    android:required="false" />

<meta-data
    android:name="com.google.android.gms.ads.AD_MANAGER_APP"
    android:value="true" />
```

若还有其他渠道所需权限、`provider`、`activity` 等，请根据 [TopOn 官方文档](https://help.toponad.net/cn/access?slug=Android) 合并到宿主工程。

### 4. 混淆与权限

TopOn 要求保留大量类与字段，请在宿主 App 的 `proguard-rules.pro` 中加入官方提供的 keep 配置，并在 `AndroidManifest.xml` 中声明网络、设备信息等必要权限，同样参考 TopOn 文档补全。

## iOS 集成说明

1. 进入示例或宿主 App 的 `ios` 目录执行 `RCT_NEW_ARCH_ENABLED=1 pod install`（或直接 `pod install`），`Topon.podspec` 会自动拉取 `AnyThinkSDK`、`AnyThinkRewardedVideo`、`AnyThinkInterstitial`、`AnyThinkBanner` 等依赖，无需手动添加。
2. 在 JS 侧调用 `SDK.init(appId, appKey)` 与 Android 保持一致，其他 API 亦共享同一套定义。
3. 确保 `Info.plist` 中包含 `NSUserTrackingUsageDescription`（请求 IDFA 必须）以及宿主业务所需的权限、`SKAdNetworkItems` 等配置，具体以 [TopOn iOS 接入文档](https://help.toponad.net/cn/access?slug=iOS) 为准。
4. 若工程启用 `use_frameworks!`，请保持与 React Native 新架构兼容的配置（`use_frameworks! :linkage => :static`），避免 Pod 链接方式冲突。

iOS 与 Android 在事件名、回调入参上保持一致，所有广告能力均通过同一个 TurboModule 导出，示例应用可直接在 iOS 端运行验证。

## 开发与贡献

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

常用脚本：

```sh
yarn typecheck
yarn lint
yarn test
```

## License

MIT
