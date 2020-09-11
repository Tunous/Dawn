package me.saket.dank.ui.accountmanager;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.CheckResult;

import com.squareup.sqlbrite2.BriteDatabase;
import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;

import java.util.List;
import javax.inject.Inject;

public class AccountManagerRepository {

  private final Lazy<BriteDatabase> database;

  @Inject
  public AccountManagerRepository(Lazy<BriteDatabase> database) {
    this.database = database;
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
