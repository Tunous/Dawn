package me.saket.dank.ui.usermanagement;

import android.annotation.SuppressLint;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

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
import me.saket.dank.ui.usermanagement.UserManagement;
import me.saket.dank.ui.usermanagement.UserManagementPlaceholderUiModel;
import me.saket.dank.ui.usermanagement.UserManagementScreenUiModel;
import me.saket.dank.ui.usermanagement.UserManagementSwipeActionsProvider;
import me.saket.dank.utils.ItemTouchHelperDragAndDropCallback;
import me.saket.dank.utils.Pair;
import me.saket.dank.utils.RecyclerViewArrayAdapter;
import me.saket.dank.utils.lifecycle.LifecycleStreams;
import me.saket.dank.widgets.swipe.SwipeableLayout;
import me.saket.dank.widgets.swipe.ViewHolderWithSwipeActions;

public class UserManagementAdapter extends RecyclerViewArrayAdapter<UserManagementScreenUiModel, RecyclerView.ViewHolder>
    implements Consumer<Pair<List<UserManagementScreenUiModel>, DiffUtil.DiffResult>>
{

  private static final Object NOTHING = LifecycleStreams.NOTHING;
  public static final long ID_ADD_NEW = -99L;
  private static final int VIEW_TYPE_APP_SHORTCUT = 0;
  private static final int VIEW_TYPE_PLACEHOLDER = 1;

  private final Lazy<UserManagementSwipeActionsProvider> swipeActionsProvider;
  private final Relay<Object> addClicks = PublishRelay.create();
  private final Relay<UserManagementViewHolder> dragStarts = PublishRelay.create();

  @Inject
  public UserManagementAdapter(Lazy<UserManagementSwipeActionsProvider> swipeActionsProvider) {
    this.swipeActionsProvider = swipeActionsProvider;
    setHasStableIds(true);
  }

  @CheckResult
  public Observable<UserManagement> streamDeleteClicks() {
    return swipeActionsProvider.get().deleteSwipeActions;
  }

  @CheckResult
  public Observable<UserManagement> streamSwitchClicks() {
    return swipeActionsProvider.get().switchSwipeActions;
  }

  @CheckResult
  public Observable<UserManagementViewHolder> streamDragStarts() {
    return dragStarts;
  }

  @Override
  protected RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent, int viewType) {
    if (viewType == VIEW_TYPE_APP_SHORTCUT) {
      UserManagementViewHolder holder = UserManagementViewHolder.create(inflater, parent);
      holder.setupDeleteGesture(swipeActionsProvider.get());
      holder.setupDragGesture(dragStarts);
      return holder;

    } else if (viewType == VIEW_TYPE_PLACEHOLDER) {
      PlaceholderViewHolder holder = PlaceholderViewHolder.create(inflater, parent);
      holder.addButton.setOnClickListener(o -> addClicks.accept(NOTHING));
      return holder;

    } else {
      throw new AssertionError();
    }
  }

  @Override
  public long getItemId(int position) {
    UserManagementScreenUiModel uiModel = getItem(position);
    return uiModel.adapterId();
  }

  @Override
  public int getItemViewType(int position) {
    UserManagementScreenUiModel uiModel = getItem(position);

    if (uiModel instanceof UserManagement) {
      return VIEW_TYPE_APP_SHORTCUT;

    } else if (uiModel instanceof UserManagementPlaceholderUiModel) {
      return VIEW_TYPE_PLACEHOLDER;

    } else {
      throw new AssertionError();
    }
  }

  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder instanceof UserManagementViewHolder) {
      ((UserManagementViewHolder) holder).set((UserManagement) getItem(position));
      ((UserManagementViewHolder) holder).render();
    }
  }

  @Override
  public void accept(Pair<List<UserManagementScreenUiModel>, DiffUtil.DiffResult> pair) {
    updateData(pair.first());
    pair.second().dispatchUpdatesTo(this);
  }

  public Observable<Object> streamAddClicks() {
    return addClicks;
  }

  static class UserManagementViewHolder extends RecyclerView.ViewHolder
      implements ViewHolderWithSwipeActions, ItemTouchHelperDragAndDropCallback.DraggableViewHolder
  {
    @BindView(R.id.user_management_item_swipeable_layout) SwipeableLayout swipeableLayout;
    @BindView(R.id.user_management_item_label) TextView labelView;
    @BindView(R.id.user_management_item_drag) ImageButton dragButton;

    private UserManagement user;

    public UserManagementViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public static UserManagementViewHolder create(LayoutInflater inflater, ViewGroup parent) {
      return new UserManagementViewHolder(inflater.inflate(R.layout.list_item_user, parent, false));
    }

    @Override
    public SwipeableLayout getSwipeableLayout() {
      return swipeableLayout;
    }

    public void setupDeleteGesture(UserManagementSwipeActionsProvider swipeActionsProvider) {
      swipeableLayout.setSwipeActionIconProvider(swipeActionsProvider.iconProvider());
      swipeableLayout.setSwipeActions(swipeActionsProvider.actions());
      swipeableLayout.setOnPerformSwipeActionListener((action, swipeDirection) ->
          swipeActionsProvider.performSwipeAction(action, user, swipeableLayout, swipeDirection)
      );
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupDragGesture(Relay<UserManagementViewHolder> dragStarts) {
      dragButton.setOnTouchListener((v, touchEvent) -> {
        if (touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
          dragStarts.accept(this);
        }
        return dragButton.onTouchEvent(touchEvent);
      });
    }

    public void set(UserManagement user) {
      this.user = user;
    }

    public void render() {
      labelView.setText(labelView.getResources().getString(R.string.user_name_u_prefix, user.label()));
    }

    @Override
    public void onDragStart() {
      swipeableLayout.animate()
          .translationZ(swipeableLayout.getResources().getDimensionPixelSize(R.dimen.elevation_recyclerview_row_drag_n_drop))
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
    @BindView(R.id.user_management_placeholder_add) Button addButton;

    static PlaceholderViewHolder create(LayoutInflater inflater, ViewGroup parent) {
      return new PlaceholderViewHolder(inflater.inflate(R.layout.list_item_user_management_placeholder, parent, false));
    }

    public PlaceholderViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
