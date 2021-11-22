# React Native Android BLE Watcher

Watches for Bluetooth Connections and runs background task

## Installation

```sh
npm install rn-android-ble-watcher
```

## Usage

BackgroundTask.js
```js
module.exports = async (args) => {
    console.log('Background task called', args);
};
```


```js
import {AppRegistry} from 'react-native';

import { configure, startMonitoring, stopMonitoring} from "rn-android-ble-watcher";

import BackgroundTask from './BackgroundTask.js';

AppRegistry.registerHeadlessTask('MyBackgroundTask', () => BackgroundTask);

// ...
configure({
    notification: {
        channelId:'MyExampleApp',
        channelName: 'My Example App',
    },
});

// Starts monitoring for device with address FF:FF:FF:FF:FF:FF and runs MyBackgroundTask
startMonitoring('FF:FF:FF:FF:FF:FF', 'MyBackgroundTask');

// stopMonitoring();

```

