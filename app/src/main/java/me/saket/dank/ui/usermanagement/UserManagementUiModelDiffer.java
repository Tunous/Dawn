package me.saket.dank.ui.usermanagement;

import java.util.List;

import me.saket.dank.ui.usermanagement.UserManagementScreenUiModel;
import me.saket.dank.utils.SimpleDiffUtilsCallbacks;

public class UserManagementUiModelDiffer extends SimpleDiffUtilsCallbacks<UserManagementScreenUiModel> {

  public static UserManagementUiModelDiffer create(List<UserManagementScreenUiModel> oldModels, List<UserManagementScreenUiModel> newModels) {
    return new UserManagementUiModelDiffer(oldModels, newModels);
  }

  private UserManagementUiModelDiffer(List<UserManagementScreenUiModel> oldModels, List<UserManagementScreenUiModel> newModels) {
    super(oldModels, newModels);
  }

  @Override
  public boolean areItemsTheSame(UserManagementScreenUiModel oldModel, UserManagementScreenUiModel newModel) {
    return oldModel.adapterId() == newModel.adapterId();
  }

  @Override
  protected boolean areContentsTheSame(UserManagementScreenUiModel oldModel, UserManagementScreenUiModel newModel) {
    return oldModel.equals(newModel);
  }
}
