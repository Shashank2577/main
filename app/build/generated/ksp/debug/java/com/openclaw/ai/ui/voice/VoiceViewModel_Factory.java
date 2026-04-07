package com.openclaw.ai.ui.voice;

import android.content.Context;
import com.openclaw.ai.data.repository.ConversationRepository;
import com.openclaw.ai.data.repository.ModelRepository;
import com.openclaw.ai.runtime.LlmModelHelper;
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
public final class VoiceViewModel_Factory implements Factory<VoiceViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<ModelRepository> modelRepositoryProvider;

  private final Provider<ConversationRepository> conversationRepositoryProvider;

  private final Provider<LlmModelHelper> llmModelHelperProvider;

  private VoiceViewModel_Factory(Provider<Context> contextProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<LlmModelHelper> llmModelHelperProvider) {
    this.contextProvider = contextProvider;
    this.modelRepositoryProvider = modelRepositoryProvider;
    this.conversationRepositoryProvider = conversationRepositoryProvider;
    this.llmModelHelperProvider = llmModelHelperProvider;
  }

  @Override
  public VoiceViewModel get() {
    return newInstance(contextProvider.get(), modelRepositoryProvider.get(), conversationRepositoryProvider.get(), llmModelHelperProvider.get());
  }

  public static VoiceViewModel_Factory create(Provider<Context> contextProvider,
      Provider<ModelRepository> modelRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<LlmModelHelper> llmModelHelperProvider) {
    return new VoiceViewModel_Factory(contextProvider, modelRepositoryProvider, conversationRepositoryProvider, llmModelHelperProvider);
  }

  public static VoiceViewModel newInstance(Context context, ModelRepository modelRepository,
      ConversationRepository conversationRepository, LlmModelHelper llmModelHelper) {
    return new VoiceViewModel(context, modelRepository, conversationRepository, llmModelHelper);
  }
}
