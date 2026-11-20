package com.kidcurate.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger

class KidCurateApplication : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: KidCurateApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.kidcurate.app.data.ApiClient.init(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024) // 50 MB
                    .build()
            }
            .crossfade(true)
            .respectCacheHeaders(false)
            .build()
    }
}
