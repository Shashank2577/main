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
public final class TripPlannerTool_Factory implements Factory<TripPlannerTool> {
  @Override
  public TripPlannerTool get() {
    return newInstance();
  }

  public static TripPlannerTool_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TripPlannerTool newInstance() {
    return new TripPlannerTool();
  }

  private static final class InstanceHolder {
    static final TripPlannerTool_Factory INSTANCE = new TripPlannerTool_Factory();
  }
}
