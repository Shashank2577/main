package com.openclaw.ai.ui.filebrowser;

import android.content.Context;
import com.openclaw.ai.data.repository.SpaceRepository;
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
public final class FileBrowserViewModel_Factory implements Factory<FileBrowserViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<SpaceRepository> spaceRepositoryProvider;

  private FileBrowserViewModel_Factory(Provider<Context> contextProvider,
      Provider<SpaceRepository> spaceRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.spaceRepositoryProvider = spaceRepositoryProvider;
  }

  @Override
  public FileBrowserViewModel get() {
    return newInstance(contextProvider.get(), spaceRepositoryProvider.get());
  }

  public static FileBrowserViewModel_Factory create(Provider<Context> contextProvider,
      Provider<SpaceRepository> spaceRepositoryProvider) {
    return new FileBrowserViewModel_Factory(contextProvider, spaceRepositoryProvider);
  }

  public static FileBrowserViewModel newInstance(Context context, SpaceRepository spaceRepository) {
    return new FileBrowserViewModel(context, spaceRepository);
  }
}
