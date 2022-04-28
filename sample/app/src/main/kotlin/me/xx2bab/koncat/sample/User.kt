package me.xx2bab.koncat.sample

import androidx.room.*
import kotlin.reflect.KClass

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?
)

val classes: Array<KClass<*>> = arrayOf(User::class)

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase()