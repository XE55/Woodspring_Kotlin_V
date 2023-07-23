package com.example.myapplication

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonDAO {
    @Insert
    fun insert(person: Person?)

    @Update
    fun update(person: Person?)

    @Delete
    fun delete(person: Person?)

    //delete all
    @Query("delete from Person")
    fun deleteAll()

    @get:Query("select * from Person order by name, firstname")
    val allPerson: List<Person>

    //select only where IsFavorite is true
    @get:Query("select * from Person where IsFavorite = 1 order by name, firstname")
    val allFavoritePerson: List<Person>


    //select person using search on firstname and name
    @Query("select * from Person where firstname like :search or name like :search order by name, firstname")
    fun searchPerson(search: String?): List<Person>
    //Select all but display IsFavorite first
    @get:Query("select * from Person order by IsFavorite desc")
    val allPersonByFavorite: List<Person?>?

    //get total number
    @get:Query("select count(*) from Person")
    val total: Int

    //get total number of favorite
    @get:Query("select count(*) from Person where IsFavorite = 1")
    val totalFavorite: Int

    //getpersonbyname
    @Query("select * from Person where firstname = :firstname and name = :name")
    fun getPersonByName(firstname: String?, name: String?): Person?

    //set state of IsFavorite to !IsFavorite
    @Query("update Person set IsFavorite = not IsFavorite where id = :id")
    fun toggleFavorite(id: Int)

    //delete only favorites
    @Query("delete from Person where IsFavorite = 1")
    fun deleteAllFavorite()

    //unfavorite all
    @Query("update Person set IsFavorite = 0")
    fun unFavoriteAll()
}