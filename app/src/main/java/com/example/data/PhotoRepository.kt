package com.example.data

import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoDao: PhotoDao) {
    
    val allPhotos: Flow<List<EditedPhoto>> = photoDao.getAllPhotos()
    val allPresets: Flow<List<CustomPreset>> = photoDao.getAllPresets()
    
    suspend fun getPhotoById(id: Int): EditedPhoto? {
        return photoDao.getPhotoById(id)
    }
    
    suspend fun savePhoto(photo: EditedPhoto): Long {
        return photoDao.insertPhoto(photo)
    }
    
    suspend fun updatePhoto(photo: EditedPhoto) {
        photoDao.updatePhoto(photo)
    }
    
    suspend fun deletePhoto(photo: EditedPhoto) {
        photoDao.deletePhoto(photo)
    }
    
    suspend fun deletePhotoById(id: Int) {
        photoDao.deletePhotoById(id)
    }
    
    suspend fun savePreset(preset: CustomPreset): Long {
        return photoDao.insertPreset(preset)
    }
    
    suspend fun deletePreset(preset: CustomPreset) {
        photoDao.deletePreset(preset)
    }
}
