package com.openclaw.ai.ui.chat;

import android.content.Context;
import com.openclaw.ai.data.repository.ConversationRepository;
import com.openclaw.ai.data.repository.ModelRepository;
import com.openclaw.ai.data.repository.SettingsRepository;
import com.openclaw.ai.runtime.ModelRouter;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ConversationRepository> conversationRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<ModelRepository> modelRepositoryProvider;

  private final Provider<ModelRouter> modelRouterProvider;

  private ChatViewModel_Factory(Provider<Context> contextProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ModelRouter> modelRouterProvider) {
    this.contextProvider = contextProvider;
    this.conversationRepositoryProvider = conversationRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.modelRepositoryProvider = modelRepositoryProvider;
    this.modelRouterProvider = modelRouterProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(contextProvider.get(), conversationRepositoryProvider.get(), settingsRepositoryProvider.get(), modelRepositoryProvider.get(), modelRouterProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ModelRouter> modelRouterProvider) {
    return new ChatViewModel_Factory(contextProvider, conversationRepositoryProvider, settingsRepositoryProvider, modelRepositoryProvider, modelRouterProvider);
  }

  public static ChatViewModel newInstance(Context context,
      ConversationRepository conversationRepository, SettingsRepository settingsRepository,
      ModelRepository modelRepository, ModelRouter modelRouter) {
    return new ChatViewModel(context, conversationRepository, settingsRepository, modelRepository, modelRouter);
  }
}
