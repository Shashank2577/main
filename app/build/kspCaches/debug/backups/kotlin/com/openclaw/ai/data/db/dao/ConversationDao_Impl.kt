package com.openclaw.ai.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.openclaw.ai.`data`.db.entity.ConversationEntity
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
public class ConversationDao_Impl(
  __db: RoomDatabase,
) : ConversationDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfConversationEntity: EntityInsertAdapter<ConversationEntity>

  private val __updateAdapterOfConversationEntity: EntityDeleteOrUpdateAdapter<ConversationEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfConversationEntity = object : EntityInsertAdapter<ConversationEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `conversations` (`id`,`spaceId`,`title`,`createdAt`,`lastMessageAt`,`systemPrompt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ConversationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.spaceId)
        statement.bindText(3, entity.title)
        statement.bindLong(4, entity.createdAt)
        statement.bindLong(5, entity.lastMessageAt)
        val _tmpSystemPrompt: String? = entity.systemPrompt
        if (_tmpSystemPrompt == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpSystemPrompt)
        }
      }
    }
    this.__updateAdapterOfConversationEntity = object :
        EntityDeleteOrUpdateAdapter<ConversationEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `conversations` SET `id` = ?,`spaceId` = ?,`title` = ?,`createdAt` = ?,`lastMessageAt` = ?,`systemPrompt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ConversationEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.spaceId)
        statement.bindText(3, entity.title)
        statement.bindLong(4, entity.createdAt)
        statement.bindLong(5, entity.lastMessageAt)
        val _tmpSystemPrompt: String? = entity.systemPrompt
        if (_tmpSystemPrompt == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpSystemPrompt)
        }
        statement.bindText(7, entity.id)
      }
    }
  }

  public override suspend fun insert(conversation: ConversationEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfConversationEntity.insert(_connection, conversation)
  }

  public override suspend fun update(conversation: ConversationEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfConversationEntity.handle(_connection, conversation)
  }

  public override fun getConversationsBySpace(spaceId: String): Flow<List<ConversationEntity>> {
    val _sql: String = "SELECT * FROM conversations WHERE spaceId = ? ORDER BY lastMessageAt DESC"
    return createFlow(__db, false, arrayOf("conversations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, spaceId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSpaceId: Int = getColumnIndexOrThrow(_stmt, "spaceId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastMessageAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageAt")
        val _columnIndexOfSystemPrompt: Int = getColumnIndexOrThrow(_stmt, "systemPrompt")
        val _result: MutableList<ConversationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ConversationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSpaceId: String
          _tmpSpaceId = _stmt.getText(_columnIndexOfSpaceId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastMessageAt: Long
          _tmpLastMessageAt = _stmt.getLong(_columnIndexOfLastMessageAt)
          val _tmpSystemPrompt: String?
          if (_stmt.isNull(_columnIndexOfSystemPrompt)) {
            _tmpSystemPrompt = null
          } else {
            _tmpSystemPrompt = _stmt.getText(_columnIndexOfSystemPrompt)
          }
          _item =
              ConversationEntity(_tmpId,_tmpSpaceId,_tmpTitle,_tmpCreatedAt,_tmpLastMessageAt,_tmpSystemPrompt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllConversations(): Flow<List<ConversationEntity>> {
    val _sql: String = "SELECT * FROM conversations ORDER BY lastMessageAt DESC"
    return createFlow(__db, false, arrayOf("conversations")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSpaceId: Int = getColumnIndexOrThrow(_stmt, "spaceId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastMessageAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageAt")
        val _columnIndexOfSystemPrompt: Int = getColumnIndexOrThrow(_stmt, "systemPrompt")
        val _result: MutableList<ConversationEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ConversationEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSpaceId: String
          _tmpSpaceId = _stmt.getText(_columnIndexOfSpaceId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastMessageAt: Long
          _tmpLastMessageAt = _stmt.getLong(_columnIndexOfLastMessageAt)
          val _tmpSystemPrompt: String?
          if (_stmt.isNull(_columnIndexOfSystemPrompt)) {
            _tmpSystemPrompt = null
          } else {
            _tmpSystemPrompt = _stmt.getText(_columnIndexOfSystemPrompt)
          }
          _item =
              ConversationEntity(_tmpId,_tmpSpaceId,_tmpTitle,_tmpCreatedAt,_tmpLastMessageAt,_tmpSystemPrompt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getConversation(id: String): ConversationEntity? {
    val _sql: String = "SELECT * FROM conversations WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSpaceId: Int = getColumnIndexOrThrow(_stmt, "spaceId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastMessageAt: Int = getColumnIndexOrThrow(_stmt, "lastMessageAt")
        val _columnIndexOfSystemPrompt: Int = getColumnIndexOrThrow(_stmt, "systemPrompt")
        val _result: ConversationEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSpaceId: String
          _tmpSpaceId = _stmt.getText(_columnIndexOfSpaceId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastMessageAt: Long
          _tmpLastMessageAt = _stmt.getLong(_columnIndexOfLastMessageAt)
          val _tmpSystemPrompt: String?
          if (_stmt.isNull(_columnIndexOfSystemPrompt)) {
            _tmpSystemPrompt = null
          } else {
            _tmpSystemPrompt = _stmt.getText(_columnIndexOfSystemPrompt)
          }
          _result =
              ConversationEntity(_tmpId,_tmpSpaceId,_tmpTitle,_tmpCreatedAt,_tmpLastMessageAt,_tmpSystemPrompt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countBySpace(spaceId: String): Int {
    val _sql: String = "SELECT COUNT(*) FROM conversations WHERE spaceId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, spaceId)
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

  public override suspend fun updateTitle(id: String, title: String) {
    val _sql: String = "UPDATE conversations SET title = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, title)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateLastMessageAt(id: String, timestamp: Long) {
    val _sql: String = "UPDATE conversations SET lastMessageAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(id: String) {
    val _sql: String = "DELETE FROM conversations WHERE id = ?"
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

  public override suspend fun deleteBySpace(spaceId: String) {
    val _sql: String = "DELETE FROM conversations WHERE spaceId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, spaceId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM conversations"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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
