package me.saket.dank.ui.accountmanager;

import androidx.annotation.StringRes;

import com.jakewharton.rxrelay2.PublishRelay;
import timber.log.Timber;

import javax.inject.Inject;

import me.saket.dank.R;
import me.saket.dank.widgets.swipe.SwipeAction;
import me.saket.dank.widgets.swipe.SwipeActions;
import me.saket.dank.widgets.swipe.SwipeActionsHolder;
import me.saket.dank.widgets.swipe.SwipeDirection;
import me.saket.dank.widgets.swipe.SwipeTriggerRippleDrawable.RippleType;
import me.saket.dank.widgets.swipe.SwipeableLayout;
import me.saket.dank.widgets.swipe.SwipeableLayout.SwipeActionIconProvider;

/**
 * Controls gesture actions on {@link AccountManager}.
 */
public class AccountManagerSwipeActionsProvider {

  private static final @StringRes int ACTION_NAME_DELETE = R.string.account_manager_swipe_action_delete;
  private static final @StringRes int ACTION_NAME_SWITCH = R.string.account_manager_swipe_action_switch;

  private final SwipeActions swipeActions;
  private final SwipeActionIconProvider swipeActionIconProvider;
  public final PublishRelay<AccountManager> deleteSwipeActions = PublishRelay.create();
  public final PublishRelay<AccountManager> switchSwipeActions = PublishRelay.create();

  @Inject
  public AccountManagerSwipeActionsProvider() {
    swipeActions = SwipeActions.builder()
        .startActions(SwipeActionsHolder.builder()
            .add(SwipeAction.create(ACTION_NAME_DELETE, R.color.destructive_action_background, 0.3f))
            .build())
        .endActions(SwipeActionsHolder.builder()
            .add(SwipeAction.create(ACTION_NAME_SWITCH, R.color.swipeable_row_item_background, 1f))
            .build())
        .build();

    swipeActionIconProvider = createActionIconProvider();
  }

  public SwipeActions actions() {
    return swipeActions;
  }

  public SwipeActionIconProvider iconProvider() {
    return swipeActionIconProvider;
  }

  public SwipeActionIconProvider createActionIconProvider() {
    return (imageView, oldAction, newAction) -> {
      Timber.e(String.valueOf(newAction.labelRes()));
      switch (newAction.labelRes()) {
        case ACTION_NAME_SWITCH:
          imageView.setImageResource(R.drawable.ic_swap_horiz_20dp);
          break;

        case ACTION_NAME_DELETE:
          imageView.setImageResource(R.drawable.ic_delete_20dp);
          break;

        default:
          throw new UnsupportedOperationException("Unknown swipe action: " + newAction);
      }
    };
  }

  public void performSwipeAction(SwipeAction swipeAction, AccountManager account, SwipeableLayout swipeableLayout, SwipeDirection swipeDirection) {
    switch (swipeAction.labelRes()) {
      case ACTION_NAME_DELETE:
        deleteSwipeActions.accept(account);
        break;
      case ACTION_NAME_SWITCH:
        switchSwipeActions.accept(account);
        break;
      default:
        throw new AssertionError("Unknown swipe action: " + swipeAction);
    }
    swipeableLayout.playRippleAnimation(swipeAction, RippleType.REGISTER, swipeDirection);
  }
}
