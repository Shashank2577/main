package com.openclaw.ai.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.openclaw.ai.`data`.db.dao.ConversationDao
import com.openclaw.ai.`data`.db.dao.ConversationDao_Impl
import com.openclaw.ai.`data`.db.dao.MessageDao
import com.openclaw.ai.`data`.db.dao.MessageDao_Impl
import com.openclaw.ai.`data`.db.dao.PerChatSettingsDao
import com.openclaw.ai.`data`.db.dao.PerChatSettingsDao_Impl
import com.openclaw.ai.`data`.db.dao.SpaceDao
import com.openclaw.ai.`data`.db.dao.SpaceDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _conversationDao: Lazy<ConversationDao> = lazy {
    ConversationDao_Impl(this)
  }

  private val _messageDao: Lazy<MessageDao> = lazy {
    MessageDao_Impl(this)
  }

  private val _spaceDao: Lazy<SpaceDao> = lazy {
    SpaceDao_Impl(this)
  }

  private val _perChatSettingsDao: Lazy<PerChatSettingsDao> = lazy {
    PerChatSettingsDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "415feef19e83a2fee6700784d0cf0d82", "ad685b6ec25ae4b33247cc7ba7372e09") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `conversations` (`id` TEXT NOT NULL, `spaceId` TEXT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `lastMessageAt` INTEGER NOT NULL, `systemPrompt` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`spaceId`) REFERENCES `spaces`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_spaceId` ON `conversations` (`spaceId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` TEXT NOT NULL, `conversationId` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `mediaUri` TEXT, `toolName` TEXT, `toolParams` TEXT, `toolResult` TEXT, `timestamp` INTEGER NOT NULL, `tokens` INTEGER, PRIMARY KEY(`id`), FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `spaces` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `emoji` TEXT NOT NULL, `description` TEXT NOT NULL, `systemPrompt` TEXT, `createdAt` INTEGER NOT NULL, `lastUsedAt` INTEGER NOT NULL, `sortOrder` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `per_chat_settings` (`conversationId` TEXT NOT NULL, `temperature` REAL NOT NULL, `topK` INTEGER NOT NULL, `topP` REAL NOT NULL, `maxTokens` INTEGER NOT NULL, `preferredModelId` TEXT, `enabledTools` TEXT, PRIMARY KEY(`conversationId`), FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '415feef19e83a2fee6700784d0cf0d82')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `conversations`")
        connection.execSQL("DROP TABLE IF EXISTS `messages`")
        connection.execSQL("DROP TABLE IF EXISTS `spaces`")
        connection.execSQL("DROP TABLE IF EXISTS `per_chat_settings`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsConversations: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsConversations.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("spaceId", TableInfo.Column("spaceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("lastMessageAt", TableInfo.Column("lastMessageAt", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsConversations.put("systemPrompt", TableInfo.Column("systemPrompt", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysConversations: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysConversations.add(TableInfo.ForeignKey("spaces", "CASCADE", "NO ACTION",
            listOf("spaceId"), listOf("id")))
        val _indicesConversations: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesConversations.add(TableInfo.Index("index_conversations_spaceId", false,
            listOf("spaceId"), listOf("ASC")))
        val _infoConversations: TableInfo = TableInfo("conversations", _columnsConversations,
            _foreignKeysConversations, _indicesConversations)
        val _existingConversations: TableInfo = read(connection, "conversations")
        if (!_infoConversations.equals(_existingConversations)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |conversations(com.openclaw.ai.data.db.entity.ConversationEntity).
              | Expected:
              |""".trimMargin() + _infoConversations + """
              |
              | Found:
              |""".trimMargin() + _existingConversations)
        }
        val _columnsMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMessages.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("conversationId", TableInfo.Column("conversationId", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("mediaUri", TableInfo.Column("mediaUri", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("toolName", TableInfo.Column("toolName", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("toolParams", TableInfo.Column("toolParams", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("toolResult", TableInfo.Column("toolResult", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMessages.put("tokens", TableInfo.Column("tokens", "INTEGER", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysMessages.add(TableInfo.ForeignKey("conversations", "CASCADE", "NO ACTION",
            listOf("conversationId"), listOf("id")))
        val _indicesMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesMessages.add(TableInfo.Index("index_messages_conversationId", false,
            listOf("conversationId"), listOf("ASC")))
        val _infoMessages: TableInfo = TableInfo("messages", _columnsMessages, _foreignKeysMessages,
            _indicesMessages)
        val _existingMessages: TableInfo = read(connection, "messages")
        if (!_infoMessages.equals(_existingMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |messages(com.openclaw.ai.data.db.entity.MessageEntity).
              | Expected:
              |""".trimMargin() + _infoMessages + """
              |
              | Found:
              |""".trimMargin() + _existingMessages)
        }
        val _columnsSpaces: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSpaces.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("emoji", TableInfo.Column("emoji", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("systemPrompt", TableInfo.Column("systemPrompt", "TEXT", false, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("lastUsedAt", TableInfo.Column("lastUsedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSpaces.put("sortOrder", TableInfo.Column("sortOrder", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSpaces: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSpaces: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSpaces: TableInfo = TableInfo("spaces", _columnsSpaces, _foreignKeysSpaces,
            _indicesSpaces)
        val _existingSpaces: TableInfo = read(connection, "spaces")
        if (!_infoSpaces.equals(_existingSpaces)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |spaces(com.openclaw.ai.data.db.entity.SpaceEntity).
              | Expected:
              |""".trimMargin() + _infoSpaces + """
              |
              | Found:
              |""".trimMargin() + _existingSpaces)
        }
        val _columnsPerChatSettings: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPerChatSettings.put("conversationId", TableInfo.Column("conversationId", "TEXT",
            true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("temperature", TableInfo.Column("temperature", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("topK", TableInfo.Column("topK", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("topP", TableInfo.Column("topP", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("maxTokens", TableInfo.Column("maxTokens", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("preferredModelId", TableInfo.Column("preferredModelId", "TEXT",
            false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPerChatSettings.put("enabledTools", TableInfo.Column("enabledTools", "TEXT", false,
            0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPerChatSettings: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysPerChatSettings.add(TableInfo.ForeignKey("conversations", "CASCADE",
            "NO ACTION", listOf("conversationId"), listOf("id")))
        val _indicesPerChatSettings: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPerChatSettings: TableInfo = TableInfo("per_chat_settings",
            _columnsPerChatSettings, _foreignKeysPerChatSettings, _indicesPerChatSettings)
        val _existingPerChatSettings: TableInfo = read(connection, "per_chat_settings")
        if (!_infoPerChatSettings.equals(_existingPerChatSettings)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |per_chat_settings(com.openclaw.ai.data.db.entity.PerChatSettingsEntity).
              | Expected:
              |""".trimMargin() + _infoPerChatSettings + """
              |
              | Found:
              |""".trimMargin() + _existingPerChatSettings)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "conversations", "messages",
        "spaces", "per_chat_settings")
  }

  public override fun clearAllTables() {
    super.performClear(true, "conversations", "messages", "spaces", "per_chat_settings")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ConversationDao::class, ConversationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(MessageDao::class, MessageDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SpaceDao::class, SpaceDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(PerChatSettingsDao::class,
        PerChatSettingsDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun conversationDao(): ConversationDao = _conversationDao.value

  public override fun messageDao(): MessageDao = _messageDao.value

  public override fun spaceDao(): SpaceDao = _spaceDao.value

  public override fun perChatSettingsDao(): PerChatSettingsDao = _perChatSettingsDao.value
}
