package me.saket.dank.ui.usermanagement;

import android.support.annotation.StringRes;

import com.jakewharton.rxrelay2.PublishRelay;
import timber.log.Timber;

import java.util.Timer;
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
 * Controls gesture actions on {@link UserManagement}.
 */
public class UserManagementSwipeActionsProvider {

  private static final @StringRes int ACTION_NAME_DELETE = R.string.user_management_swipe_action_delete;
  private static final @StringRes int ACTION_NAME_SWITCH = R.string.user_management_swipe_action_switch;

  private final SwipeActions swipeActions;
  private final SwipeActionIconProvider swipeActionIconProvider;
  public final PublishRelay<UserManagement> deleteSwipeActions = PublishRelay.create();
  public final PublishRelay<UserManagement> switchSwipeActions = PublishRelay.create();

  @Inject
  public UserManagementSwipeActionsProvider() {
    swipeActions = SwipeActions.builder()
        .startActions(SwipeActionsHolder.builder()
            .add(SwipeAction.create(ACTION_NAME_DELETE, R.color.user_management_swipe_delete, 0.3f))
            .build())
        .endActions(SwipeActionsHolder.builder()
            .add(SwipeAction.create(ACTION_NAME_SWITCH, R.color.user_management_swipe_switch, 1f))
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

  public void performSwipeAction(SwipeAction swipeAction, UserManagement user, SwipeableLayout swipeableLayout, SwipeDirection swipeDirection) {
    switch (swipeAction.labelRes()) {
      case ACTION_NAME_DELETE:
        deleteSwipeActions.accept(user);
        break;
      case ACTION_NAME_SWITCH:
        switchSwipeActions.accept(user);
        break;
      default:
        throw new AssertionError("Unknown swipe action: " + swipeAction);
    }
    swipeableLayout.playRippleAnimation(swipeAction, RippleType.REGISTER, swipeDirection);
  }
}