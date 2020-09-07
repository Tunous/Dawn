package me.saket.dank

import android.content.Context

object AppUpdateActions {
  const val UPDATE_FLAGS_PREFS = "update_flags"
  const val DATA_VERSION_FIELD = "data_version"

  // When making any incompatible changes to app's data structure
  // that require non-automatic migration logic, CURRENT_DATA_VERSION should be incremented
  // and this change should be handled in afterUpdate function.
  const val CURRENT_DATA_VERSION = 2

  // If change requires clearing one of caches, set needed const values below
  // to match current (incremented) data version. Values <= 0 are ignored.
  const val MEDIA_CACHE_SHOULD_BE_CLEAN_SINCE = -1
  const val ROOM_CACHE_SHOULD_BE_CLEAN_SINCE = 2

  @JvmStatic fun handleDataUpdateIfNeeded(appContext: Context) {
    val flags = appContext.getSharedPreferences(UPDATE_FLAGS_PREFS, Context.MODE_PRIVATE)
    val prevData = flags.getInt(DATA_VERSION_FIELD, -1)
    if (prevData != CURRENT_DATA_VERSION) {
      afterUpdate(appContext, prevData)
      val editor = flags.edit()
      editor.putInt(DATA_VERSION_FIELD, CURRENT_DATA_VERSION)
      editor.apply()
    }
  }

  @JvmStatic private fun afterUpdate(appContext: Context, prevData: Int) {
    val checkVersion: (Int) -> Boolean = {
      it > 0 && prevData < it
    }

    if (checkVersion(MEDIA_CACHE_SHOULD_BE_CLEAN_SINCE)) {
      appContext.cacheDir.deleteRecursively()
    }

    if (checkVersion(ROOM_CACHE_SHOULD_BE_CLEAN_SINCE)) {
      appContext.deleteDatabase("Dank-room")
    }
  }
}
