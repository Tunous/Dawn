package me.saket.dank.ui.usermanagement;

import com.google.auto.value.AutoValue;

import me.saket.dank.ui.usermanagement.UserManagementScreenUiModel;
import me.saket.dank.ui.usermanagement.UserManagementAdapter;
import me.saket.dank.ui.usermanagement.AutoValue_UserManagementPlaceholderUiModel;

@AutoValue
public abstract class UserManagementPlaceholderUiModel implements UserManagementScreenUiModel {

  @Override
  public long adapterId() {
    return UserManagementAdapter.ID_ADD_NEW;
  }

  public static UserManagementPlaceholderUiModel create() {
    return new AutoValue_UserManagementPlaceholderUiModel();
  }
}
