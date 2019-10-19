package me.saket.dank.ui.accountmanager;

import java.util.List;

import me.saket.dank.ui.accountmanager.AccountManagerScreenUiModel;
import me.saket.dank.utils.SimpleDiffUtilsCallbacks;

public class AccountManagerUiModelDiffer extends SimpleDiffUtilsCallbacks<AccountManagerScreenUiModel> {

  public static AccountManagerUiModelDiffer create(List<AccountManagerScreenUiModel> oldModels, List<AccountManagerScreenUiModel> newModels) {
    return new AccountManagerUiModelDiffer(oldModels, newModels);
  }

  private AccountManagerUiModelDiffer(List<AccountManagerScreenUiModel> oldModels, List<AccountManagerScreenUiModel> newModels) {
    super(oldModels, newModels);
  }

  @Override
  public boolean areItemsTheSame(AccountManagerScreenUiModel oldModel, AccountManagerScreenUiModel newModel) {
    return oldModel.adapterId() == newModel.adapterId();
  }

  @Override
  protected boolean areContentsTheSame(AccountManagerScreenUiModel oldModel, AccountManagerScreenUiModel newModel) {
    return oldModel.equals(newModel);
  }
}
