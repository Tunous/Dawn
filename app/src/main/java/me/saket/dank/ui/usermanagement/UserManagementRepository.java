package me.saket.dank.ui.usermanagement;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.CheckResult;

import com.squareup.sqlbrite2.BriteDatabase;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.util.List;
import javax.inject.Inject;

@TargetApi(Build.VERSION_CODES.N_MR1)
public class UserManagementRepository {

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
        .fromAction(() -> database.get().insert(UserManagement.TABLE_NAME, user.toValues(), SQLiteDatabase.CONFLICT_REPLACE));
  }

  @CheckResult
  public Completable delete(UserManagement user) {
    return Completable
        .fromAction(() -> database.get().delete(UserManagement.TABLE_NAME, UserManagement.WHERE_USER, user.label()));
  }
}
