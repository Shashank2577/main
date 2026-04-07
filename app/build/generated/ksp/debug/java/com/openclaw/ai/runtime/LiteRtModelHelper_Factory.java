package com.openclaw.ai.runtime;

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
public final class LiteRtModelHelper_Factory implements Factory<LiteRtModelHelper> {
  @Override
  public LiteRtModelHelper get() {
    return newInstance();
  }

  public static LiteRtModelHelper_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LiteRtModelHelper newInstance() {
    return new LiteRtModelHelper();
  }

  private static final class InstanceHolder {
    static final LiteRtModelHelper_Factory INSTANCE = new LiteRtModelHelper_Factory();
  }
}
