package me.saket.dank.ui.usermanagement;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.auto.value.AutoValue;
import io.reactivex.functions.Function;

import me.saket.dank.ui.usermanagement.UserManagementScreenUiModel;
import me.saket.dank.ui.usermanagement.AutoValue_UserManagement;
import me.saket.dank.utils.Cursors;

@AutoValue
public abstract class UserManagement implements UserManagementScreenUiModel {

  static final String TABLE_NAME = "User";
  static final String COLUMN_USER = "user";
  static final String COLUMN_LABEL = "label";

  public static final String QUERY_CREATE_TABLE =
      "CREATE TABLE " + TABLE_NAME + " ("
          + COLUMN_LABEL + " TEXT NOT NULL PRIMARY KEY, "
          + COLUMN_USER + " TEXT NOT NULL)";

  public static final String QUERY_GET_ALL_ORDERED_BY_USER =
      "SELECT * FROM " + TABLE_NAME;

  public static final String WHERE_USER =
      COLUMN_LABEL + " = ?";

  public static UserManagement create(int rank, String label) {
    return new AutoValue_UserManagement(rank, label);
  }

  public static UserManagement create(String username) {
    return new AutoValue_UserManagement(1, username);
  }

  public static final Function<Cursor, UserManagement> MAPPER = cursor -> {
    int user = Cursors.intt(cursor, COLUMN_USER);
    String label = Cursors.string(cursor, COLUMN_LABEL);
    return create(user, label);
  };

  public abstract int rank();

  public abstract String label();

  public UserManagement withRank(int newRank) {
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
    values.put(COLUMN_USER, rank());
    values.put(COLUMN_LABEL, label());
    return values;
  }
}
