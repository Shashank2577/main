package com.openclaw.ai.di;

import com.openclaw.ai.data.db.AppDatabase;
import com.openclaw.ai.data.db.dao.PerChatSettingsDao;
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
public final class AppModule_ProvidePerChatSettingsDaoFactory implements Factory<PerChatSettingsDao> {
  private final Provider<AppDatabase> dbProvider;

  private AppModule_ProvidePerChatSettingsDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PerChatSettingsDao get() {
    return providePerChatSettingsDao(dbProvider.get());
  }

  public static AppModule_ProvidePerChatSettingsDaoFactory create(
      Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvidePerChatSettingsDaoFactory(dbProvider);
  }

  public static PerChatSettingsDao providePerChatSettingsDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePerChatSettingsDao(db));
  }
}
