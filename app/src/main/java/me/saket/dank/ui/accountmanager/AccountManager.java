package me.saket.dank.ui.accountmanager;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.auto.value.AutoValue;
import io.reactivex.functions.Function;

import me.saket.dank.ui.accountmanager.AccountManagerScreenUiModel;
import me.saket.dank.ui.accountmanager.AutoValue_AccountManager;
import me.saket.dank.utils.Cursors;

@AutoValue
public abstract class AccountManager implements AccountManagerScreenUiModel {

  static final String TABLE_NAME = "Account";
  static final String COLUMN_USERNAME = "username";
  static final String COLUMN_LABEL = "label";

  public static final String QUERY_CREATE_TABLE =
      "CREATE TABLE " + TABLE_NAME + " ("
          + COLUMN_LABEL + " TEXT NOT NULL PRIMARY KEY, "
          + COLUMN_USERNAME + " TEXT NOT NULL)";

  public static final String QUERY_GET_ALL_ORDERED_BY_USER =
      "SELECT * FROM " + TABLE_NAME;

  public static final String WHERE_USERNAME =
      COLUMN_LABEL + " = ?";

  public static AccountManager create(int rank, String label) {
    return new AutoValue_AccountManager(rank, label);
  }

  public static AccountManager create(String username) {
    return new AutoValue_AccountManager(1, username);
  }

  public static final Function<Cursor, AccountManager> MAPPER = cursor -> {
    int user = Cursors.intt(cursor, COLUMN_USERNAME);
    String label = Cursors.string(cursor, COLUMN_LABEL);
    return create(user, label);
  };

  public abstract int rank();

  public abstract String label();

  public AccountManager withRank(int newRank) {
    return create(newRank, label());
  }

  public String id() {
    return label();
  }

  @Override
  public long adapterId() {
    return label().hashCode();
  }

  public ContentValues toValues() {
    ContentValues values = new ContentValues(2);
    values.put(COLUMN_USERNAME, rank());
    values.put(COLUMN_LABEL, label());
    return values;
  }
}
