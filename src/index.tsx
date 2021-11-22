import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'rn-android-ble-watcher' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo managed workflow\n';

type NotificationOptions = {
  channelId?: string;
  channelName?: string;
};
type ConfigurationOptions = {
  notification: NotificationOptions;
};

const RnAndroidBleWatcher = NativeModules.RnAndroidBleWatcher
  ? NativeModules.RnAndroidBleWatcher
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );


export function startMonitoring(
  deviceId: string,
  taskName: string
): Promise<void> {
  return RnAndroidBleWatcher.startMonitoring(deviceId, taskName);
}

export function stopMonitoring(): Promise<void> {
  return RnAndroidBleWatcher.stopMonitoring();
}

export function configure(options: ConfigurationOptions): Promise<void> {
  return RnAndroidBleWatcher.configure(options);
}
