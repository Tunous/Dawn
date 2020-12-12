package me.saket.dank.ui.accountmanager;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.airbnb.deeplinkdispatch.DeepLink;
import dagger.Lazy;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
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
import me.saket.dank.ui.DankActivity;
import me.saket.dank.ui.authentication.LoginActivity;
import me.saket.dank.ui.user.UserSessionRepository;
import me.saket.dank.ui.accountmanager.AccountManagerAdapter.AccountManagerViewHolder;
import me.saket.dank.ui.subscriptions.SubscriptionRepository;
import me.saket.dank.utils.ItemTouchHelperDragAndDropCallback;
import me.saket.dank.utils.RxDiffUtil;
import me.saket.dank.utils.itemanimators.SlideUpAlphaAnimator;
import me.saket.dank.widgets.swipe.RecyclerSwipeListener;

@DeepLink(AccountManagerActivity.DEEP_LINK)
public class AccountManagerActivity extends DankActivity {
  public static final String DEEP_LINK = "dawn://accountManager";

  private final int ACTION_DELETE = 1;
  private final int ACTION_LOGOUT = 2;

  private AccountManager selectedAccount = null;

  @BindView(R.id.account_manager_root) ViewGroup rootViewGroup;
  @BindView(R.id.account_manager_accounts_recyclerview) RecyclerView accountsRecyclerView;
  @BindView(R.id.account_manager_logout) Button logoutButton;
  @BindView(R.id.account_progressbar) View loadingProgessbar;

  @Inject Lazy<UserSessionRepository> userSessionRepository;
  @Inject Lazy<SubscriptionRepository> subscriptionRepository;
  @Inject Lazy<AccountManagerRepository> accountManagerRepository;
  @Inject Lazy<AccountManagerAdapter> accountManagerAdapter;
  @Inject Lazy<ErrorResolver> errorResolver;

  private Disposable confirmTimer = Disposables.disposed();
  private Disposable timerDisposable = Disposables.empty();

  @CheckResult
  public static Intent intent(Context context) {
    return new Intent(context, AccountManagerActivity.class);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    Dank.dependencyInjector().inject(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_account_manager);
    ButterKnife.bind(this);
    setupUserList();
  }

  @Override
  @OnClick(R.id.account_manager_done)
  public void finish() {
    timerDisposable.dispose();
    super.finish();
  }

  @OnClick(R.id.account_manager_logout)
  public void logout() {
    confirmAction(ACTION_LOGOUT);
  }

  private Completable queueToDelete(AccountManager account){
    this.selectedAccount = account;
    return this.confirmAction(ACTION_DELETE);
  }

