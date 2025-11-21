import { useEffect, useState } from 'react';
import {
  Button,
  NativeEventEmitter,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Topon, {
  SDK,
  RewardedVideo,
  Banner,
  ToponEvents,
  type RewardedEventName,
  type InterstitialEventName,
  type BannerEventName,
  Interstitial,
} from 'react-native-topon';

const eventEmitter = new NativeEventEmitter(Topon.NativeModule);
const APP_ID = 'YOUR_APP_ID';
const APP_KEY = 'YOUR_APP_KEY';
const rewardPlacementId = 'YOUR_REWARDED_PLACEMENT_ID';

type LogEntry = { type: string; payload: unknown };

export default function App() {
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [isLoadingRewarded, setIsLoadingRewarded] = useState(false);

  const handleShowRewarded = async () => {
    setIsLoadingRewarded(true);
    try {
      const isReady = await RewardedVideo.hasAdReady(rewardPlacementId);
      if (isReady) {
        RewardedVideo.showAd(rewardPlacementId);
      } else {
        setLogs((prev) => [
          {
            type: 'Rewarded/Info',
            payload: 'Ad not ready yet, requesting load...',
          },
          ...prev,
        ]);
        RewardedVideo.loadAd(rewardPlacementId);
      }
    } finally {
      setIsLoadingRewarded(false);
    }
  };

  useEffect(() => {
    SDK.setLogDebug(true);
    SDK.init(APP_ID, APP_KEY);

    const rewardListeners = Object.values(ToponEvents.RewardedVideo).map(
      (event) =>
        eventEmitter.addListener(event as RewardedEventName, (payload) => {
          setLogs((prev) => [{ type: `Rewarded/${event}`, payload }, ...prev]);
        })
    );

    const interstitialListeners = Object.values(ToponEvents.Interstitial).map(
      (event) =>
        eventEmitter.addListener(event as InterstitialEventName, (payload) => {
          setLogs((prev) => [
            { type: `Interstitial/${event}`, payload },
            ...prev,
          ]);
        })
    );

    const bannerListeners = Object.values(ToponEvents.Banner).map((event) =>
      eventEmitter.addListener(event as BannerEventName, (payload) => {
        setLogs((prev) => [{ type: `Banner/${event}`, payload }, ...prev]);
      })
    );

    RewardedVideo.loadAd(rewardPlacementId);

    // 如需插屏广告，请将此处替换为真实的插屏位 ID 再启用
    Interstitial.loadAd('your-interstitial-placement-id');

    Banner.loadAd('YOUR_BANNER_PLACEMENT_ID', {
      width: 320,
      height: 50,
      adaptive_type: 0,
    });

    return () => {
      [
        ...rewardListeners,
        ...interstitialListeners,
        ...bannerListeners,
      ].forEach((listener) => listener.remove());
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>TopOn Demo</Text>
      <View style={styles.buttonRow}>
        <Button
          title={isLoadingRewarded ? 'Checking…' : '显示激励广告'}
          onPress={handleShowRewarded}
          disabled={isLoadingRewarded}
        />
      </View>
      <ScrollView style={styles.logContainer}>
        {logs.map((item, index) => (
          <View key={index} style={styles.logRow}>
            <Text style={styles.logType}>{item.type}</Text>
            <Text style={styles.logPayload}>
              {JSON.stringify(item.payload)}
            </Text>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
    backgroundColor: '#f2f4f8',
  },
  title: {
    fontSize: 22,
    fontWeight: '600',
    marginBottom: 8,
  },
  subtitle: {
    fontSize: 14,
    color: '#6b7280',
    marginBottom: 16,
  },
  logContainer: {
    flex: 1,
    backgroundColor: '#ffffff',
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 8,
  },
  buttonRow: {
    marginBottom: 12,
  },
  logRow: {
    marginBottom: 12,
  },
  logType: {
    fontWeight: '600',
    color: '#1f2937',
  },
  logPayload: {
    color: '#4b5563',
  },
});
