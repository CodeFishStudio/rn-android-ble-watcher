package com.rnandroidblewatcher;

import android.os.Build;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

@ReactModule(name = RnAndroidBleWatcherModule.NAME)
public class RnAndroidBleWatcherModule extends ReactContextBaseJavaModule {
    public static final String NAME = "RnAndroidBleWatcher";

    public static final String LOG_TAG = NAME;

    private WritableMap options = Arguments.createMap();

    private ReactApplicationContext mContext = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private PersistableBundle optionsToBundle(ReadableMap opts) {
      PersistableBundle bundle = new PersistableBundle();

      PersistableBundle notificationBundle = new PersistableBundle();

      // Merge new options with defaults
      options.merge(opts);

      ReadableMap notificationOptions = options.getMap("notification");

      notificationBundle.putString("channelId", notificationOptions.getString("channelId"));
      notificationBundle.putString("channelName", notificationOptions.getString("channelName"));

      bundle.putPersistableBundle("notification", notificationBundle);

      return bundle;
    }

    public RnAndroidBleWatcherModule(ReactApplicationContext reactContext) {
        super(reactContext);

        mContext = reactContext;

        WritableMap notificationOptions = Arguments.createMap();

        notificationOptions.putString("channelId", "BleWatcher");
        notificationOptions.putString("channelName", "Ble Watcher");

        options.putMap("notification", notificationOptions);

        options.putString("taskName", "RNWatcherTask");
    }


    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    @ReactMethod
    public void configure(ReadableMap opts) {
      options.merge(opts);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void startMonitoring(String deviceId, String taskName) {
      Log.d(LOG_TAG, "Starting monitoring on: " + deviceId);
      PersistableBundle bundle = optionsToBundle(options);

      RnAndroidBleWatcherJobService.StartJob(mContext, deviceId, taskName, bundle, true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @ReactMethod
    public void stopMonitoring() {
      Log.d(LOG_TAG, "Stopping monitoring") ;
      RnAndroidBleWatcherJobService.StopJob(mContext);
    }
}
