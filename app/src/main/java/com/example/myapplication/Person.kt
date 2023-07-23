package com.example.myapplication

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Person")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    val name: String,

    val firstname: String,

    var IsFavorite: Boolean,
) {

    fun IsFavorite(b: Boolean) {
        IsFavorite = b
    }

}