  private Completable confirmAction(int action) {
    if (confirmTimer.isDisposed()) {
      int confirmText = ACTION_LOGOUT == action ? R.string.userprofile_confirm_logout : R.string.userprofile_confirm_delete;

      runOnUiThread(() -> {
        // Stuff that updates the UI
        logoutButton.setText(confirmText);
        logoutButton.setVisibility(View.VISIBLE);
      });

      confirmTimer = Observable.timer(5, TimeUnit.SECONDS)
          .subscribeOn(AndroidSchedulers.mainThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(o -> {
            // Stuff that updates the UI
            if (userSessionRepository.get().isUserLoggedIn()) {
              logoutButton.setText(R.string.login_logout);
              logoutButton.setVisibility(View.VISIBLE);
            } else {
              logoutButton.setText("");
              logoutButton.setVisibility(View.INVISIBLE);
            }
          });

    } else {
      // Confirm logout/delete was visible when this button was clicked. Perform the action.
      confirmTimer.dispose();
      timerDisposable.dispose();

      int ongoingActionText = ACTION_LOGOUT == action ? R.string.userprofile_logging_out : R.string.userprofile_deleting_account;
      runOnUiThread(() -> {

        // Stuff that updates the UI
        logoutButton.setText(ongoingActionText);
        logoutButton.setVisibility(View.VISIBLE);
      });

      if (action == ACTION_DELETE) {
        //this.userSessionRepository.get().switchAccount(null, getApplicationContext());
        timerDisposable = accountManagerRepository.get().delete(selectedAccount)
            .subscribeOn(io())
            .observeOn(mainThread())
            .delay(2, TimeUnit.SECONDS)
            .subscribe(() -> {
              if (userSessionRepository.get().isUserLoggedIn()) {
                logoutButton.setText(R.string.login_logout);
                logoutButton.setVisibility(View.VISIBLE);
              } else {
                logoutButton.setText("");
                logoutButton.setVisibility(View.INVISIBLE);
              }
              },
                error -> {
                  ResolvedError resolvedError = errorResolver.get().resolve(error);
                  resolvedError.ifUnknown(() -> Timber.e(error, "Delete failure"));
                }
            );
      } else {
        //this.userSessionRepository.get().switchAccount(null, getApplicationContext());
        timerDisposable = userSessionRepository.get().logout()
            .subscribeOn(io())
            .observeOn(mainThread())
            .delay(2, TimeUnit.SECONDS)
            .subscribe(() -> {
              if (userSessionRepository.get().isUserLoggedIn()) {
                logoutButton.setText(R.string.login_logout);
                logoutButton.setVisibility(View.VISIBLE);
              } else {
                logoutButton.setText("");
                logoutButton.setVisibility(View.INVISIBLE);
              }
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
    accountsRecyclerView.setItemAnimator(animator);
    accountsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    accountsRecyclerView.setAdapter(accountManagerAdapter.get());

    Observable<List<AccountManager>> storedUsers = accountManagerRepository.get().accounts()
        .subscribeOn(io())
        .replay()
        .refCount();

    // Adapter data-set.
    storedUsers
        .toFlowable(BackpressureStrategy.LATEST)
        .map(accounts -> {
          List<AccountManagerScreenUiModel> uiModels = new ArrayList<>(accounts.size() + 1);

          uiModels.add(AccountManagerPlaceholderUiModel.create());
          uiModels.addAll(accounts);

          return uiModels;
        })
        .compose(RxDiffUtil.calculateDiff(AccountManagerUiModelDiffer::create))
        .observeOn(mainThread())
        .doOnSubscribe(d -> loadingProgessbar.setVisibility(View.GONE))
        .takeUntil(lifecycle().onDestroyFlowable())
        .subscribe(accountManagerAdapter.get());

    // Add new.
    accountManagerAdapter.get().streamAddClicks()
        .takeUntil(lifecycle().onDestroy())
        .subscribe(o -> startActivity(LoginActivity.intent(this)));

    // Drags.
    ItemTouchHelper dragHelper = new ItemTouchHelper(createDragAndDropCallbacks());
    dragHelper.attachToRecyclerView(accountsRecyclerView);
    accountManagerAdapter.get().streamDragStarts()
        .takeUntil(lifecycle().onDestroy())
        .subscribe(viewHolder -> dragHelper.startDrag(viewHolder));

    // Deletes.
    // WARNING: THIS TOUCH LISTENER FOR SWIPE SHOULD BE REGISTERED AFTER DRAG-DROP LISTENER.
    // Drag-n-drop's long-press listener does not get canceled if a row is being swiped.
    accountsRecyclerView.addOnItemTouchListener(new RecyclerSwipeListener(accountsRecyclerView));
    accountManagerAdapter.get().streamDeleteClicks()
        .observeOn(io())
        .flatMapCompletable(userToDelete -> queueToDelete(userToDelete))
        .ambWith(lifecycle().onDestroyCompletable())
        .subscribe();

    // Switches.
    accountManagerAdapter.get().streamSwitchClicks()
        .observeOn(io())
        .flatMapCompletable(userToSwitch -> this.userSessionRepository.get().switchAccount(userToSwitch.label()))
        .ambWith(lifecycle().onDestroyCompletable())
        .subscribe();

    // Dismiss on outside click.
    rootViewGroup.setOnClickListener(o -> finish());
  }

  private ItemTouchHelperDragAndDropCallback createDragAndDropCallbacks() {
    return new ItemTouchHelperDragAndDropCallback() {
      @Override
      protected boolean onItemMove(@NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target) {
        AccountManagerViewHolder sourceViewHolder = (AccountManagerViewHolder) source;
        AccountManagerViewHolder targetViewHolder = (AccountManagerViewHolder) target;

        int fromPosition = sourceViewHolder.getAdapterPosition() - 1; // "Add Account placeholder will add 1 to Index
        int toPosition = targetViewHolder.getAdapterPosition() - 1;

        //noinspection ConstantConditions
        List<AccountManager> accounts = Observable.fromIterable(accountManagerAdapter.get().getData())
            .ofType(AccountManager.class)
            .toList()
            .blockingGet();

        if (fromPosition < toPosition) {
          for (int i = fromPosition; i < toPosition; i++) {
            Collections.swap(accounts, i, i + 1);
          }
        } else {
          for (int i = fromPosition; i > toPosition; i--) {
            Collections.swap(accounts, i, i - 1);
          }
        }

        for (int i = 0; i < accounts.size(); i++) {
          AccountManager account  = accounts.get(i);
          accountManagerRepository.get().add(account.withRank(i))
              .subscribeOn(io())
              .subscribe();
        }
        return true;
      }
    };
  }
}
