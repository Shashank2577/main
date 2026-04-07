package com.openclaw.ai.`data`.db.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.openclaw.ai.`data`.db.entity.SpaceEntity
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
public class SpaceDao_Impl(
  __db: RoomDatabase,
) : SpaceDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSpaceEntity: EntityInsertAdapter<SpaceEntity>

  private val __updateAdapterOfSpaceEntity: EntityDeleteOrUpdateAdapter<SpaceEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSpaceEntity = object : EntityInsertAdapter<SpaceEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `spaces` (`id`,`name`,`emoji`,`description`,`systemPrompt`,`createdAt`,`lastUsedAt`,`sortOrder`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SpaceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindText(4, entity.description)
        val _tmpSystemPrompt: String? = entity.systemPrompt
        if (_tmpSystemPrompt == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpSystemPrompt)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.lastUsedAt)
        statement.bindLong(8, entity.sortOrder.toLong())
      }
    }
    this.__updateAdapterOfSpaceEntity = object : EntityDeleteOrUpdateAdapter<SpaceEntity>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `spaces` SET `id` = ?,`name` = ?,`emoji` = ?,`description` = ?,`systemPrompt` = ?,`createdAt` = ?,`lastUsedAt` = ?,`sortOrder` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SpaceEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.emoji)
        statement.bindText(4, entity.description)
        val _tmpSystemPrompt: String? = entity.systemPrompt
        if (_tmpSystemPrompt == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpSystemPrompt)
        }
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.lastUsedAt)
        statement.bindLong(8, entity.sortOrder.toLong())
        statement.bindText(9, entity.id)
      }
    }
  }

  public override suspend fun insert(space: SpaceEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfSpaceEntity.insert(_connection, space)
  }

  public override suspend fun update(space: SpaceEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __updateAdapterOfSpaceEntity.handle(_connection, space)
  }

  public override fun getAllSpaces(): Flow<List<SpaceEntity>> {
    val _sql: String = "SELECT * FROM spaces ORDER BY sortOrder ASC, lastUsedAt DESC"
    return createFlow(__db, false, arrayOf("spaces")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSystemPrompt: Int = getColumnIndexOrThrow(_stmt, "systemPrompt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastUsedAt: Int = getColumnIndexOrThrow(_stmt, "lastUsedAt")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _result: MutableList<SpaceEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SpaceEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpSystemPrompt: String?
          if (_stmt.isNull(_columnIndexOfSystemPrompt)) {
            _tmpSystemPrompt = null
          } else {
            _tmpSystemPrompt = _stmt.getText(_columnIndexOfSystemPrompt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastUsedAt: Long
          _tmpLastUsedAt = _stmt.getLong(_columnIndexOfLastUsedAt)
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          _item =
              SpaceEntity(_tmpId,_tmpName,_tmpEmoji,_tmpDescription,_tmpSystemPrompt,_tmpCreatedAt,_tmpLastUsedAt,_tmpSortOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSpace(id: String): SpaceEntity? {
    val _sql: String = "SELECT * FROM spaces WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfEmoji: Int = getColumnIndexOrThrow(_stmt, "emoji")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfSystemPrompt: Int = getColumnIndexOrThrow(_stmt, "systemPrompt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastUsedAt: Int = getColumnIndexOrThrow(_stmt, "lastUsedAt")
        val _columnIndexOfSortOrder: Int = getColumnIndexOrThrow(_stmt, "sortOrder")
        val _result: SpaceEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpEmoji: String
          _tmpEmoji = _stmt.getText(_columnIndexOfEmoji)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpSystemPrompt: String?
          if (_stmt.isNull(_columnIndexOfSystemPrompt)) {
            _tmpSystemPrompt = null
          } else {
            _tmpSystemPrompt = _stmt.getText(_columnIndexOfSystemPrompt)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastUsedAt: Long
          _tmpLastUsedAt = _stmt.getLong(_columnIndexOfLastUsedAt)
          val _tmpSortOrder: Int
          _tmpSortOrder = _stmt.getLong(_columnIndexOfSortOrder).toInt()
          _result =
              SpaceEntity(_tmpId,_tmpName,_tmpEmoji,_tmpDescription,_tmpSystemPrompt,_tmpCreatedAt,_tmpLastUsedAt,_tmpSortOrder)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM spaces"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun updateLastUsedAt(id: String, timestamp: Long) {
    val _sql: String = "UPDATE spaces SET lastUsedAt = ? WHERE id = ?"
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
    val _sql: String = "DELETE FROM spaces WHERE id = ?"
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

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
