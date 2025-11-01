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

详细 API 请查看 `src/index.tsx` 中的导出定义。

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

## iOS 状态

当前仓库已优先完成 Android 新架构支持，iOS 部分仍在迁移中，请暂时忽略代码中的旧示例实现。

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
