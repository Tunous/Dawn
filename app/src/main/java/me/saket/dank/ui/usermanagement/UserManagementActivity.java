package me.saket.dank.ui.usermanagement;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;
import static me.saket.dank.utils.RxUtils.applySchedulers;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewFlipper;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.deeplinkdispatch.DeepLink;
import dagger.Lazy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
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
import me.saket.dank.di.RootComponent;
import me.saket.dank.ui.DankActivity;
import me.saket.dank.ui.authentication.LoginActivity;
import me.saket.dank.ui.user.UserSessionRepository;
import me.saket.dank.ui.usermanagement.UserManagementAdapter.UserManagementViewHolder;
import me.saket.dank.ui.subscriptions.SubscriptionRepository;
import me.saket.dank.utils.ItemTouchHelperDragAndDropCallback;
import me.saket.dank.utils.RxDiffUtil;
import me.saket.dank.utils.itemanimators.SlideUpAlphaAnimator;
import me.saket.dank.widgets.swipe.RecyclerSwipeListener;

@DeepLink(UserManagementActivity.DEEP_LINK)
@RequiresApi(Build.VERSION_CODES.N_MR1)
public class UserManagementActivity extends DankActivity {
  public static final String DEEP_LINK = "dank://userManagement";

  private final int ACTION_DELETE = 1;
  private final int ACTION_LOGOUT = 2;
  private int ACTION = 0;

  private UserManagement selectedAccount = null;

  @BindView(R.id.user_management_root) ViewGroup rootViewGroup;
  @BindView(R.id.user_management_users_recyclerview) RecyclerView usersRecyclerView;
  @BindView(R.id.user_management_logout) Button logoutButton;
  @BindView(R.id.accounts_progress) View fullscreenProgressView;

  @Inject Lazy<UserSessionRepository> userSessionRepository;
  @Inject Lazy<SubscriptionRepository> subscriptionRepository;
  @Inject Lazy<UserManagementRepository> userRepository;
  @Inject Lazy<UserManagementAdapter> usersAdapter;
  @Inject Lazy<ErrorResolver> errorResolver;

  private Disposable confirmTimer = Disposables.disposed();
  private Disposable timerDisposable = Disposables.empty();

  public static Intent intent(Context context) {
    return new Intent(context, UserManagementActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    Dank.dependencyInjector().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_user_management);
    ButterKnife.bind(this);

    if (!userSessionRepository.get().isUserLoggedIn()) {
      logoutButton.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedState) {
    super.onPostCreate(savedState);

    setupUserList();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  @OnClick(R.id.user_management_done)
  public void finish() {
    timerDisposable.dispose();
    super.finish();
  }

  @OnClick(R.id.user_management_logout)
  public void logout() {
    confirmAction(ACTION_LOGOUT);
  }

  private Completable queueToDelete(UserManagement user){
    this.selectedAccount = user;
    return this.confirmAction(ACTION_DELETE);
  }

  private Completable confirmAction(int action) {
    if (confirmTimer.isDisposed()) {
      ACTION = action;
      int confirmText = ACTION_LOGOUT == ACTION ? R.string.userprofile_confirm_logout : R.string.userprofile_confirm_delete;

      runOnUiThread(() -> {
        // Stuff that updates the UI
        logoutButton.setText(confirmText);
        logoutButton.setVisibility(View.VISIBLE);
      });


      confirmTimer = Observable.timer(5, TimeUnit.SECONDS)
          .compose(applySchedulers())
          .subscribe(o -> {
            runOnUiThread(() -> {
              // Stuff that updates the UI
              if (userSessionRepository.get().isUserLoggedIn()) {
                logoutButton.setText(R.string.login_logout);
                logoutButton.setVisibility(View.VISIBLE);
              } else {
                logoutButton.setText("");
                logoutButton.setVisibility(View.INVISIBLE);
              }
            });
          });

    } else {
      // Confirm logout/delete was visible when this button was clicked. Perform the action.
      confirmTimer.dispose();
      timerDisposable.dispose();

      int ongoingActionText = ACTION_LOGOUT == ACTION ? R.string.userprofile_logging_out : R.string.userprofile_deleting_account;
      runOnUiThread(() -> {

        // Stuff that updates the UI
        logoutButton.setText(ongoingActionText);
        logoutButton.setVisibility(View.VISIBLE);
      });

      Thread thread = new Thread()
      {
        @Override
        public void run() {
          try {
            while(true) {
              sleep(2000);
              runOnUiThread(() -> {
                if (userSessionRepository.get().isUserLoggedIn()) {
                  logoutButton.setText(R.string.login_logout);
                  logoutButton.setVisibility(View.VISIBLE);
                } else {
                  logoutButton.setText("");
                  logoutButton.setVisibility(View.INVISIBLE);
                }
              });
            }
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      };

      if (ACTION == ACTION_DELETE) {
        timerDisposable = userRepository.get().delete(this.selectedAccount)
            .subscribeOn(io())
            .observeOn(mainThread())
            .subscribe(
                () -> {
                  //this.userSessionRepository.get().switchAccount(null, getApplicationContext());
                  thread.start();
                },
                error -> {
                  ResolvedError resolvedError = errorResolver.get().resolve(error);
                  resolvedError.ifUnknown(() -> Timber.e(error, "Delete failure"));
                }
            );
      } else {
        timerDisposable = userSessionRepository.get().logout()
            .subscribeOn(io())
            .observeOn(mainThread())
            .subscribe(
                () -> {
                  //this.userSessionRepository.get().switchAccount(null, getApplicationContext());
                  thread.start();
                },
                error -> {
                  ResolvedError resolvedError = errorResolver.get().resolve(error);
                  resolvedError.ifUnknown(() -> Timber.e(error, "Logout failure"));
                }
            );
      }
    }

    return Completable.complete();
  }

  private void setupUserList() {
    SlideUpAlphaAnimator animator = SlideUpAlphaAnimator.create();
    animator.setSupportsChangeAnimations(false);
    usersRecyclerView.setItemAnimator(animator);
    usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    usersRecyclerView.setAdapter(usersAdapter.get());

    fullscreenProgressView.setVisibility(View.VISIBLE);

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
          fullscreenProgressView.setVisibility(View.GONE);
          return uiModels;
        })
        .compose(RxDiffUtil.calculateDiff(UserManagementUiModelDiffer::create))
        .observeOn(mainThread())
        .takeUntil(lifecycle().onDestroyFlowable())
        .subscribe(x -> usersAdapter.get());

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
        .flatMapCompletable(userToDelete -> queueToDelete(userToDelete))
        .ambWith(lifecycle().onDestroyCompletable())
        .subscribe();

    // Switches.
    usersAdapter.get().streamSwitchClicks()
        .observeOn(io())
        .flatMapCompletable(userToSwitch -> this.userSessionRepository.get().switchAccount(userToSwitch.label(), getBaseContext()))
        .ambWith(lifecycle().onDestroyCompletable())
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
          UserManagement user = user_management.get(i);
          userRepository.get().add(user.withRank(i))
              .subscribeOn(io())
              .subscribe();
        }
        return true;
      }
    };
  }
}
