package com.openclaw.ai.tools;

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
public final class DefaultToolRegistry_Factory implements Factory<DefaultToolRegistry> {
  @Override
  public DefaultToolRegistry get() {
    return newInstance();
  }

  public static DefaultToolRegistry_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DefaultToolRegistry newInstance() {
    return new DefaultToolRegistry();
  }

  private static final class InstanceHolder {
    static final DefaultToolRegistry_Factory INSTANCE = new DefaultToolRegistry_Factory();
  }
}
