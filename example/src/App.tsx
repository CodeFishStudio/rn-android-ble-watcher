import * as React from 'react';

import { StyleSheet, View, Text } from 'react-native';
import { startMonitoring } from 'rn-android-ble-watcher';

export default function App() {
    React.useEffect(() => {
            startMonitoring('CC:57:8B:D4:1D:5B', 'MyBackgroundTask');
            }, []);

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
