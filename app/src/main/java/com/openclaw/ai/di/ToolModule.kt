package com.openclaw.ai.di

import com.openclaw.ai.tools.DefaultToolRegistry
import com.openclaw.ai.tools.ToolRegistry
import com.openclaw.ai.tools.builtin.FileSearchTool
import com.openclaw.ai.tools.builtin.TaskManagerTool
import com.openclaw.ai.tools.builtin.TripPlannerTool
import com.openclaw.ai.tools.builtin.WebSearchTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ToolModule {

    @Provides
    @Singleton
    fun provideToolRegistry(
        taskManagerTool: TaskManagerTool,
        tripPlannerTool: TripPlannerTool,
        fileSearchTool: FileSearchTool,
        webSearchTool: WebSearchTool,
    ): ToolRegistry {
        val registry = DefaultToolRegistry()
        registry.register(taskManagerTool)
        registry.register(tripPlannerTool)
        registry.register(fileSearchTool)
        registry.register(webSearchTool)
        return registry
    }
}
