package com.rnandroidblewatcher;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import javax.annotation.Nullable;

public class RnAndroidBleWatcherTask extends HeadlessJsTaskService {

  public static final int NOTIFICATION_ID = 1337420;
  @Override
  protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
    Bundle extras = intent.getExtras();

    if (extras != null) {
      String taskName = extras.getString("taskName");

      Log.d("RnAndroidBleWatcher", "Starting Task: " + taskName);
      return new HeadlessJsTaskConfig(taskName, Arguments.fromBundle(extras), 15000, true);
    }
    return null;
  }
  @Override
  public void onCreate() {
    super.onCreate();

    Log.d("RnAndroidBleWatcher", "On Create in task called");
  }
  @RequiresApi(api = Build.VERSION_CODES.O)
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d("RnAndroidBleWatcher", "Start command called");
    super.onStartCommand(intent, flags, startId);

    Bundle extras = intent.getExtras();

    Bundle options = extras.getBundle("options");

    Bundle notificationOptions = options.getBundle("notification");

    String channelId = notificationOptions.getString("channelId");
    String channelName = notificationOptions.getString("channelName");

    HeadlessJsTaskConfig taskConfig = getTaskConfig(intent);

    NotificationManager notificationManager = getSystemService(NotificationManager.class);

    NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);

    notificationManager.createNotificationChannel(channel);

    Notification notification = new Notification.Builder(this, channelId)
        .setContentTitle(channelName)
        .setSmallIcon(R.drawable.redbox_top_border_background)
        .setPriority(Notification.PRIORITY_HIGH)
        .build();

    startForeground(NOTIFICATION_ID, notification);

    if (taskConfig != null) {
      startTask(taskConfig);

      return START_REDELIVER_INTENT;
    }
    return START_STICKY;
  }
}
