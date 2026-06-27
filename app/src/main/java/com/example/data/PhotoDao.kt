package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    
    // Photos
    @Query("SELECT * FROM edited_photos ORDER BY dateModified DESC")
    fun getAllPhotos(): Flow<List<EditedPhoto>>
    
    @Query("SELECT * FROM edited_photos WHERE id = :id")
    suspend fun getPhotoById(id: Int): EditedPhoto?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: EditedPhoto): Long
    
    @Update
    suspend fun updatePhoto(photo: EditedPhoto)
    
    @Delete
    suspend fun deletePhoto(photo: EditedPhoto)
    
    @Query("DELETE FROM edited_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Int)
    
    // Presets
    @Query("SELECT * FROM custom_presets ORDER BY dateCreated DESC")
    fun getAllPresets(): Flow<List<CustomPreset>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: CustomPreset): Long
    
    @Delete
    suspend fun deletePreset(preset: CustomPreset)
}
