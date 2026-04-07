package com.openclaw.ai.ui.drawer;

import com.openclaw.ai.data.repository.ConversationRepository;
import com.openclaw.ai.data.repository.SpaceRepository;
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
public final class DrawerViewModel_Factory implements Factory<DrawerViewModel> {
  private final Provider<ConversationRepository> conversationRepositoryProvider;

  private final Provider<SpaceRepository> spaceRepositoryProvider;

  private DrawerViewModel_Factory(Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<SpaceRepository> spaceRepositoryProvider) {
    this.conversationRepositoryProvider = conversationRepositoryProvider;
    this.spaceRepositoryProvider = spaceRepositoryProvider;
  }

  @Override
  public DrawerViewModel get() {
    return newInstance(conversationRepositoryProvider.get(), spaceRepositoryProvider.get());
  }

  public static DrawerViewModel_Factory create(
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<SpaceRepository> spaceRepositoryProvider) {
    return new DrawerViewModel_Factory(conversationRepositoryProvider, spaceRepositoryProvider);
  }

  public static DrawerViewModel newInstance(ConversationRepository conversationRepository,
      SpaceRepository spaceRepository) {
    return new DrawerViewModel(conversationRepository, spaceRepository);
  }
}
