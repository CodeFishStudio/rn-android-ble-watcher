import * as React from 'react';

import { StyleSheet, View, Text, AppState } from 'react-native';
import { configure, startMonitoring, stopMonitoring } from 'rn-android-ble-watcher';

export default function App() {
  const [currentState, setCurrentState] = React.useState<string>(
    AppState.currentState
  );

  React.useEffect(() => {
    const handler = (nextState: string) => {
      setCurrentState(nextState);
    };
    AppState.addEventListener('change', handler);

    return () => {
      AppState.removeEventListener('change', handler);
    };
  }, []);

  React.useEffect(() => {
    if (currentState === 'active') {
      stopMonitoring();
    } else {
        
      configure({
            notification: {
                channelId: 'blewatcher-example',
                channelName: 'BLE Watcher Example',
            }
       });
      startMonitoring('CC:57:8B:D4:1D:5B', 'MyBackgroundTask');
    }
  }, [currentState]);

  return (
    <View style={styles.container}>
      <Text>Starting to monitor?</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
