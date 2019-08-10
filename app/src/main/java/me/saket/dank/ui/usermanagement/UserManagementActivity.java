package me.saket.dank.ui.usermanagement;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;
import static me.saket.dank.utils.RxUtils.applySchedulers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ViewFlipper;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.deeplinkdispatch.DeepLink;
import com.jakewharton.rxbinding2.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import dagger.Lazy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import net.dean.jraw.oauth.AccountHelper;
import timber.log.Timber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import me.saket.dank.R;
import me.saket.dank.data.ErrorResolver;
import me.saket.dank.data.ResolvedError;
import me.saket.dank.di.Dank;
import me.saket.dank.ui.DankActivity;
import me.saket.dank.ui.authentication.LoginActivity;
import me.saket.dank.ui.user.UserSessionRepository;
import me.saket.dank.ui.usermanagement.UserManagement;
import me.saket.dank.ui.usermanagement.UserManagementPlaceholderUiModel;
import me.saket.dank.ui.usermanagement.UserManagementRepository;
import me.saket.dank.ui.usermanagement.UserManagementScreenUiModel;
import me.saket.dank.ui.usermanagement.UserManagementAdapter;
import me.saket.dank.ui.usermanagement.UserManagementAdapter.UserManagementViewHolder;
import me.saket.dank.ui.usermanagement.UserManagementUiModelDiffer;
import me.saket.dank.ui.subscriptions.SubredditAdapter;
import me.saket.dank.ui.subscriptions.SubredditFlexboxLayoutManager;
import me.saket.dank.ui.subscriptions.SubredditSubscription;
import me.saket.dank.ui.subscriptions.SubscriptionRepository;
import me.saket.dank.utils.Animations;
import me.saket.dank.utils.ItemTouchHelperDragAndDropCallback;
import me.saket.dank.utils.Keyboards;
import me.saket.dank.utils.Optional;
import me.saket.dank.utils.Pair;
import me.saket.dank.utils.RxDiffUtil;
import me.saket.dank.utils.RxUtils;
import me.saket.dank.utils.itemanimators.SlideUpAlphaAnimator;
import me.saket.dank.widgets.swipe.RecyclerSwipeListener;

@DeepLink(UserManagementActivity.DEEP_LINK)
@RequiresApi(Build.VERSION_CODES.N_MR1)
public class UserManagementActivity extends DankActivity {
  private static final String KEY_VISIBLE_SCREEN = "visibleScreen";
  public static final String DEEP_LINK = "dank://userManagement";

  @BindView(R.id.user_management_root) ViewGroup rootViewGroup;
  @BindView(R.id.user_management_content_flipper) ViewFlipper contentViewFlipper;
  @BindView(R.id.user_management_users_recyclerview) RecyclerView usersRecyclerView;
  @BindView(R.id.user_management_logout) Button logoutButton;

  @Inject Lazy<UserSessionRepository> userSessionRepository;
  @Inject Lazy<SubscriptionRepository> subscriptionRepository;
  @Inject Lazy<UserManagementRepository> userRepository;
  @Inject Lazy<UserManagementAdapter> usersAdapter;
  @Inject Lazy<ErrorResolver> errorResolver;

  @BindInt(R.integer.submissionoptions_animation_duration) int pageChangeAnimDuration;

  private Disposable confirmLogoutTimer = Disposables.disposed();
  private Disposable logoutDisposable = Disposables.empty();
  private final BehaviorRelay<Screen> screenChanges = BehaviorRelay.create();

  private enum Screen {
    SHORTCUTS(R.id.user_management_flipper_users_screen),
    ADD_NEW_ACCOUNT(R.id.login_webview);

    private final int viewId;

    Screen(@IdRes int viewId) {
      this.viewId = viewId;
    }
  }

