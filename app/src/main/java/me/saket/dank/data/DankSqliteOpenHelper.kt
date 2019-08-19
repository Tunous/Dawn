package me.saket.dank.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import me.saket.dank.reply.PendingSyncReply
import me.saket.dank.ui.appshortcuts.AppShortcut
import me.saket.dank.ui.subscriptions.SubredditSubscription
import me.saket.dank.ui.user.messages.CachedMessage
import me.saket.dank.ui.usermanagement.UserManagement
import timber.log.Timber

class DankSqliteOpenHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(SubredditSubscription.QUERY_CREATE_TABLE)
    db.execSQL(CachedMessage.QUERY_CREATE_TABLE)
    db.execSQL(PendingSyncReply.QUERY_CREATE_TABLE)
    db.execSQL(AppShortcut.QUERY_CREATE_TABLE)
    db.execSQL(UserManagement.QUERY_CREATE_TABLE)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    when (newVersion) {
      2 -> {
        // JRAW was updateed to 1.0
        db.execSQL("DELETE FROM ${CachedMessage.TABLE_NAME}")
      }
      3 -> {
        db.execSQL(UserManagement.QUERY_CREATE_TABLE)
      }
      else -> throw IllegalStateException("onUpgrade() with unknown oldVersion $oldVersion")
    }
  }

  companion object {
    private const val DB_VERSION = 3
    private const val DB_NAME = "Dank"
  }
}
