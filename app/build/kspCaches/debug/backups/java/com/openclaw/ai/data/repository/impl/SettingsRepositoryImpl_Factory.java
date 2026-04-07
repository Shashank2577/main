package com.openclaw.ai.data.repository.impl;

import android.content.Context;
import com.openclaw.ai.data.db.dao.PerChatSettingsDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsRepositoryImpl_Factory implements Factory<SettingsRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<PerChatSettingsDao> perChatSettingsDaoProvider;

  private SettingsRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<PerChatSettingsDao> perChatSettingsDaoProvider) {
    this.contextProvider = contextProvider;
    this.perChatSettingsDaoProvider = perChatSettingsDaoProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return newInstance(contextProvider.get(), perChatSettingsDaoProvider.get());
  }

  public static SettingsRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<PerChatSettingsDao> perChatSettingsDaoProvider) {
    return new SettingsRepositoryImpl_Factory(contextProvider, perChatSettingsDaoProvider);
  }

  public static SettingsRepositoryImpl newInstance(Context context,
      PerChatSettingsDao perChatSettingsDao) {
    return new SettingsRepositoryImpl(context, perChatSettingsDao);
  }
}
