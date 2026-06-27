package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_presets")
data class CustomPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dateCreated: Long = System.currentTimeMillis(),
    
    // Light
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val whites: Float = 0f,
    val blacks: Float = 0f,
    
    // Color
    val temp: Float = 0f,
    val tint: Float = 0f,
    val saturation: Float = 0f,
    val vibrance: Float = 0f,
    
    // HSL Hue
    val hueRed: Float = 0f,
    val hueOrange: Float = 0f,
    val hueYellow: Float = 0f,
    val hueGreen: Float = 0f,
    val hueAqua: Float = 0f,
    val hueBlue: Float = 0f,
    val huePurple: Float = 0f,
    val hueMagenta: Float = 0f,
    
    // HSL Saturation
    val satRed: Float = 0f,
    val satOrange: Float = 0f,
    val satYellow: Float = 0f,
    val satGreen: Float = 0f,
    val satAqua: Float = 0f,
    val satBlue: Float = 0f,
    val satPurple: Float = 0f,
    val satMagenta: Float = 0f,
    
    // HSL Luminance
    val lumRed: Float = 0f,
    val lumOrange: Float = 0f,
    val lumYellow: Float = 0f,
    val lumGreen: Float = 0f,
    val lumAqua: Float = 0f,
    val lumBlue: Float = 0f,
    val lumPurple: Float = 0f,
    val lumMagenta: Float = 0f,
    
    // Effects
    val texture: Float = 0f,
    val clarity: Float = 0f,
    val dehaze: Float = 0f,
    val vignette: Float = 0f,
    
    // Detail
    val sharpening: Float = 0f,
    val noiseReduction: Float = 0f
)
