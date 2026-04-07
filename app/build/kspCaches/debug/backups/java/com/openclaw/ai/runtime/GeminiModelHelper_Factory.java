package com.openclaw.ai.runtime;

import com.openclaw.ai.data.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class GeminiModelHelper_Factory implements Factory<GeminiModelHelper> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private GeminiModelHelper_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public GeminiModelHelper get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static GeminiModelHelper_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new GeminiModelHelper_Factory(settingsRepositoryProvider);
  }

  public static GeminiModelHelper newInstance(SettingsRepository settingsRepository) {
    return new GeminiModelHelper(settingsRepository);
  }
}