  public static Intent intent(Context context) {
    return new Intent(context, UserManagementActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    Dank.dependencyInjector().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_management);
    ButterKnife.bind(this);

    contentViewFlipper.setClipToOutline(true);

    if (!userSessionRepository.get().isUserLoggedIn()) {
      logoutButton.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedState) {
    super.onPostCreate(savedState);

    screenChanges.accept(Optional.ofNullable(savedState)
        .map(state -> (Screen) state.getSerializable(KEY_VISIBLE_SCREEN))
        .orElse(Screen.SHORTCUTS));

    setupUserList();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(KEY_VISIBLE_SCREEN, screenChanges.getValue());
  }

  @Override
  @OnClick(R.id.user_management_done)
  public void finish() {
    logoutDisposable.dispose();
    super.finish();
  }

  @OnClick(R.id.user_management_logout)
  public void logout() {

    if (confirmLogoutTimer.isDisposed()) {
      logoutButton.setText(R.string.userprofile_confirm_logout);
      confirmLogoutTimer = Observable.timer(5, TimeUnit.SECONDS)
          .compose(applySchedulers())
          .subscribe(o -> {
            logoutButton.setText(R.string.login_logout);
            logoutButton.setVisibility(View.VISIBLE);
          });
    } else {
      // Confirm logout was visible when this button was clicked. Logout the user for real.
      confirmLogoutTimer.dispose();
      logoutDisposable.dispose();
      logoutButton.setText(R.string.userprofile_logging_out);

      logoutDisposable = userSessionRepository.get().logout()
          .subscribeOn(io())
          .observeOn(mainThread())
          .subscribe(
              () -> {
                this.userSessionRepository.get().switchAccount(null, getApplicationContext());
                logoutButton.setVisibility(View.INVISIBLE);
              },
              error -> {
                logoutButton.setText(R.string.login_logout);

                ResolvedError resolvedError = errorResolver.get().resolve(error);
                resolvedError.ifUnknown(() -> Timber.e(error, "Logout failure"));
              }
          );
    }
  }

  private void setupUserList() {
    SlideUpAlphaAnimator animator = SlideUpAlphaAnimator.create();
    animator.setSupportsChangeAnimations(false);
    usersRecyclerView.setItemAnimator(animator);
    usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    usersRecyclerView.setAdapter(usersAdapter.get());

    Observable<List<UserManagement>> storedUsers = userRepository.get().users()
        .subscribeOn(io())
        .replay()
        .refCount();

    // Adapter data-set.
    storedUsers
        .toFlowable(BackpressureStrategy.LATEST)
        .map(users -> {
          List<UserManagementScreenUiModel> uiModels = new ArrayList<>(users.size() + 1);
          // add a "add account" before listing the others
          uiModels.add(UserManagementPlaceholderUiModel.create());
          uiModels.addAll(users);
          return uiModels;
        })
        .compose(RxDiffUtil.calculateDiff(UserManagementUiModelDiffer::create))
        .observeOn(mainThread())
        .takeUntil(lifecycle().onDestroyFlowable())
        .subscribe(usersAdapter.get());

    // Add new.
    usersAdapter.get().streamAddClicks()
        .takeUntil(lifecycle().onDestroy())
        .subscribe(o -> startActivity(LoginActivity.intent(this)));

    // Drags.
    ItemTouchHelper dragHelper = new ItemTouchHelper(createDragAndDropCallbacks());
    dragHelper.attachToRecyclerView(usersRecyclerView);
    usersAdapter.get().streamDragStarts()
        .takeUntil(lifecycle().onDestroy())
        .subscribe(viewHolder -> dragHelper.startDrag(viewHolder));

    // Deletes.
    // WARNING: THIS TOUCH LISTENER FOR SWIPE SHOULD BE REGISTERED AFTER DRAG-DROP LISTENER.
    // Drag-n-drop's long-press listener does not get canceled if a row is being swiped.
    usersRecyclerView.addOnItemTouchListener(new RecyclerSwipeListener(usersRecyclerView));
    usersAdapter.get().streamDeleteClicks()
        .observeOn(io())
        .flatMapCompletable(shortcutToDelete -> userRepository.get().delete(shortcutToDelete))
        .ambWith(lifecycle().onDestroyCompletable())
        .subscribe();

    // Switches.
    usersAdapter.get().streamSwitchClicks()
        .observeOn(io())
        .flatMapCompletable(userToSwitch -> this.userSessionRepository.get().switchAccount(userToSwitch.label(), getApplicationContext()))
        .subscribe();

    // Dismiss on outside click.
    rootViewGroup.setOnClickListener(o -> finish());
  }

  private ItemTouchHelperDragAndDropCallback createDragAndDropCallbacks() {
    return new ItemTouchHelperDragAndDropCallback() {
      @Override
      protected boolean onItemMove(ViewHolder source, ViewHolder target) {
        UserManagementViewHolder sourceViewHolder = (UserManagementViewHolder) source;
        UserManagementViewHolder targetViewHolder = (UserManagementViewHolder) target;

        int fromPosition = sourceViewHolder.getAdapterPosition();
        int toPosition = targetViewHolder.getAdapterPosition();

        //noinspection ConstantConditions
        List<UserManagement> user_management = Observable.fromIterable(usersAdapter.get().getData())
            .ofType(UserManagement.class)
            .toList()
            .blockingGet();

        if (fromPosition < toPosition) {
          for (int i = fromPosition; i < toPosition; i++) {
            Collections.swap(user_management, i, i + 1);
          }
        } else {
          for (int i = fromPosition; i > toPosition; i--) {
            Collections.swap(user_management, i, i - 1);
          }
        }

        for (int i = 0; i < user_management.size(); i++) {
          UserManagement shortcut = user_management.get(i);
          userRepository.get().add(shortcut.withRank(i))
              .subscribeOn(io())
              .subscribe();
        }
        return true;
      }
    };
  }

  /**
   * Show user's search term in the results unless an exact match was found.
   */
  private List<SubredditSubscription> addSearchTermIfMatchNotFound(Pair<List<SubredditSubscription>, String> pair) {
    List<SubredditSubscription> filteredSubs = pair.first();
    String searchTerm = pair.second();

    if (!searchTerm.isEmpty()) {
      boolean exactSearchFound = false;
      for (SubredditSubscription filteredSub : filteredSubs) {
        if (filteredSub.name().equalsIgnoreCase(searchTerm)) {
          exactSearchFound = true;
          break;
        }
      }

      if (!exactSearchFound) {
        ArrayList<SubredditSubscription> filteredSubsWithQuery = new ArrayList<>(filteredSubs.size() + 1);
        filteredSubsWithQuery.addAll(filteredSubs);
        filteredSubsWithQuery.add(SubredditSubscription.create(searchTerm, SubredditSubscription.PendingState.NONE, false));
        return Collections.unmodifiableList(filteredSubsWithQuery);
      }
    }
    return filteredSubs;
  }
}
