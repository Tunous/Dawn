package me.saket.dank.ui.user;

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import net.dean.jraw.models.Account;
import net.dean.jraw.oauth.AccountHelper;

import javax.inject.Inject;
import javax.inject.Named;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import me.saket.dank.R;
import me.saket.dank.reddit.Reddit;
import me.saket.dank.ui.authentication.LoginActivity;
import me.saket.dank.utils.Optional;
import me.saket.dank.utils.Preconditions;
import timber.log.Timber;


/**
 * TODO: Merge with {@link UserProfileRepository}.
 */
public class UserSessionRepository {

  private static final String KEY_LOGGED_IN_USERNAME = "logged_in_user_dank";
  private static final String KEY_ACCOUNTS = "accounts";
  private static final String EMPTY = "";

  private Lazy<Reddit> reddit;
  private final Preference<String> loggedInUsername;
  private final Preference<String> savedSessions;
  private ArrayList<String> accounts;

  @Inject AccountHelper accountHelper;

  @Inject
  public UserSessionRepository(Lazy<Reddit> reddit, @Named("user_session") RxSharedPreferences rxSharedPreferences) {
    this.reddit = reddit;
    loggedInUsername = rxSharedPreferences.getString(KEY_LOGGED_IN_USERNAME, EMPTY);
    savedSessions = rxSharedPreferences.getString(KEY_ACCOUNTS, EMPTY);
    this.accounts = new ArrayList<>();


    if (!TextUtils.isEmpty(savedSessions.get())) {
      try {
        JSONArray accountsJSON = new JSONArray(savedSessions.get());
        if (accountsJSON.length() > 0) {
          for (int i = 0; i < accountsJSON.length(); i++) {
            try {
                this.accounts.add(accountsJSON.get(i).toString());
            } catch (Exception e) {
              e.printStackTrace();
              continue;
            }

          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public void switchAccount(String username, Context ctx) {
     this.createNewSession(username, ctx);
  }

  private void createNewSession(String username, Context ctx) {
      Timber.i("creating new session");
      try {
          accountHelper.trySwitchToUser(username);
          loggedInUsername.set(username);
          Toast.makeText(ctx, ctx.getString(R.string.login_welcome_user, username), Toast.LENGTH_SHORT).show();
      } catch (Exception e) {
          Timber.e(e, "Error while switching users");
      }
  }

  public void setLoggedInUsername(String username) {
    Preconditions.checkNotNull(username, "username == null");

    if (!this.accounts.contains(username)) {
        this.accounts.add(username);

        JSONArray jsonArray = new JSONArray();
        for (String s : this.accounts) {
            jsonArray.put(s);
        }

        savedSessions.set(jsonArray.toString());
    }

    loggedInUsername.set(username);
  }

  public Completable logout() {
    return reddit.get()
        .loggedInUser()
        .logout()
        .andThen(Completable.fromAction(() -> removeLoggedInUsername()));
  }

  public void removeLoggedInUsername() {
    loggedInUsername.set(EMPTY);
  }

  public boolean isUserLoggedIn() {
    //noinspection ConstantConditions
    return loggedInUserName() != null && !loggedInUserName().equals(EMPTY);
  }

  @Nullable
  public String loggedInUserName() {
    return loggedInUsername.get();
  }

  public ArrayList<String> getAccounts() {
    return this.accounts;
  }

  /** Note: emits the current value immediately. */
  @CheckResult
  public Observable<Optional<UserSession>> streamSessions() {
    return loggedInUsername.asObservable()
        .map(username -> username.equals(EMPTY)
            ? Optional.empty()
            : Optional.of(UserSession.create(username))
        );
  }
}
