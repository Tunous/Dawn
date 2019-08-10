package me.saket.dank.ui.usermanagement;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.CheckResult;

import com.squareup.sqlbrite2.BriteDatabase;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

import me.saket.dank.R;
import me.saket.dank.deeplinks.DeepLinkHandlingActivity;
import me.saket.dank.ui.usermanagement.UserManagement;
import me.saket.dank.ui.usermanagement.UserManagementActivity;

@TargetApi(Build.VERSION_CODES.N_MR1)
public class UserManagementRepository {

  public static final int MAX_USER_COUNT = 6;

  private final Application appContext;
  private final Lazy<BriteDatabase> database;
  private final Lazy<ShortcutManager> shortcutManager;

  @Inject
  public UserManagementRepository(Application appContext, Lazy<BriteDatabase> database, Lazy<ShortcutManager> shortcutManager) {
    this.appContext = appContext;
    this.database = database;
    this.shortcutManager = shortcutManager;
  }

  @CheckResult
  public Observable<List<UserManagement>> users() {
    return database.get()
        .createQuery(UserManagement.TABLE_NAME, UserManagement.QUERY_GET_ALL_ORDERED_BY_USER)
        .mapToList(UserManagement.MAPPER);
  }

  @CheckResult
  public Completable add(UserManagement user) {
    return Completable
        .fromAction(() -> database.get().insert(UserManagement.TABLE_NAME, user.toValues(), SQLiteDatabase.CONFLICT_REPLACE))
        .andThen(updateStoredUsers()
            .doOnError(error -> Timber.e(error, "Couldn't update users"))
            .onErrorResumeNext(error -> delete(user)
                .andThen(Completable.error(error))));
  }

  @CheckResult
  public Completable delete(UserManagement user) {
    return Completable
        .fromAction(() -> database.get().delete(UserManagement.TABLE_NAME, UserManagement.WHERE_USER, user.label()))
        .andThen(updateStoredUsers());
  }

  @CheckResult
  public Completable updateStoredUsers() {
    return  Completable.complete();
//    return users()
//        .firstOrError()
//        .map(shortcuts -> {
//          List<ShortcutInfo> shortcutInfos;
//
//          if (shortcuts.isEmpty()) {
//            ShortcutInfo configureShortcut = new ShortcutInfo.Builder(appContext, "add_users")
//                .setShortLabel(appContext.getString(R.string.add_launcher_app_shortcuts_label))
//                .setIcon(Icon.createWithResource(appContext, R.drawable.ic_configure_app_shortcuts))
//                .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(UserManagementActivity.DEEP_LINK)))
//                .build();
//            shortcutInfos = Collections.singletonList(configureShortcut);
//
//          } else {
//            shortcutInfos = new ArrayList<>(MAX_USER_COUNT);
//
//            for (int i = 0; i < shortcuts.size(); i++) {
//              // Android displays shortcuts in descending rank, but our UI for configuring
//              // them uses ascending. So I'm manually reversing it again here.
//              int androidRank = shortcuts.size() - i;
//              UserManagement shortcut = shortcuts.get(i);
//
//              shortcutInfos.add(new ShortcutInfo.Builder(appContext, shortcut.id())
//                  .setShortLabel(appContext.getString(R.string.subreddit_name_r_prefix, shortcut.label()))    // Used by pinned shortcuts.
//                  .setLongLabel(shortcut.label())           // Shown in shortcuts popup.
//                  .setRank(androidRank)
//                  .setIcon(Icon.createWithResource(appContext, R.drawable.ic_shortcut_subreddit))
////                  .setIntent(DeepLinkHandlingActivity.userManagementIntent(appContext, shortcut))
//                  .build());
//            }
//          }
//          return shortcutInfos;
//        })
//        .flatMapCompletable(shortcutInfos -> Completable.fromAction(() ->
//            shortcutManager.get().setDynamicShortcuts(shortcutInfos))
//        );
  }
}
