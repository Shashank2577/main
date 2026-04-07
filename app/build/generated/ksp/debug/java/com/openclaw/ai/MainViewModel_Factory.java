package com.openclaw.ai;

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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private MainViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public MainViewModel get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static MainViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new MainViewModel_Factory(settingsRepositoryProvider);
  }

  public static MainViewModel newInstance(SettingsRepository settingsRepository) {
    return new MainViewModel(settingsRepository);
  }
}
