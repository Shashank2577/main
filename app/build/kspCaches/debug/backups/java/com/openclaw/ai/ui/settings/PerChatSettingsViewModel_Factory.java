package com.openclaw.ai.ui.settings;

import com.openclaw.ai.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class PerChatSettingsViewModel_Factory implements Factory<PerChatSettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private PerChatSettingsViewModel_Factory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public PerChatSettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static PerChatSettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new PerChatSettingsViewModel_Factory(settingsRepositoryProvider);
  }

  public static PerChatSettingsViewModel newInstance(SettingsRepository settingsRepository) {
    return new PerChatSettingsViewModel(settingsRepository);
  }
}
