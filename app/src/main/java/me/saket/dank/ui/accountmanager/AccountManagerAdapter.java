package me.saket.dank.ui.accountmanager;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.Relay;
import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import java.util.List;
import javax.inject.Inject;

import me.saket.dank.R;
import me.saket.dank.utils.ItemTouchHelperDragAndDropCallback;
import me.saket.dank.utils.Pair;
import me.saket.dank.utils.RecyclerViewArrayAdapter;
import me.saket.dank.utils.lifecycle.LifecycleStreams;
import me.saket.dank.widgets.swipe.SwipeableLayout;
import me.saket.dank.widgets.swipe.ViewHolderWithSwipeActions;

public class AccountManagerAdapter extends RecyclerViewArrayAdapter<AccountManagerScreenUiModel, RecyclerView.ViewHolder>
    implements Consumer<Pair<List<AccountManagerScreenUiModel>, DiffUtil.DiffResult>>
{

  private static final Object NOTHING = LifecycleStreams.NOTHING;
  public static final long ID_ADD_NEW = -99L;
  private static final int VIEW_TYPE_APP_SHORTCUT = 0;
  private static final int VIEW_TYPE_PLACEHOLDER = 1;

  private final Lazy<AccountManagerSwipeActionsProvider> swipeActionsProvider;
  private final Relay<Object> addClicks = PublishRelay.create();
  private final Relay<AccountManagerViewHolder> dragStarts = PublishRelay.create();

  @Inject
  public AccountManagerAdapter(Lazy<AccountManagerSwipeActionsProvider> swipeActionsProvider) {
    this.swipeActionsProvider = swipeActionsProvider;
    setHasStableIds(true);
  }

  @CheckResult
  public Observable<AccountManager> streamDeleteClicks() {
    return swipeActionsProvider.get().deleteSwipeActions;
  }

  @CheckResult
  public Observable<AccountManager> streamSwitchClicks() {
    return swipeActionsProvider.get().switchSwipeActions;
  }

  @CheckResult
  public Observable<AccountManagerViewHolder> streamDragStarts() {
    return dragStarts;
  }

  @Override
  protected RecyclerView.ViewHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_APP_SHORTCUT) {
      AccountManagerViewHolder holder = AccountManagerViewHolder.create(inflater, parent);
      holder.setupDeleteGesture(swipeActionsProvider.get());
      holder.setupDragGesture(dragStarts);
      return holder;

    } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
      PlaceholderViewHolder holder = PlaceholderViewHolder.create(inflater, parent);
      holder.addButton.setOnClickListener(o -> addClicks.accept(NOTHING));
      return holder;

    } else {
      throw new IllegalArgumentException("Invalid view type");
    }
  }

  @Override
  public long getItemId(int position) {
    AccountManagerScreenUiModel uiModel = getItem(position);
    return uiModel.adapterId();
  }

  @Override
  public int getItemViewType(int position) {
    AccountManagerScreenUiModel uiModel = getItem(position);

    if (uiModel instanceof AccountManager) {
      return VIEW_TYPE_APP_SHORTCUT;

    } else if (uiModel instanceof AccountManagerPlaceholderUiModel) {
      return VIEW_TYPE_PLACEHOLDER;

    } else {
      throw new IllegalArgumentException("Invalid view type");
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof AccountManagerViewHolder) {
      final AccountManagerViewHolder viewHolder = (AccountManagerViewHolder) holder;
      viewHolder.set((AccountManager) getItem(position));
      viewHolder.render();
    }
  }

  @Override
  public void accept(Pair<List<AccountManagerScreenUiModel>, DiffUtil.DiffResult> pair) {
    updateData(pair.first());
    pair.second().dispatchUpdatesTo(this);
  }

  public Observable<Object> streamAddClicks() {
    return addClicks;
  }

  static class AccountManagerViewHolder extends RecyclerView.ViewHolder
      implements ViewHolderWithSwipeActions, ItemTouchHelperDragAndDropCallback.DraggableViewHolder
  {
    @BindView(R.id.account_manager_item_swipeable_layout) SwipeableLayout swipeableLayout;
    @BindView(R.id.account_manager_item_label) TextView labelView;
    @BindView(R.id.account_manager_item_drag) ImageButton dragButton;
    private final int recyclerViewDragnDropElevation;

    private AccountManager account;

    public AccountManagerViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
      recyclerViewDragnDropElevation = swipeableLayout.getResources().getDimensionPixelSize(R.dimen.elevation_recyclerview_row_drag_n_drop);
    }

    public static AccountManagerViewHolder create(LayoutInflater inflater, ViewGroup parent) {
      return new AccountManagerViewHolder(inflater.inflate(R.layout.list_item_account, parent, false));
    }

    @Override
    public SwipeableLayout getSwipeableLayout() {
      return swipeableLayout;
    }

    public void setupDeleteGesture(AccountManagerSwipeActionsProvider swipeActionsProvider) {
      swipeableLayout.setSwipeActionIconProvider(swipeActionsProvider.iconProvider());
      swipeableLayout.setSwipeActions(swipeActionsProvider.actions());
      swipeableLayout.setOnPerformSwipeActionListener((action, swipeDirection) ->
          swipeActionsProvider.performSwipeAction(action, account, swipeableLayout, swipeDirection)
      );
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupDragGesture(Relay<AccountManagerViewHolder> dragStarts) {
      dragButton.setOnTouchListener((v, touchEvent) -> {
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
          dragStarts.accept(this);
        }
        return dragButton.onTouchEvent(touchEvent);
      });
    }

    public void set(AccountManager account) {
      this.account = account;
    }

    public void render() {
      labelView.setText(labelView.getResources().getString(R.string.user_name_u_prefix, account.label()));
    }

    @Override
    public void onDragStart() {
      swipeableLayout.animate()
          .translationZ(recyclerViewDragnDropElevation)
          .setDuration(100)
          .start();
    }

    @Override
    public void onDragEnd() {
      swipeableLayout.animate()
          .translationZ(0)
          .setDuration(50)
          .start();
    }
  }

  static class PlaceholderViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.account_manager_placeholder_add) Button addButton;

    static PlaceholderViewHolder create(LayoutInflater inflater, ViewGroup parent) {
      return new PlaceholderViewHolder(inflater.inflate(R.layout.list_item_account_manager_placeholder, parent, false));
    }

    public PlaceholderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
