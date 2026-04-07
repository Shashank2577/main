package com.openclaw.ai.data.repository

import com.openclaw.ai.data.db.entity.ConversationEntity
import com.openclaw.ai.data.db.entity.MessageEntity
import com.openclaw.ai.data.model.ConversationWithLastMessage
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {

    fun getConversationsBySpace(spaceId: String): Flow<List<ConversationEntity>>

    fun getAllConversations(): Flow<List<ConversationEntity>>

    suspend fun getConversation(id: String): ConversationEntity?

    suspend fun createConversation(
        spaceId: String,
        title: String = "New Chat",
        systemPrompt: String? = null,
    ): ConversationEntity

    suspend fun updateTitle(id: String, title: String)

    suspend fun deleteConversation(id: String)

    suspend fun deleteAllConversations()

    // Messages
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    suspend fun getMessagesSync(conversationId: String): List<MessageEntity>

    suspend fun addMessage(message: MessageEntity)

    suspend fun deleteMessage(id: String)

    suspend fun getLastMessage(conversationId: String): MessageEntity?

    // Convenience
    suspend fun getConversationsWithLastMessage(spaceId: String): List<ConversationWithLastMessage>
}
