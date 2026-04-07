package com.openclaw.ai.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.openclaw.ai.`data`.db.entity.MessageEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class MessageDao_Impl(
  __db: RoomDatabase,
) : MessageDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMessageEntity: EntityInsertAdapter<MessageEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMessageEntity = object : EntityInsertAdapter<MessageEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `messages` (`id`,`conversationId`,`role`,`content`,`mediaUri`,`toolName`,`toolParams`,`toolResult`,`timestamp`,`tokens`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.conversationId)
        statement.bindText(3, entity.role)
        statement.bindText(4, entity.content)
        val _tmpMediaUri: String? = entity.mediaUri
        if (_tmpMediaUri == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpMediaUri)
        }
        val _tmpToolName: String? = entity.toolName
        if (_tmpToolName == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpToolName)
        }
        val _tmpToolParams: String? = entity.toolParams
        if (_tmpToolParams == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpToolParams)
        }
        val _tmpToolResult: String? = entity.toolResult
        if (_tmpToolResult == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpToolResult)
        }
        statement.bindLong(9, entity.timestamp)
        val _tmpTokens: Int? = entity.tokens
        if (_tmpTokens == null) {
          statement.bindNull(10)
        } else {
          statement.bindLong(10, _tmpTokens.toLong())
        }
      }
    }
  }

  public override suspend fun insert(message: MessageEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfMessageEntity.insert(_connection, message)
  }

  public override suspend fun insertAll(messages: List<MessageEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMessageEntity.insert(_connection, messages)
  }

  public override fun getMessagesByConversation(conversationId: String): Flow<List<MessageEntity>> {
    val _sql: String = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC"
    return createFlow(__db, false, arrayOf("messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfMediaUri: Int = getColumnIndexOrThrow(_stmt, "mediaUri")
        val _columnIndexOfToolName: Int = getColumnIndexOrThrow(_stmt, "toolName")
        val _columnIndexOfToolParams: Int = getColumnIndexOrThrow(_stmt, "toolParams")
        val _columnIndexOfToolResult: Int = getColumnIndexOrThrow(_stmt, "toolResult")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTokens: Int = getColumnIndexOrThrow(_stmt, "tokens")
        val _result: MutableList<MessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpMediaUri: String?
          if (_stmt.isNull(_columnIndexOfMediaUri)) {
            _tmpMediaUri = null
          } else {
            _tmpMediaUri = _stmt.getText(_columnIndexOfMediaUri)
          }
          val _tmpToolName: String?
          if (_stmt.isNull(_columnIndexOfToolName)) {
            _tmpToolName = null
          } else {
            _tmpToolName = _stmt.getText(_columnIndexOfToolName)
          }
          val _tmpToolParams: String?
          if (_stmt.isNull(_columnIndexOfToolParams)) {
            _tmpToolParams = null
          } else {
            _tmpToolParams = _stmt.getText(_columnIndexOfToolParams)
          }
          val _tmpToolResult: String?
          if (_stmt.isNull(_columnIndexOfToolResult)) {
            _tmpToolResult = null
          } else {
            _tmpToolResult = _stmt.getText(_columnIndexOfToolResult)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTokens: Int?
          if (_stmt.isNull(_columnIndexOfTokens)) {
            _tmpTokens = null
          } else {
            _tmpTokens = _stmt.getLong(_columnIndexOfTokens).toInt()
          }
          _item =
              MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpMediaUri,_tmpToolName,_tmpToolParams,_tmpToolResult,_tmpTimestamp,_tmpTokens)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMessagesByConversationSync(conversationId: String):
      List<MessageEntity> {
    val _sql: String = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfMediaUri: Int = getColumnIndexOrThrow(_stmt, "mediaUri")
        val _columnIndexOfToolName: Int = getColumnIndexOrThrow(_stmt, "toolName")
        val _columnIndexOfToolParams: Int = getColumnIndexOrThrow(_stmt, "toolParams")
        val _columnIndexOfToolResult: Int = getColumnIndexOrThrow(_stmt, "toolResult")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTokens: Int = getColumnIndexOrThrow(_stmt, "tokens")
        val _result: MutableList<MessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpMediaUri: String?
          if (_stmt.isNull(_columnIndexOfMediaUri)) {
            _tmpMediaUri = null
          } else {
            _tmpMediaUri = _stmt.getText(_columnIndexOfMediaUri)
          }
          val _tmpToolName: String?
          if (_stmt.isNull(_columnIndexOfToolName)) {
            _tmpToolName = null
          } else {
            _tmpToolName = _stmt.getText(_columnIndexOfToolName)
          }
          val _tmpToolParams: String?
          if (_stmt.isNull(_columnIndexOfToolParams)) {
            _tmpToolParams = null
          } else {
            _tmpToolParams = _stmt.getText(_columnIndexOfToolParams)
          }
          val _tmpToolResult: String?
          if (_stmt.isNull(_columnIndexOfToolResult)) {
            _tmpToolResult = null
          } else {
            _tmpToolResult = _stmt.getText(_columnIndexOfToolResult)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTokens: Int?
          if (_stmt.isNull(_columnIndexOfTokens)) {
            _tmpTokens = null
          } else {
            _tmpTokens = _stmt.getLong(_columnIndexOfTokens).toInt()
          }
          _item =
              MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpMediaUri,_tmpToolName,_tmpToolParams,_tmpToolResult,_tmpTimestamp,_tmpTokens)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getMessage(id: String): MessageEntity? {
    val _sql: String = "SELECT * FROM messages WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfMediaUri: Int = getColumnIndexOrThrow(_stmt, "mediaUri")
        val _columnIndexOfToolName: Int = getColumnIndexOrThrow(_stmt, "toolName")
        val _columnIndexOfToolParams: Int = getColumnIndexOrThrow(_stmt, "toolParams")
        val _columnIndexOfToolResult: Int = getColumnIndexOrThrow(_stmt, "toolResult")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTokens: Int = getColumnIndexOrThrow(_stmt, "tokens")
        val _result: MessageEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpMediaUri: String?
          if (_stmt.isNull(_columnIndexOfMediaUri)) {
            _tmpMediaUri = null
          } else {
            _tmpMediaUri = _stmt.getText(_columnIndexOfMediaUri)
          }
          val _tmpToolName: String?
          if (_stmt.isNull(_columnIndexOfToolName)) {
            _tmpToolName = null
          } else {
            _tmpToolName = _stmt.getText(_columnIndexOfToolName)
          }
          val _tmpToolParams: String?
          if (_stmt.isNull(_columnIndexOfToolParams)) {
            _tmpToolParams = null
          } else {
            _tmpToolParams = _stmt.getText(_columnIndexOfToolParams)
          }
          val _tmpToolResult: String?
          if (_stmt.isNull(_columnIndexOfToolResult)) {
            _tmpToolResult = null
          } else {
            _tmpToolResult = _stmt.getText(_columnIndexOfToolResult)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTokens: Int?
          if (_stmt.isNull(_columnIndexOfTokens)) {
            _tmpTokens = null
          } else {
            _tmpTokens = _stmt.getLong(_columnIndexOfTokens).toInt()
          }
          _result =
              MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpMediaUri,_tmpToolName,_tmpToolParams,_tmpToolResult,_tmpTimestamp,_tmpTokens)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countByConversation(conversationId: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM messages WHERE conversationId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastMessage(conversationId: String): MessageEntity? {
    val _sql: String =
        "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp DESC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfMediaUri: Int = getColumnIndexOrThrow(_stmt, "mediaUri")
        val _columnIndexOfToolName: Int = getColumnIndexOrThrow(_stmt, "toolName")
        val _columnIndexOfToolParams: Int = getColumnIndexOrThrow(_stmt, "toolParams")
        val _columnIndexOfToolResult: Int = getColumnIndexOrThrow(_stmt, "toolResult")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfTokens: Int = getColumnIndexOrThrow(_stmt, "tokens")
        val _result: MessageEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpMediaUri: String?
          if (_stmt.isNull(_columnIndexOfMediaUri)) {
            _tmpMediaUri = null
          } else {
            _tmpMediaUri = _stmt.getText(_columnIndexOfMediaUri)
          }
          val _tmpToolName: String?
          if (_stmt.isNull(_columnIndexOfToolName)) {
            _tmpToolName = null
          } else {
            _tmpToolName = _stmt.getText(_columnIndexOfToolName)
          }
          val _tmpToolParams: String?
          if (_stmt.isNull(_columnIndexOfToolParams)) {
            _tmpToolParams = null
          } else {
            _tmpToolParams = _stmt.getText(_columnIndexOfToolParams)
          }
          val _tmpToolResult: String?
          if (_stmt.isNull(_columnIndexOfToolResult)) {
            _tmpToolResult = null
          } else {
            _tmpToolResult = _stmt.getText(_columnIndexOfToolResult)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpTokens: Int?
          if (_stmt.isNull(_columnIndexOfTokens)) {
            _tmpTokens = null
          } else {
            _tmpTokens = _stmt.getLong(_columnIndexOfTokens).toInt()
          }
          _result =
              MessageEntity(_tmpId,_tmpConversationId,_tmpRole,_tmpContent,_tmpMediaUri,_tmpToolName,_tmpToolParams,_tmpToolResult,_tmpTimestamp,_tmpTokens)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM messages WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteByConversation(conversationId: String) {
    val _sql: String = "DELETE FROM messages WHERE conversationId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
