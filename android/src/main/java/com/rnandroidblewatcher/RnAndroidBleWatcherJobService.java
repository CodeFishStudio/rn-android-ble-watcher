package com.rnandroidblewatcher;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;

import okhttp3.internal.http2.Header;

@RequiresApi(api = Build.VERSION_CODES.M)
public class RnAndroidBleWatcherJobService extends JobService
{

  public static final String ACTION_TASK_FINISHED = "com.rnandroidblewatcher.TASK_FINISHED";

  public static final String LOG_TAG = "RnAndroidBleWatcher";

  public static final int JOB_ID = 1326233;

  public static final int RESTART_DELAY = 5000; // Delay to restart job in MS

  public static RnAndroidBleWatcherJobService mInstance = null;

  private boolean recreateJobOnStop = true;

  private BluetoothGatt bluetoothGatt = null;

  public void StartBackgroundTask(String deviceId, String taskName, Bundle options) {
    Intent service = new Intent(getApplicationContext(), RnAndroidBleWatcherTask.class);

    Bundle bundle = new Bundle();

    bundle.putString("deviceId", deviceId);
    bundle.putString("taskName", taskName);
    bundle.putBundle("options", options);
    service.putExtras(bundle);

    /**
     * On Newer versions of android needs to run as foreground service
     */
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      ContextCompat.startForegroundService(getApplicationContext(), service);
    } else {
      getApplicationContext().startService(service);
    }

    HeadlessJsTaskService.acquireWakeLockNow(getApplicationContext());
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    Log.d("RnAndroidBleWatcher", "Job Started??");
    mInstance = this;

    // Flag the job to recreate after stop/finish
    recreateJobOnStop = true;

    String deviceId = params.getExtras().getString("deviceId");
    String taskName = params.getExtras().getString("taskName");
    Bundle options = new Bundle();
    options.putAll(params.getExtras().getPersistableBundle("options"));

    Context ctx = getApplicationContext();

    BluetoothManager bluetoothManager = null;
    bluetoothManager = (BluetoothManager) ctx.getSystemService(Context.BLUETOOTH_SERVICE);

    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceId);


    Log.d("RnAndroidBleWatcher", "Trying to connect to: " + deviceId);
    bluetoothGatt = bluetoothDevice.connectGatt(ctx, true, new BluetoothGattCallback() {
      @Override
      public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);

        Log.d("RnAndroidBleWatcher", "Connection State Change" + newState);

        if (newState == BluetoothProfile.STATE_CONNECTED) {
          Log.d("RnAndroidBleWatcher", "Connected to: " + deviceId);
          StartBackgroundTask(deviceId, taskName, options);
        }
      }
    });

    IntentFilter taskedFinished = new IntentFilter(ACTION_TASK_FINISHED);

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == ACTION_TASK_FINISHED) {
          /**
           * Check if we still have a bluetooth connection and disconnect it
           */
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (bluetoothGatt != null) {
              bluetoothGatt.disconnect();
            }
          }

          // Tell the service that our job is finished
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            jobFinished(params, false);
          }


          /**
           * Run a delayed task after 5s to start our job again
           * Delay it so that the BLE device is properly disconnected
           */
          Handler handler = new Handler();
          handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              StartJob(context, deviceId, taskName, params.getExtras());
            }
          }, RESTART_DELAY);
        }
      }
    };

    registerReceiver(broadcastReceiver, taskedFinished);
    return false;
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    return false;
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  public static void StartJob(Context context, String deviceId, String taskName, PersistableBundle options) {
      Log.d(LOG_TAG, "Starting Job: " + deviceId);

      ComponentName serviceComponent = new ComponentName(context, RnAndroidBleWatcherTask.class);

      JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceComponent);

      builder.setMinimumLatency(1 * 1000);
      builder.setOverrideDeadline(3 * 1000);
      builder.setPersisted(true);

      PersistableBundle bundle = new PersistableBundle();

      bundle.putString("deviceId", deviceId);
      bundle.putString("taskName", deviceId);

      bundle.putPersistableBundle("options", options);

      builder.setExtras(bundle);

      JobScheduler jobScheduler = null;
      jobScheduler = context.getSystemService(JobScheduler.class);

      boolean hasBeenScheduled = false;

      /**
       * Loop all scheduled jobs and find if ours is already scheduled
       */
      for (JobInfo jobInfo : jobScheduler.getAllPendingJobs())  {
        if (jobInfo.getId() == JOB_ID) {
          hasBeenScheduled = true;
          break;
        }
      }

      /**
       * If our job has been scheduled, cancel it
       * Cancelling the job will called onStopJob where we will reschedule the job
       */
      if (hasBeenScheduled) {
        jobScheduler.cancel(JOB_ID);
      } else {
        jobScheduler.schedule(builder.build());
      }

      /**
       * Create a wakelock to keep the job alive?
       */
      /*PowerManager pm = (PowerManager)  context.getSystemService(Context.POWER_SERVICE);
      PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RnAndroidBleWatcherService:wakelock");
      wakeLock.acquire(); */
  }

  public static void StopJob(Context context) {
    if (mInstance != null) {
      mInstance.recreateJobOnStop = false;
    }

    JobScheduler jobScheduler = null;
    jobScheduler = context.getSystemService(JobScheduler.class);

    jobScheduler.cancel(JOB_ID);

  }
}
