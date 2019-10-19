package me.saket.dank.ui.accountmanager;

import com.google.auto.value.AutoValue;

import me.saket.dank.ui.accountmanager.AccountManagerScreenUiModel;
import me.saket.dank.ui.accountmanager.AccountManagerAdapter;
import me.saket.dank.ui.accountmanager.AutoValue_AccountManagerPlaceholderUiModel;

@AutoValue
public abstract class AccountManagerPlaceholderUiModel implements AccountManagerScreenUiModel {

  @Override
  public long adapterId() {
    return AccountManagerAdapter.ID_ADD_NEW;
  }

  public static AccountManagerPlaceholderUiModel create() {
    return new AutoValue_AccountManagerPlaceholderUiModel();
  }
}
