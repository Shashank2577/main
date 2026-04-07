package com.openclaw.ai.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.openclaw.ai.`data`.db.entity.PerChatSettingsEntity
import javax.`annotation`.processing.Generated
import kotlin.Float
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PerChatSettingsDao_Impl(
  __db: RoomDatabase,
) : PerChatSettingsDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfPerChatSettingsEntity: EntityInsertAdapter<PerChatSettingsEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfPerChatSettingsEntity = object :
        EntityInsertAdapter<PerChatSettingsEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `per_chat_settings` (`conversationId`,`temperature`,`topK`,`topP`,`maxTokens`,`preferredModelId`,`enabledTools`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PerChatSettingsEntity) {
        statement.bindText(1, entity.conversationId)
        statement.bindDouble(2, entity.temperature.toDouble())
        statement.bindLong(3, entity.topK.toLong())
        statement.bindDouble(4, entity.topP.toDouble())
        statement.bindLong(5, entity.maxTokens.toLong())
        val _tmpPreferredModelId: String? = entity.preferredModelId
        if (_tmpPreferredModelId == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpPreferredModelId)
        }
        val _tmpEnabledTools: String? = entity.enabledTools
        if (_tmpEnabledTools == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpEnabledTools)
        }
      }
    }
  }

  public override suspend fun upsert(settings: PerChatSettingsEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPerChatSettingsEntity.insert(_connection, settings)
  }

  public override suspend fun getSettings(conversationId: String): PerChatSettingsEntity? {
    val _sql: String = "SELECT * FROM per_chat_settings WHERE conversationId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfTemperature: Int = getColumnIndexOrThrow(_stmt, "temperature")
        val _columnIndexOfTopK: Int = getColumnIndexOrThrow(_stmt, "topK")
        val _columnIndexOfTopP: Int = getColumnIndexOrThrow(_stmt, "topP")
        val _columnIndexOfMaxTokens: Int = getColumnIndexOrThrow(_stmt, "maxTokens")
        val _columnIndexOfPreferredModelId: Int = getColumnIndexOrThrow(_stmt, "preferredModelId")
        val _columnIndexOfEnabledTools: Int = getColumnIndexOrThrow(_stmt, "enabledTools")
        val _result: PerChatSettingsEntity?
        if (_stmt.step()) {
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpTemperature: Float
          _tmpTemperature = _stmt.getDouble(_columnIndexOfTemperature).toFloat()
          val _tmpTopK: Int
          _tmpTopK = _stmt.getLong(_columnIndexOfTopK).toInt()
          val _tmpTopP: Float
          _tmpTopP = _stmt.getDouble(_columnIndexOfTopP).toFloat()
          val _tmpMaxTokens: Int
          _tmpMaxTokens = _stmt.getLong(_columnIndexOfMaxTokens).toInt()
          val _tmpPreferredModelId: String?
          if (_stmt.isNull(_columnIndexOfPreferredModelId)) {
            _tmpPreferredModelId = null
          } else {
            _tmpPreferredModelId = _stmt.getText(_columnIndexOfPreferredModelId)
          }
          val _tmpEnabledTools: String?
          if (_stmt.isNull(_columnIndexOfEnabledTools)) {
            _tmpEnabledTools = null
          } else {
            _tmpEnabledTools = _stmt.getText(_columnIndexOfEnabledTools)
          }
          _result =
              PerChatSettingsEntity(_tmpConversationId,_tmpTemperature,_tmpTopK,_tmpTopP,_tmpMaxTokens,_tmpPreferredModelId,_tmpEnabledTools)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeSettings(conversationId: String): Flow<PerChatSettingsEntity?> {
    val _sql: String = "SELECT * FROM per_chat_settings WHERE conversationId = ?"
    return createFlow(__db, false, arrayOf("per_chat_settings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, conversationId)
        val _columnIndexOfConversationId: Int = getColumnIndexOrThrow(_stmt, "conversationId")
        val _columnIndexOfTemperature: Int = getColumnIndexOrThrow(_stmt, "temperature")
        val _columnIndexOfTopK: Int = getColumnIndexOrThrow(_stmt, "topK")
        val _columnIndexOfTopP: Int = getColumnIndexOrThrow(_stmt, "topP")
        val _columnIndexOfMaxTokens: Int = getColumnIndexOrThrow(_stmt, "maxTokens")
        val _columnIndexOfPreferredModelId: Int = getColumnIndexOrThrow(_stmt, "preferredModelId")
        val _columnIndexOfEnabledTools: Int = getColumnIndexOrThrow(_stmt, "enabledTools")
        val _result: PerChatSettingsEntity?
        if (_stmt.step()) {
          val _tmpConversationId: String
          _tmpConversationId = _stmt.getText(_columnIndexOfConversationId)
          val _tmpTemperature: Float
          _tmpTemperature = _stmt.getDouble(_columnIndexOfTemperature).toFloat()
          val _tmpTopK: Int
          _tmpTopK = _stmt.getLong(_columnIndexOfTopK).toInt()
          val _tmpTopP: Float
          _tmpTopP = _stmt.getDouble(_columnIndexOfTopP).toFloat()
          val _tmpMaxTokens: Int
          _tmpMaxTokens = _stmt.getLong(_columnIndexOfMaxTokens).toInt()
          val _tmpPreferredModelId: String?
          if (_stmt.isNull(_columnIndexOfPreferredModelId)) {
            _tmpPreferredModelId = null
          } else {
            _tmpPreferredModelId = _stmt.getText(_columnIndexOfPreferredModelId)
          }
          val _tmpEnabledTools: String?
          if (_stmt.isNull(_columnIndexOfEnabledTools)) {
            _tmpEnabledTools = null
          } else {
            _tmpEnabledTools = _stmt.getText(_columnIndexOfEnabledTools)
          }
          _result =
              PerChatSettingsEntity(_tmpConversationId,_tmpTemperature,_tmpTopK,_tmpTopP,_tmpMaxTokens,_tmpPreferredModelId,_tmpEnabledTools)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun delete(conversationId: String) {
    val _sql: String = "DELETE FROM per_chat_settings WHERE conversationId = ?"
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
