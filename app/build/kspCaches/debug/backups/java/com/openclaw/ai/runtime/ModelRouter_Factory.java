package com.openclaw.ai.runtime;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ModelRouter_Factory implements Factory<ModelRouter> {
  private final Provider<LiteRtModelHelper> liteRtModelHelperProvider;

  private final Provider<GeminiModelHelper> geminiModelHelperProvider;

  private ModelRouter_Factory(Provider<LiteRtModelHelper> liteRtModelHelperProvider,
      Provider<GeminiModelHelper> geminiModelHelperProvider) {
    this.liteRtModelHelperProvider = liteRtModelHelperProvider;
    this.geminiModelHelperProvider = geminiModelHelperProvider;
  }

  @Override
  public ModelRouter get() {
    return newInstance(liteRtModelHelperProvider.get(), geminiModelHelperProvider.get());
  }

  public static ModelRouter_Factory create(Provider<LiteRtModelHelper> liteRtModelHelperProvider,
      Provider<GeminiModelHelper> geminiModelHelperProvider) {
    return new ModelRouter_Factory(liteRtModelHelperProvider, geminiModelHelperProvider);
  }

  public static ModelRouter newInstance(LiteRtModelHelper liteRtModelHelper,
      GeminiModelHelper geminiModelHelper) {
    return new ModelRouter(liteRtModelHelper, geminiModelHelper);
  }
}
