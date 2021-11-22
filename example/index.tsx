import { AppRegistry } from 'react-native';
import App from './src/App';
import { name as appName } from './app.json';

import BackgroundTask from './src/BackgroundTask.js';

AppRegistry.registerComponent(appName, () => App);

AppRegistry.registerHeadlessTask('MyBackgroundTask', () => BackgroundTask);
