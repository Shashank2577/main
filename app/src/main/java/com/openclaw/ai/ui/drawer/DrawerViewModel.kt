package com.openclaw.ai.ui.drawer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclaw.ai.data.db.entity.ConversationEntity
import com.openclaw.ai.data.db.entity.SpaceEntity
import com.openclaw.ai.data.repository.ConversationRepository
import com.openclaw.ai.data.repository.SpaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val conversationRepository: ConversationRepository,
    private val spaceRepository: SpaceRepository,
) : ViewModel() {

    private val _currentSpaceId = MutableStateFlow("")
    val currentSpaceId: StateFlow<String> = _currentSpaceId.asStateFlow()

    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    val spaces: StateFlow<List<SpaceEntity>> = spaceRepository.getAllSpaces()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val conversations: StateFlow<List<ConversationEntity>> = _currentSpaceId
        .flatMapLatest { spaceId ->
            if (spaceId.isNotEmpty()) {
                conversationRepository.getConversationsBySpace(spaceId)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    init {
        viewModelScope.launch {
            val defaultSpace = spaceRepository.ensureDefaultSpace()
            _currentSpaceId.value = defaultSpace.id
        }
    }

    fun loadConversationsForSpace(spaceId: String) {
        _currentSpaceId.value = spaceId
    }

    fun switchSpace(spaceId: String) {
        _currentSpaceId.value = spaceId
        _currentConversationId.value = null
    }

    fun setCurrentConversation(conversationId: String?) {
        _currentConversationId.value = conversationId
    }

    suspend fun createNewChat(): ConversationEntity {
        val spaceId = _currentSpaceId.value.ifEmpty {
            spaceRepository.ensureDefaultSpace().id.also { _currentSpaceId.value = it }
        }
        val conversation = conversationRepository.createConversation(spaceId = spaceId)
        _currentConversationId.value = conversation.id
        return conversation
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
            if (_currentConversationId.value == id) {
                _currentConversationId.value = null
            }
        }
    }

    fun createSpace(name: String, emoji: String) {
        viewModelScope.launch {
            spaceRepository.createSpace(name = name, emoji = emoji)
        }
    }
}
