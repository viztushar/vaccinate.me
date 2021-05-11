package dev.viztushar.vaccinateme

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.*

class MainActivity : FlutterActivity() {
    private val CHANNEL = "dev.viztushar.vaccinateme/check"
     val SHARED_PREFERENCES_NAME = "FlutterSharedPreferences"
    private var preferences: SharedPreferences? = null

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            if (call.method == "startServices") {

                val services = call.argument<Boolean>("services")
                preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

                val notificationIntent = Intent(this, CheckServices::class.java)
                val arrPerm = ArrayList<String>()
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    arrPerm.add(Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND)
                }
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    arrPerm.add(Manifest.permission.REQUEST_COMPANION_USE_DATA_IN_BACKGROUND)
                }
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    arrPerm.add(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                }
                if (!arrPerm.isEmpty()) {
                    var permissions: Array<String?>? = arrayOfNulls(arrPerm.size)
                    permissions = arrPerm.toArray(permissions)
                    ActivityCompat.requestPermissions(this, permissions, 0)
                }

                val cp = ComponentName(
                    applicationContext,
                    CheckServices::class.java
                )
                val pm = packageManager
                pm.setComponentEnabledSetting(
                    cp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                if (services == true) {
                    if (!CheckServices.isServiceCreated()) {
                        ContextCompat.startForegroundService(applicationContext,notificationIntent)
                    } else {
                        stopService(notificationIntent)
                        Thread.sleep(3000)
                        ContextCompat.startForegroundService(applicationContext,notificationIntent)
                    }
                } else if (services == false) {
                    stopService(notificationIntent)
                }


                result.success(preferences?.getString("flutter.checkAvailability", "0"))
//                val batteryLevel = getBatteryLevel()
//
//                if (batteryLevel != -1) {
//                    result.success(batteryLevel)
//                } else {
//                    result.error("UNAVAILABLE", "Battery level not available.", null)
//                }
            } else {
                result.notImplemented()
            }
        }
    }

}
