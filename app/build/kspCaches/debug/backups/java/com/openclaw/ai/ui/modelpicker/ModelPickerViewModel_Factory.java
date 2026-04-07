package com.openclaw.ai.ui.modelpicker;

import com.openclaw.ai.data.repository.ModelRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class ModelPickerViewModel_Factory implements Factory<ModelPickerViewModel> {
  private final Provider<ModelRepository> modelRepositoryProvider;

  private ModelPickerViewModel_Factory(Provider<ModelRepository> modelRepositoryProvider) {
    this.modelRepositoryProvider = modelRepositoryProvider;
  }

  @Override
  public ModelPickerViewModel get() {
    return newInstance(modelRepositoryProvider.get());
  }

  public static ModelPickerViewModel_Factory create(
      Provider<ModelRepository> modelRepositoryProvider) {
    return new ModelPickerViewModel_Factory(modelRepositoryProvider);
  }

  public static ModelPickerViewModel newInstance(ModelRepository modelRepository) {
    return new ModelPickerViewModel(modelRepository);
  }
}
