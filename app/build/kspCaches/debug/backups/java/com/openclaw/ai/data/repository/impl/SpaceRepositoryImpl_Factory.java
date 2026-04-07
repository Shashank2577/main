package com.openclaw.ai.data.repository.impl;

import com.openclaw.ai.data.db.dao.SpaceDao;
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
public final class SpaceRepositoryImpl_Factory implements Factory<SpaceRepositoryImpl> {
  private final Provider<SpaceDao> spaceDaoProvider;

  private SpaceRepositoryImpl_Factory(Provider<SpaceDao> spaceDaoProvider) {
    this.spaceDaoProvider = spaceDaoProvider;
  }

  @Override
  public SpaceRepositoryImpl get() {
    return newInstance(spaceDaoProvider.get());
  }

  public static SpaceRepositoryImpl_Factory create(Provider<SpaceDao> spaceDaoProvider) {
    return new SpaceRepositoryImpl_Factory(spaceDaoProvider);
  }

  public static SpaceRepositoryImpl newInstance(SpaceDao spaceDao) {
    return new SpaceRepositoryImpl(spaceDao);
  }
}
