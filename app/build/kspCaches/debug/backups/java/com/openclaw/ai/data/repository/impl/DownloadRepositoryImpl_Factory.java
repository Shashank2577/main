package com.openclaw.ai.data.repository.impl;

import android.content.Context;
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
public final class DownloadRepositoryImpl_Factory implements Factory<DownloadRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private DownloadRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public DownloadRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static DownloadRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new DownloadRepositoryImpl_Factory(contextProvider);
  }

  public static DownloadRepositoryImpl newInstance(Context context) {
    return new DownloadRepositoryImpl(context);
  }
}
