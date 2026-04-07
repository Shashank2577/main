package com.openclaw.ai.di;

import com.openclaw.ai.tools.ToolRegistry;
import com.openclaw.ai.tools.builtin.FileSearchTool;
import com.openclaw.ai.tools.builtin.TaskManagerTool;
import com.openclaw.ai.tools.builtin.TripPlannerTool;
import com.openclaw.ai.tools.builtin.WebSearchTool;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
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
public final class ToolModule_ProvideToolRegistryFactory implements Factory<ToolRegistry> {
  private final Provider<TaskManagerTool> taskManagerToolProvider;

  private final Provider<TripPlannerTool> tripPlannerToolProvider;

  private final Provider<FileSearchTool> fileSearchToolProvider;

  private final Provider<WebSearchTool> webSearchToolProvider;

  private ToolModule_ProvideToolRegistryFactory(Provider<TaskManagerTool> taskManagerToolProvider,
      Provider<TripPlannerTool> tripPlannerToolProvider,
      Provider<FileSearchTool> fileSearchToolProvider,
      Provider<WebSearchTool> webSearchToolProvider) {
    this.taskManagerToolProvider = taskManagerToolProvider;
    this.tripPlannerToolProvider = tripPlannerToolProvider;
    this.fileSearchToolProvider = fileSearchToolProvider;
    this.webSearchToolProvider = webSearchToolProvider;
  }

  @Override
  public ToolRegistry get() {
    return provideToolRegistry(taskManagerToolProvider.get(), tripPlannerToolProvider.get(), fileSearchToolProvider.get(), webSearchToolProvider.get());
  }

  public static ToolModule_ProvideToolRegistryFactory create(
      Provider<TaskManagerTool> taskManagerToolProvider,
      Provider<TripPlannerTool> tripPlannerToolProvider,
      Provider<FileSearchTool> fileSearchToolProvider,
      Provider<WebSearchTool> webSearchToolProvider) {
    return new ToolModule_ProvideToolRegistryFactory(taskManagerToolProvider, tripPlannerToolProvider, fileSearchToolProvider, webSearchToolProvider);
  }

  public static ToolRegistry provideToolRegistry(TaskManagerTool taskManagerTool,
      TripPlannerTool tripPlannerTool, FileSearchTool fileSearchTool, WebSearchTool webSearchTool) {
    return Preconditions.checkNotNullFromProvides(ToolModule.INSTANCE.provideToolRegistry(taskManagerTool, tripPlannerTool, fileSearchTool, webSearchTool));
  }
}
