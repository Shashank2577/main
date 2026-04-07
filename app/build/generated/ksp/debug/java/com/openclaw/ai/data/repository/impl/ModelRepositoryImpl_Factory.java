package com.openclaw.ai.data.repository.impl;

import android.content.Context;
import com.openclaw.ai.data.repository.DownloadRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ModelRepositoryImpl_Factory implements Factory<ModelRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private ModelRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
  }

  @Override
  public ModelRepositoryImpl get() {
    return newInstance(contextProvider.get(), downloadRepositoryProvider.get());
  }

  public static ModelRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<DownloadRepository> downloadRepositoryProvider) {
    return new ModelRepositoryImpl_Factory(contextProvider, downloadRepositoryProvider);
  }

  public static ModelRepositoryImpl newInstance(Context context,
      DownloadRepository downloadRepository) {
    return new ModelRepositoryImpl(context, downloadRepository);
  }
}
