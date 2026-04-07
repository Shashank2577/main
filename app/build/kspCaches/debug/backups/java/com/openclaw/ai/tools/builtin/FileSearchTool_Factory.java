package com.openclaw.ai.tools.builtin;

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
public final class FileSearchTool_Factory implements Factory<FileSearchTool> {
  private final Provider<Context> contextProvider;

  private FileSearchTool_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FileSearchTool get() {
    return newInstance(contextProvider.get());
  }

  public static FileSearchTool_Factory create(Provider<Context> contextProvider) {
    return new FileSearchTool_Factory(contextProvider);
  }

  public static FileSearchTool newInstance(Context context) {
    return new FileSearchTool(context);
  }
}
