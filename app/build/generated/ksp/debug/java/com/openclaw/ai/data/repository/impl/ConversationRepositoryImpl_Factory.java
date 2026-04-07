package com.openclaw.ai.data.repository.impl;

import com.openclaw.ai.data.db.dao.ConversationDao;
import com.openclaw.ai.data.db.dao.MessageDao;
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
public final class ConversationRepositoryImpl_Factory implements Factory<ConversationRepositoryImpl> {
  private final Provider<ConversationDao> conversationDaoProvider;

  private final Provider<MessageDao> messageDaoProvider;

  private ConversationRepositoryImpl_Factory(Provider<ConversationDao> conversationDaoProvider,
      Provider<MessageDao> messageDaoProvider) {
    this.conversationDaoProvider = conversationDaoProvider;
    this.messageDaoProvider = messageDaoProvider;
  }

  @Override
  public ConversationRepositoryImpl get() {
    return newInstance(conversationDaoProvider.get(), messageDaoProvider.get());
  }

  public static ConversationRepositoryImpl_Factory create(
      Provider<ConversationDao> conversationDaoProvider, Provider<MessageDao> messageDaoProvider) {
    return new ConversationRepositoryImpl_Factory(conversationDaoProvider, messageDaoProvider);
  }

  public static ConversationRepositoryImpl newInstance(ConversationDao conversationDao,
      MessageDao messageDao) {
    return new ConversationRepositoryImpl(conversationDao, messageDao);
  }
}
