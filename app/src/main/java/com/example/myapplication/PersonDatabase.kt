package com.example.myapplication

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Person::class], version = 1)
abstract class PersonDatabase : RoomDatabase() {
    abstract fun personDAO(): PersonDAO
    fun personExists(firstName: String?, lastName: String?): Boolean {
        val personDAO = personDAO() // Get the PersonDAO instance

        // Check if a person with the given first name and last name exists in the database
        return personDAO.getPersonByName(firstName, lastName) != null
    }
}