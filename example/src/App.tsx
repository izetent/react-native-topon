import { useEffect, useState } from 'react';
import {
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
} from 'react-native-topon';

const eventEmitter = new NativeEventEmitter(Topon.NativeModule);

type LogEntry = { type: string; payload: unknown };

export default function App() {
  const [logs, setLogs] = useState<LogEntry[]>([]);

  useEffect(() => {
    SDK.setLogDebug(true);
    SDK.init('YOUR_APP_ID', 'YOUR_APP_KEY');

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

    RewardedVideo.loadAd('your-reward-placement-id', {
      userID: 'demo-user',
      media_ext: 'demo',
    });

    // 如需插屏广告，请将此处替换为真实的插屏位 ID 再启用
    // Interstitial.loadAd('your-interstitial-placement-id');

    Banner.loadAd('your-banner-placement-id', {
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
