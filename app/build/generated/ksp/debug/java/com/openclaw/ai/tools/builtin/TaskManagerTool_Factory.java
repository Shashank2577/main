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
public final class TaskManagerTool_Factory implements Factory<TaskManagerTool> {
  @Override
  public TaskManagerTool get() {
    return newInstance();
  }

  public static TaskManagerTool_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TaskManagerTool newInstance() {
    return new TaskManagerTool();
  }

  private static final class InstanceHolder {
    static final TaskManagerTool_Factory INSTANCE = new TaskManagerTool_Factory();
  }
}
