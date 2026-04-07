package com.openclaw.ai.ui.settings;

import com.openclaw.ai.data.repository.ConversationRepository;
import com.openclaw.ai.data.repository.ModelRepository;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<ModelRepository> modelRepositoryProvider;

  private final Provider<ConversationRepository> conversationRepositoryProvider;

  private SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.modelRepositoryProvider = modelRepositoryProvider;
    this.conversationRepositoryProvider = conversationRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), modelRepositoryProvider.get(), conversationRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, modelRepositoryProvider, conversationRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      ModelRepository modelRepository, ConversationRepository conversationRepository) {
    return new SettingsViewModel(settingsRepository, modelRepository, conversationRepository);
  }
}
