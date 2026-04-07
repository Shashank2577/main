package com.openclaw.ai.data.repository.impl

import com.openclaw.ai.data.db.dao.ConversationDao
import com.openclaw.ai.data.db.dao.MessageDao
import com.openclaw.ai.data.db.entity.ConversationEntity
import com.openclaw.ai.data.db.entity.MessageEntity
import com.openclaw.ai.data.model.ConversationWithLastMessage
import com.openclaw.ai.data.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
) : ConversationRepository {

    override fun getConversationsBySpace(spaceId: String): Flow<List<ConversationEntity>> =
        conversationDao.getConversationsBySpace(spaceId)

    override fun getAllConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getAllConversations()

    override suspend fun getConversation(id: String): ConversationEntity? =
        conversationDao.getConversation(id)

    override suspend fun createConversation(
        spaceId: String,
        title: String,
        systemPrompt: String?,
    ): ConversationEntity {
        val now = System.currentTimeMillis()
        val conversation = ConversationEntity(
            id = UUID.randomUUID().toString(),
            spaceId = spaceId,
            title = title,
            createdAt = now,
            lastMessageAt = now,
            systemPrompt = systemPrompt,
        )
        conversationDao.insert(conversation)
        return conversation
    }

    override suspend fun updateTitle(id: String, title: String) {
        conversationDao.updateTitle(id, title)
    }

    override suspend fun deleteConversation(id: String) {
        conversationDao.delete(id)
    }

    override suspend fun deleteAllConversations() {
        conversationDao.deleteAll()
    }

    override fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesByConversation(conversationId)

    override suspend fun getMessagesSync(conversationId: String): List<MessageEntity> =
        messageDao.getMessagesByConversationSync(conversationId)

    override suspend fun addMessage(message: MessageEntity) {
        messageDao.insert(message)
        conversationDao.updateLastMessageAt(message.conversationId, System.currentTimeMillis())
    }

    override suspend fun deleteMessage(id: String) {
        messageDao.delete(id)
    }

    override suspend fun getLastMessage(conversationId: String): MessageEntity? =
        messageDao.getLastMessage(conversationId)

    override suspend fun getConversationsWithLastMessage(spaceId: String): List<ConversationWithLastMessage> {
        val convList = conversationDao.getConversationsBySpace(spaceId).first()
        return convList.map { conv ->
            val lastMsg = messageDao.getLastMessage(conv.id)
            ConversationWithLastMessage(
                id = conv.id,
                spaceId = conv.spaceId,
                title = conv.title,
                lastMessageAt = conv.lastMessageAt,
                lastMessagePreview = lastMsg?.content,
            )
        }
    }
}
