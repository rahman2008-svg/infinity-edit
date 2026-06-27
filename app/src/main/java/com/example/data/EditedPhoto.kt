package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edited_photos")
data class EditedPhoto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val imagePath: String, // Resource name (e.g. "img_sample_landscape") or Uri string
    val isLocalUri: Boolean = false,
    val dateModified: Long = System.currentTimeMillis(),
    
    // Light adjustments
    val exposure: Float = 0f,      // -1f to 1f
    val contrast: Float = 0f,      // -1f to 1f
    val highlights: Float = 0f,    // -1f to 1f
    val shadows: Float = 0f,       // -1f to 1f
    val whites: Float = 0f,        // -1f to 1f
    val blacks: Float = 0f,        // -1f to 1f
    
    // Color adjustments
    val temp: Float = 0f,          // -1f (cool) to 1f (warm)
    val tint: Float = 0f,          // -1f (green) to 1f (magenta)
    val saturation: Float = 0f,    // -1f to 1f
    val vibrance: Float = 0f,      // -1f to 1f
    
    // Color Mix (HSL) - Red
    val hueRed: Float = 0f,
    val satRed: Float = 0f,
    val lumRed: Float = 0f,
    
    // Orange (crucial for skin tones in Lightroom)
    val hueOrange: Float = 0f,
    val satOrange: Float = 0f,
    val lumOrange: Float = 0f,
    
    // Yellow
    val hueYellow: Float = 0f,
    val satYellow: Float = 0f,
    val lumYellow: Float = 0f,
    
    // Green
    val hueGreen: Float = 0f,
    val satGreen: Float = 0f,
    val lumGreen: Float = 0f,
    
    // Aqua/Cyan
    val hueAqua: Float = 0f,
    val satAqua: Float = 0f,
    val lumAqua: Float = 0f,
    
    // Blue
    val hueBlue: Float = 0f,
    val satBlue: Float = 0f,
    val lumBlue: Float = 0f,
    
    // Purple
    val huePurple: Float = 0f,
    val satPurple: Float = 0f,
    val lumPurple: Float = 0f,
    
    // Magenta
    val hueMagenta: Float = 0f,
    val satMagenta: Float = 0f,
    val lumMagenta: Float = 0f,
    
    // Effects
    val texture: Float = 0f,       // -1f to 1f
    val clarity: Float = 0f,       // -1f to 1f
    val dehaze: Float = 0f,        // -1f to 1f
    val vignette: Float = 0f,      // 0f to 1f (always positive dark border strength)
    
    // Detail
    val sharpening: Float = 0f,    // 0f to 1f
    val noiseReduction: Float = 0f, // 0f to 1f
    
    // Geometry/Crop
    val cropRatio: String = "Free", // "Free", "1:1", "4:3", "16:9", "3:2", "5:7"
    val rotation: Float = 0f,      // -45f to 45f fine rotation
    val rotation90: Int = 0,       // 0, 90, 180, 270 degrees
    val isFlippedHorizontally: Boolean = false,
    val isFlippedVertically: Boolean = false,
    
    // Active Filter/Preset Name
    val activePresetName: String = "None"
)
