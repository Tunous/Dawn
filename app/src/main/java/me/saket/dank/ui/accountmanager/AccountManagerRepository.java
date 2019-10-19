package me.saket.dank.ui.accountmanager;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.pm.ShortcutManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import androidx.annotation.CheckResult;

import com.squareup.sqlbrite2.BriteDatabase;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.util.List;
import javax.inject.Inject;

@TargetApi(Build.VERSION_CODES.N_MR1)
public class AccountManagerRepository {

  private final Application appContext;
  private final Lazy<BriteDatabase> database;
  private final Lazy<ShortcutManager> shortcutManager;

  @Inject
  public AccountManagerRepository(Application appContext, Lazy<BriteDatabase> database, Lazy<ShortcutManager> shortcutManager) {
    this.appContext = appContext;
    this.database = database;
    this.shortcutManager = shortcutManager;
  }

  @CheckResult
  public Observable<List<AccountManager>> accounts() {
    return database.get()
        .createQuery(AccountManager.TABLE_NAME, AccountManager.QUERY_GET_ALL_ORDERED_BY_USER)
        .mapToList(AccountManager.MAPPER);
  }

  @CheckResult
  public Completable add(AccountManager account) {
    return Completable
        .fromAction(() -> database.get().insert(AccountManager.TABLE_NAME, account.toValues(), SQLiteDatabase.CONFLICT_REPLACE));
  }

  @CheckResult
  public Completable delete(AccountManager account) {
    return Completable
        .fromAction(() -> database.get().delete(AccountManager.TABLE_NAME, AccountManager.WHERE_USERNAME, account.label()));
  }
}
