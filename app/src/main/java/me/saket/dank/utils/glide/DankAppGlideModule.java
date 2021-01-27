package me.saket.dank.utils.glide;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.Excludes;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

import java.io.File;

/**
 * Glide requires atleast one app module if library modules are used.
 */
@GlideModule
@Excludes({ OkHttpLibraryGlideModule.class })
public class DankAppGlideModule extends AppGlideModule {

  // Must be kept in sync with file_provider_paths.xml
  private static final String GLIDE_DISK_CACHE_FOLDER_NAME = "image_manager_disk_cache";

  // 200 MB in bytes as expected by Glide
  private static final long GLIDE_DISK_CACHE_SIZE = 200 * 1024 * 1024;

  @Override
  public boolean isManifestParsingEnabled() {
    return false;
  }

  @Override
  public void applyOptions(Context context, GlideBuilder builder) {
    final String cacheDirectoryPath = new File(context.getCacheDir(), GLIDE_DISK_CACHE_FOLDER_NAME).getAbsolutePath();
    builder.setLogLevel(Log.ERROR);
    builder.setDiskCache(() -> new DiskLruCacheFactory(cacheDirectoryPath, GLIDE_DISK_CACHE_SIZE).build());
  }
}
