package com.openclaw.ai.di;

import com.openclaw.ai.data.db.AppDatabase;
import com.openclaw.ai.data.db.dao.SpaceDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideSpaceDaoFactory implements Factory<SpaceDao> {
  private final Provider<AppDatabase> dbProvider;

  private AppModule_ProvideSpaceDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SpaceDao get() {
    return provideSpaceDao(dbProvider.get());
  }

  public static AppModule_ProvideSpaceDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideSpaceDaoFactory(dbProvider);
  }

  public static SpaceDao provideSpaceDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSpaceDao(db));
  }
}
