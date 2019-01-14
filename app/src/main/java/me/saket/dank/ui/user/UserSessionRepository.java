package me.saket.dank.ui.user;

import android.support.annotation.CheckResult;
import android.support.annotation.Nullable;

import com.f2prateek.rx.preferences2.Preference;
import com.f2prateek.rx.preferences2.RxSharedPreferences;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Lazy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import net.dean.jraw.models.SubredditSort;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.saket.dank.reddit.Reddit;
import me.saket.dank.reddit.Subreddit;
import me.saket.dank.ui.submission.SortingAndTimePeriod;
import me.saket.dank.utils.Optional;
import me.saket.dank.utils.Preconditions;

/**
 * TODO: Merge with {@link UserProfileRepository}.
 */
public class UserSessionRepository {

  private static final String KEY_LOGGED_IN_USERNAME = "logged_in_username_v0.6.1";
  private static final String KEY_SORTING_AND_PERIOD = "sorting_and_period_";
  private static final String EMPTY = "";

  private Lazy<Reddit> reddit;
  private final Preference<String> loggedInUsername;
  private final Preference<String> sortingAndPeriod;
  private HashMap<String, String> sortHashMap;

  @Inject
  public UserSessionRepository(Lazy<Reddit> reddit, @Named("user_session") RxSharedPreferences rxSharedPreferences) {
    this.reddit = reddit;
    loggedInUsername = rxSharedPreferences.getString(KEY_LOGGED_IN_USERNAME, EMPTY);
    sortingAndPeriod = rxSharedPreferences.getString(KEY_SORTING_AND_PERIOD, EMPTY);
    sortHashMap = new HashMap<>();
  }

  public void setLoggedInUsername(String username) {
    Preconditions.checkNotNull(username, "username == null");
    loggedInUsername.set(username);
    this.loadSortAndPeriod();
  }

  public Completable logout() {
    return reddit.get()
        .loggedInUser()
        .logout()
        .andThen(Completable.fromAction(() -> removeLoggedInUsername()));
  }

  public SortingAndTimePeriod getSortingAndTimePeriodForSub(String subredditName) {
    if (this.sortHashMap.isEmpty())
      this.loadSortAndPeriod();

    String sortPeriod = this.sortHashMap.get(subredditName);

    if (sortPeriod == null) {
      return null;
    } else {
      return SortingAndTimePeriod.create(sortPeriod);
    }
  }

  public void saveSortingAndTimePeriodForSub(String subredditName, SortingAndTimePeriod sortingTime){
    try {
      JSONArray arr = new JSONArray();

      this.sortHashMap.put(subredditName, sortingTime.serialize());

      for(String index : this.sortHashMap.keySet()) {
        JSONObject json = new JSONObject();
        json.put("subreddit", index);
        json.put("sort", this.sortHashMap.get(index));
        arr.put(json);
      }

      sortingAndPeriod.set(arr.toString());

    } catch (JSONException exception) {
      // Do something with exception
    }
  }

  public void removeLoggedInUsername() {
    loggedInUsername.set(EMPTY);
  }

  public boolean isUserLoggedIn() {
    //noinspection ConstantConditions
    return loggedInUserName() != null && !loggedInUserName().equals(EMPTY);
  }

  private void loadSortAndPeriod() {
    try {
      JSONArray arr = new JSONArray(sortingAndPeriod.get());

      for(int i = 0; i < arr.length(); i++) {
        JSONObject json = arr.getJSONObject(i);
        this.sortHashMap.put(json.getString("subreddit"), json.getString("sort"));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Nullable
  public String loggedInUserName() {
    return loggedInUsername.get();
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
