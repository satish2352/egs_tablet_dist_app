package com.sipl.egstabdistribution.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sipl.egstabdistribution.database.entity.User
import com.sipl.egstabdistribution.database.model.UsersWithAreaNames

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: User) : Long

    @Update
    suspend fun updateUser(user: User):Int

    @Delete
    suspend fun deleteLabour(user: User)

    @Query("SELECT * FROM users WHERE isSynced='0'")
    fun getAllUsers(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getLabourById(id: Int): User



    @Query("SELECT COUNT(*) FROM users WHERE isSynced='0'")
    suspend fun getUsersCount(): Int

    @Query("SELECT l.*, village.name AS villageName, district.name AS districtName, taluka.name AS talukaName " +
            "FROM users l " +
            "LEFT JOIN area AS village ON l.village = village.location_id " +
            "LEFT JOIN area AS district ON l.district = district.location_id " +
            "LEFT JOIN area AS taluka ON l.taluka = taluka.location_id WHERE isSynced='0' ORDER BY id DESC")
    suspend fun getUsersWithAreaNames(): List<UsersWithAreaNames>


    @Query("SELECT l.*, village.name AS villageName, district.name AS districtName, taluka.name AS talukaName " +
            "FROM users l " +
            "LEFT JOIN area AS village ON l.village = village.location_id " +
            "LEFT JOIN area AS district ON l.district = district.location_id " +
            "LEFT JOIN area AS taluka ON l.taluka = taluka.location_id " +
            "WHERE l.id = :labourId AND isSynced='0'")
    suspend fun getUsersWithAreaNamesById(labourId: Int): UsersWithAreaNames?
}