package dev.viztushar.vaccinateme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;


import dev.viztushar.vaccinateme.task.CheckAppointment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class CheckServices extends Service implements CheckAppointment.Callbacks {
    String TAG = CheckServices.class.getSimpleName();
    private static CheckServices mInstance = null;
    PowerManager.WakeLock wakeLock;
    WifiManager.WifiLock wifiLock;
    private final IBinder mBinder = new LocalBinder();
    String SHARED_PREFERENCES_NAME = "FlutterSharedPreferences";
    SharedPreferences sharedpreferences;

    boolean searchbypin = false;
    boolean searchbyDistrict = false;
    boolean eighteenPlus = false;
    boolean fortyfivePlus = false;
    boolean covishield = false;
    boolean covaxin = false;
    boolean free = false;
    boolean paid = false;
    String checkAvailability;
    String state;
    String district;
    String pincode;
    String token;
    Handler mSearchHandler;
    Runnable mSearchRunnable;

    public static boolean isServiceCreated() {
        try {
            // If instance was not cleared but the service was destroyed an Exception will be thrown
            return mInstance != null && mInstance.ping();
        } catch (NullPointerException e) {
            // destroyed/not-started
            return false;
        }
    }

    public static synchronized CheckServices getInstance() {
        return mInstance;
    }

    private boolean ping() {
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        createNotificationChannel("Check");

        Intent notificationIntent = new Intent(this, CheckServices.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Notification notification =
                null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this, "Check")
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText("Background Service Running")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setTicker("text")
                    .setAutoCancel(false)
                    .setChannelId("Check")
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSound(uri)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText("Background Service Running")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND)
                    .setTicker("text")
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setSound(uri)
                    .build();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForeground(1234, notification);
        } else {
            getApplicationContext().startService(notificationIntent);
        }


        super.onCreate();
    }

    private void createNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.enableLights(false);
            channel.setSound(null, null);
            channel.setShowBadge(false);

            channel.setDescription("Background Service");
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved: " + rootIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent i = new Intent();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + getPackageName()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
        PowerManager p = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        assert p != null;
        wakeLock = p.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock");

        if (wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock.acquire();
        }
        sharedpreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        searchbypin = sharedpreferences.getString("flutter.searchBy", "null").toString().compareTo("pin") == 0;
        searchbyDistrict = sharedpreferences.getString("flutter.searchBy", "null").toString().compareTo("district") == 0;
        eighteenPlus = sharedpreferences.getBoolean("flutter.18", false);
        fortyfivePlus = sharedpreferences.getBoolean("flutter.45", false);
        covishield = sharedpreferences.getBoolean("flutter.covishield", false);
        covaxin = sharedpreferences.getBoolean("flutter.covaxin", false);
        free = sharedpreferences.getBoolean("flutter.free", false);
        paid = sharedpreferences.getBoolean("flutter.paid", false);
        checkAvailability = sharedpreferences.getString("flutter.checkAvailability", "null");
        state = sharedpreferences.getString("flutter.state", "null");
        district = sharedpreferences.getString("flutter.district", "null");
        pincode = sharedpreferences.getString("flutter.pincode", "null");
        token = sharedpreferences.getString("flutter.fcmToken", "null");
        Log.d(TAG, "run: " + searchbypin);
        Log.d(TAG, "run: " + searchbyDistrict);
        Log.d(TAG, "run: " + eighteenPlus);
        Log.d(TAG, "run: " + fortyfivePlus);
        Log.d(TAG, "run: " + covishield);
        Log.d(TAG, "run: " + covaxin);
        Log.d(TAG, "run: " + free);
        Log.d(TAG, "run: " + paid);
        Log.d(TAG, "run: " + checkAvailability);
        Log.d(TAG, "run: " + state);
        Log.d(TAG, "run: " + district);
        Log.d(TAG, "run: " + pincode);
        int delays = 60000;//900000;
        if (checkAvailability != null) {
            delays = Integer.parseInt(checkAvailability);
        }
        Log.d(TAG, "run: " + delays);
        mSearchHandler = new Handler();
        int finalDelays = delays;
        mSearchRunnable = new Runnable() {
            public void run() {
                try {
                    Date dt = new Date();
                    Calendar c = Calendar.getInstance();
                    c.setTime(dt);
                    c.add(Calendar.DATE, 1);
                    dt = c.getTime();
                    @SuppressLint("SimpleDateFormat") final String date = new SimpleDateFormat("dd-MM-yyyy").format(dt);

                    String districtUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=" + district + "&date=" + date;
                    String pinUrl = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=" + pincode + "&date=" + date;
                    String url;
                    if (searchbypin) {
                        url = pinUrl;
                    } else {
                        url = districtUrl;
                    }
                    Log.d(TAG, "run: " + url);
                    wakeLock.acquire();
                    mSearchHandler.postDelayed(this, finalDelays);

                    new CheckAppointment(getApplicationContext(), CheckServices.this, url, sharedpreferences, eighteenPlus, fortyfivePlus, covishield, covaxin, free, paid, token).execute();
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        };
        mSearchHandler.postDelayed(mSearchRunnable, delays);

        return START_STICKY;
    }


    public synchronized void stop() {
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
        stopForeground(false);
        // stop();
        return super.stopService(name);
    }


    @Override
    public void onDestroy() {

        mInstance = null;
        stop();
        Log.d(TAG, "Destroyed");
        stopForeground(false);
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {

        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "in onRebind()");
        stopSelf();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
        super.onRebind(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onListLoaded(String jsonResult, boolean jsonSwitch) {

    }

    public class LocalBinder extends Binder {
        CheckServices getService() {
            return CheckServices.this;
        }
    }
}
