package com.example.shortcuts_dynamic

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.Executors

class ShortcutsDynamicPlugin : FlutterPlugin, MethodCallHandler {
    private val TAG = "ShortcutsDynamicPlugin"
    private lateinit var channel: MethodChannel
    private lateinit var mContext: Context
    private lateinit var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    private val executor = Executors.newSingleThreadExecutor()

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "shortcut")
        channel.setMethodCallHandler(this)
        mContext = flutterPluginBinding.applicationContext
        this.flutterPluginBinding = flutterPluginBinding
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "create" -> {
                try {
                    val shortcutInfoIntent = Intent()
                    @Suppress("UNCHECKED_CAST")
                    val map = call.arguments as? Map<String, String> ?: run {
                        result.error("INVALID_ARGUMENTS", "Arguments must be a Map<String, String>", null)
                        return
                    }

                    val name = call.argument<String>("name")
                    val id = call.argument<String>("id")
                    val iconPath = map["iconPath"]

                    if (name == null || id == null || iconPath == null) {
                        result.error("INVALID_ARGUMENTS", "name, id and iconPath are required", null)
                        return
                    }

                    if (map.containsKey("packageName") && map.containsKey("activityName")) {
                        shortcutInfoIntent.setClassName(map["packageName"]!!, map["activityName"]!!)
                    }

                    map.forEach { (key, value) ->
                        shortcutInfoIntent.putExtra(key, value)
                    }

                    shortcutInfoIntent.action = Intent.ACTION_MAIN

                    // Verifica se o iconPath é uma URL
                    if (iconPath.startsWith("http://") || iconPath.startsWith("https://")) {
                        executor.execute {
                            try {
                                val bitmap = downloadImage(iconPath)
                                if (bitmap != null) {
                                    val file = saveBitmapToFile(bitmap, id)
                                    addShortcut(name, id, shortcutInfoIntent, bitmap, result)
                                } else {
                                    result.error("SHORTCUT_ERROR", "Failed to download image", null)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error downloading image", e)
                                result.error("SHORTCUT_ERROR", "Failed to download image", e.message)
                            }
                        }
                    } else {
                        // Tenta carregar do assets
                        val key = flutterPluginBinding.flutterAssets.getAssetFilePathByName(iconPath)
                        addShortcut(name, id, shortcutInfoIntent, getImageFromAssetsFile(key), result)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating shortcut", e)
                    result.error("SHORTCUT_ERROR", "Failed to create shortcut", e.message)
                }
            }
            "search" -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val map = call.arguments as? Map<String, String> ?: run {
                        result.error("INVALID_ARGUMENTS", "Arguments must be a Map<String, String>", null)
                        return
                    }

                    val id = map["id"]
                    if (id == null) {
                        result.error("INVALID_ARGUMENTS", "id is required", null)
                        return
                    }

                    val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager == null) {
                        result.error("SHORTCUT_ERROR", "ShortcutManager not available", null)
                        return
                    }

                    if (shortcutManager.isRequestPinShortcutSupported) {
                        val pinnedShortcuts = shortcutManager.pinnedShortcuts
                        val exists = pinnedShortcuts.any { it.id == id }
                        result.success(exists)
                    } else {
                        result.error("SHORTCUT_ERROR", "Pin shortcut not supported", null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error searching shortcut", e)
                    result.error("SHORTCUT_ERROR", "Failed to search shortcut", e.message)
                }
            }
            "list" -> {
                try {
                    val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
                    if (shortcutManager == null) {
                        result.error("SHORTCUT_ERROR", "ShortcutManager not available", null)
                        return
                    }

                    val pinnedShortcuts = shortcutManager.pinnedShortcuts
                    val shortcutsList = pinnedShortcuts.map { shortcut ->
                        mapOf(
                            "id" to shortcut.id,
                            "shortLabel" to shortcut.shortLabel,
                            "longLabel" to shortcut.longLabel,
                            "iconPath" to "", // O ícone não pode ser recuperado diretamente
                            "packageName" to mContext.packageName,
                            "activityName" to "${mContext.packageName}.MainActivity"
                        )
                    }

                    result.success(shortcutsList)
                } catch (e: Exception) {
                    Log.e(TAG, "Error listing shortcuts", e)
                    result.error("SHORTCUT_ERROR", "Failed to list shortcuts", e.message)
                }
            }
            "remove" -> {
                try {
                    @Suppress("UNCHECKED_CAST")
                    val map = call.arguments as? Map<String, String> ?: run {
                        result.error("INVALID_ARGUMENTS", "Arguments must be a Map<String, String>", null)
                        return
                    }

                    val id = map["id"]
                    if (id == null) {
                        result.error("INVALID_ARGUMENTS", "id is required", null)
                        return
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val shortcutManager = mContext.getSystemService(ShortcutManager::class.java)
                        if (shortcutManager == null) {
                            result.error("SHORTCUT_ERROR", "ShortcutManager not available", null)
                            return
                        }

                        // Desativa todos os atalhos
                        shortcutManager.disableShortcuts(listOf(id))
                        
                        // Remove os atalhos
                        shortcutManager.removeDynamicShortcuts(listOf(id))
                        
                        // Limpa o cache do ícone se existir
                        val iconFile = File(mContext.cacheDir, "shortcut_icon_$id.png")
                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                        
                        result.success(true)
                    } else {
                        // Para versões antigas do Android
                        val shortcut = Intent("com.android.launcher.action.UNINSTALL_SHORTCUT")
                        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, id)
                        mContext.sendBroadcast(shortcut)
                        
                        // Limpa o cache do ícone se existir
                        val iconFile = File(mContext.cacheDir, "shortcut_icon_$id.png")
                        if (iconFile.exists()) {
                            iconFile.delete()
                        }
                        
                        result.success(true)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error removing shortcut", e)
                    result.error("SHORTCUT_ERROR", "Failed to remove shortcut", e.message)
                }
            }
            else -> result.notImplemented()
        }
    }

    private fun downloadImage(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading image", e)
            null
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap, id: String): File {
        val file = File(mContext.cacheDir, "shortcut_icon_$id.png")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    private fun getImageFromAssetsFile(fileName: String): Bitmap? {
        var image: Bitmap? = null
        val am = flutterPluginBinding.applicationContext.assets
        try {
            am.open(fileName).use { inputStream ->
                image = BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading image from assets", e)
        }
        return image
    }

    private fun createAdaptiveIcon(bitmap: Bitmap): Icon {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Icon.createWithAdaptiveBitmap(bitmap)
        } else {
            Icon.createWithBitmap(bitmap)
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        executor.shutdown()
    }

    fun addShortcut(name: String, id: String, actionIntent: Intent, bitmap: Bitmap?, result: Result) {
        if (bitmap == null) {
            result.error("SHORTCUT_ERROR", "Failed to load icon", null)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            try {
                val shortcut = Intent("com.android.launcher.action.INSTALL_SHORTCUT")
                shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
                shortcut.putExtra("duplicate", false)
                shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
                shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, actionIntent)
                mContext.sendBroadcast(shortcut)
                result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating legacy shortcut", e)
                result.error("SHORTCUT_ERROR", "Failed to create legacy shortcut", e.message)
            }
        } else {
            try {
                val shortcutManager = mContext.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
                if (shortcutManager == null) {
                    result.error("SHORTCUT_ERROR", "ShortcutManager not available", null)
                    return
                }

                val icon = createAdaptiveIcon(bitmap)
                val shortcutInfo = ShortcutInfo.Builder(mContext, id)
                    .setShortLabel(name)
                    .setIntent(actionIntent)
                    .setLongLabel(name)
                    .setIcon(icon)
                    .build()

                val shortcutCallbackIntent = PendingIntent.getBroadcast(
                    mContext,
                    0,
                    actionIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                shortcutManager.requestPinShortcut(
                    shortcutInfo,
                    shortcutCallbackIntent.intentSender
                )
                result.success(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error creating modern shortcut", e)
                result.error("SHORTCUT_ERROR", "Failed to create modern shortcut", e.message)
            }
        }
    }
} 