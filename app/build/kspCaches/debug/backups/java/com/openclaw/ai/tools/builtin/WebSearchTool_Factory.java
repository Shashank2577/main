package com.openclaw.ai.tools.builtin;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class WebSearchTool_Factory implements Factory<WebSearchTool> {
  @Override
  public WebSearchTool get() {
    return newInstance();
  }

  public static WebSearchTool_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WebSearchTool newInstance() {
    return new WebSearchTool();
  }

  private static final class InstanceHolder {
    static final WebSearchTool_Factory INSTANCE = new WebSearchTool_Factory();
  }
}
