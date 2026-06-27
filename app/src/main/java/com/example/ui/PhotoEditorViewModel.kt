package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomPreset
import com.example.data.EditedPhoto
import com.example.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Stack

class PhotoEditorViewModel(
    application: Application,
    private val repository: PhotoRepository
) : AndroidViewModel(application) {

    // All photos edited in studio
    val studioPhotos: StateFlow<List<EditedPhoto>> = repository.allPhotos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Custom Presets
    val customPresets: StateFlow<List<CustomPreset>> = repository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current editing photo state
    private val _currentPhoto = MutableStateFlow<EditedPhoto?>(null)
    val currentPhoto: StateFlow<EditedPhoto?> = _currentPhoto.asStateFlow()

    // Undo / Redo Stacks
    private val undoStack = Stack<EditedPhoto>()
    private val redoStack = Stack<EditedPhoto>()

    // History log of actions applied
    private val _editHistory = MutableStateFlow<List<String>>(listOf("Photo Loaded"))
    val editHistory: StateFlow<List<String>> = _editHistory.asStateFlow()

    // Active Category in Editor
    private val _activeCategory = MutableStateFlow("Presets")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    // Selected Color for Color Mix (Red, Orange, Yellow, Green, Aqua, Blue, Purple, Magenta)
    private val _selectedMixColor = MutableStateFlow("Red")
    val selectedMixColor: StateFlow<String> = _selectedMixColor.asStateFlow()

    // Selected Crop Aspect Ratio
    private val _selectedCropRatio = MutableStateFlow("Free")
    val selectedCropRatio: StateFlow<String> = _selectedCropRatio.asStateFlow()

    // Original unedited photo for "Compare / Before" view
    private val _originalPhoto = MutableStateFlow<EditedPhoto?>(null)
    val originalPhoto: StateFlow<EditedPhoto?> = _originalPhoto.asStateFlow()

    fun loadPhoto(photo: EditedPhoto) {
        _currentPhoto.value = photo
        _originalPhoto.value = photo.copy(
            exposure = 0f, contrast = 0f, highlights = 0f, shadows = 0f, whites = 0f, blacks = 0f,
            temp = 0f, tint = 0f, saturation = 0f, vibrance = 0f,
            hueRed = 0f, satRed = 0f, lumRed = 0f,
            hueOrange = 0f, satOrange = 0f, lumOrange = 0f,
            hueYellow = 0f, satYellow = 0f, lumYellow = 0f,
            hueGreen = 0f, satGreen = 0f, lumGreen = 0f,
            hueAqua = 0f, satAqua = 0f, lumAqua = 0f,
            hueBlue = 0f, satBlue = 0f, lumBlue = 0f,
            huePurple = 0f, satPurple = 0f, lumPurple = 0f,
            hueMagenta = 0f, satMagenta = 0f, lumMagenta = 0f,
            texture = 0f, clarity = 0f, dehaze = 0f, vignette = 0f,
            sharpening = 0f, noiseReduction = 0f,
            cropRatio = "Free", rotation = 0f, rotation90 = 0,
            isFlippedHorizontally = false, isFlippedVertically = false,
            activePresetName = "None"
        )
        undoStack.clear()
        redoStack.clear()
        _editHistory.value = listOf("Imported ${photo.title}")
    }

    fun startNewEditSession(title: String, imagePath: String, isLocalUri: Boolean = false) {
        val newPhoto = EditedPhoto(
            title = title,
            imagePath = imagePath,
            isLocalUri = isLocalUri
        )
        loadPhoto(newPhoto)
    }

    // Helper to commit change to undo stack
    private fun commitState(actionName: String) {
        _currentPhoto.value?.let { current ->
            undoStack.push(current.copy())
            redoStack.clear()
            _editHistory.value = _editHistory.value + actionName
        }
    }

    fun updateActiveCategory(category: String) {
        _activeCategory.value = category
    }

    fun updateMixColor(color: String) {
        _selectedMixColor.value = color
    }

    // Slider Adjustments
    fun updateExposure(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(exposure = value)
    }

    fun finishExposureChange() {
        commitState("Adjusted Exposure")
    }

    fun updateContrast(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(contrast = value)
    }

    fun finishContrastChange() {
        commitState("Adjusted Contrast")
    }

    fun updateHighlights(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(highlights = value)
    }

    fun finishHighlightsChange() {
        commitState("Adjusted Highlights")
    }

    fun updateShadows(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(shadows = value)
    }

    fun finishShadowsChange() {
        commitState("Adjusted Shadows")
    }

    fun updateWhites(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(whites = value)
    }

    fun finishWhitesChange() {
        commitState("Adjusted Whites")
    }

    fun updateBlacks(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(blacks = value)
    }

    fun finishBlacksChange() {
        commitState("Adjusted Blacks")
    }

    // Color Sliders
    fun updateTemp(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(temp = value)
    }

    fun finishTempChange() {
        commitState("Adjusted Temperature")
    }

    fun updateTint(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(tint = value)
    }

    fun finishTintChange() {
        commitState("Adjusted Tint")
    }

    fun updateSaturation(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(saturation = value)
    }

    fun finishSaturationChange() {
        commitState("Adjusted Saturation")
    }

    fun updateVibrance(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(vibrance = value)
    }

    fun finishVibranceChange() {
        commitState("Adjusted Vibrance")
    }

    // HSL Colors
    fun updateHslHue(color: String, value: Float) {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = when (color) {
            "Red" -> current.copy(hueRed = value)
            "Orange" -> current.copy(hueOrange = value)
            "Yellow" -> current.copy(hueYellow = value)
            "Green" -> current.copy(hueGreen = value)
            "Aqua" -> current.copy(hueAqua = value)
            "Blue" -> current.copy(hueBlue = value)
            "Purple" -> current.copy(huePurple = value)
            "Magenta" -> current.copy(hueMagenta = value)
            else -> current
        }
    }

    fun finishHslHueChange(color: String) {
        commitState("Adjusted $color Hue")
    }

    fun updateHslSat(color: String, value: Float) {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = when (color) {
            "Red" -> current.copy(satRed = value)
            "Orange" -> current.copy(satOrange = value)
            "Yellow" -> current.copy(satYellow = value)
            "Green" -> current.copy(satGreen = value)
            "Aqua" -> current.copy(satAqua = value)
            "Blue" -> current.copy(satBlue = value)
            "Purple" -> current.copy(satPurple = value)
            "Magenta" -> current.copy(satMagenta = value)
            else -> current
        }
    }

    fun finishHslSatChange(color: String) {
        commitState("Adjusted $color Saturation")
    }

    fun updateHslLum(color: String, value: Float) {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = when (color) {
            "Red" -> current.copy(lumRed = value)
            "Orange" -> current.copy(lumOrange = value)
            "Yellow" -> current.copy(lumYellow = value)
            "Green" -> current.copy(lumGreen = value)
            "Aqua" -> current.copy(lumAqua = value)
            "Blue" -> current.copy(lumBlue = value)
            "Purple" -> current.copy(lumPurple = value)
            "Magenta" -> current.copy(lumMagenta = value)
            else -> current
        }
    }

    fun finishHslLumChange(color: String) {
        commitState("Adjusted $color Luminance")
    }

    // Effects Sliders
    fun updateTexture(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(texture = value)
    }

    fun finishTextureChange() {
        commitState("Adjusted Texture")
    }

    fun updateClarity(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(clarity = value)
    }

    fun finishClarityChange() {
        commitState("Adjusted Clarity")
    }

    fun updateDehaze(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(dehaze = value)
    }

    fun finishDehazeChange() {
        commitState("Adjusted Dehaze")
    }

    fun updateVignette(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(vignette = value)
    }

    fun finishVignetteChange() {
        commitState("Adjusted Vignette")
    }

    // Detail Sliders
    fun updateSharpening(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(sharpening = value)
    }

    fun finishSharpeningChange() {
        commitState("Adjusted Sharpening")
    }

    fun updateNoiseReduction(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(noiseReduction = value)
    }

    fun finishNoiseReductionChange() {
        commitState("Adjusted Noise Reduction")
    }

    // Crop / Rotate
    fun updateCropRatio(ratio: String) {
        _selectedCropRatio.value = ratio
        _currentPhoto.value = _currentPhoto.value?.copy(cropRatio = ratio)
        commitState("Set Crop Ratio to $ratio")
    }

    fun updateRotation(value: Float) {
        _currentPhoto.value = _currentPhoto.value?.copy(rotation = value)
    }

    fun finishRotationChange() {
        commitState("Fine-tuned Rotation")
    }

    fun rotate90() {
        val current = _currentPhoto.value ?: return
        val nextRotation = (current.rotation90 + 90) % 360
        _currentPhoto.value = current.copy(rotation90 = nextRotation)
        commitState("Rotated 90°")
    }

    fun flipHorizontal() {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = current.copy(isFlippedHorizontally = !current.isFlippedHorizontally)
        commitState("Flipped Horizontally")
    }

    fun flipVertical() {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = current.copy(isFlippedVertically = !current.isFlippedVertically)
        commitState("Flipped Vertically")
    }

    // Auto Enhance / Auto Levels
    fun autoEnhance() {
        val current = _currentPhoto.value ?: return
        // Intelligent auto balance
        _currentPhoto.value = current.copy(
            exposure = 0.15f,
            contrast = 0.20f,
            highlights = -0.15f,
            shadows = 0.25f,
            saturation = 0.12f,
            vibrance = 0.18f,
            clarity = 0.10f,
            sharpening = 0.20f
        )
        commitState("AI Auto-Enhanced")
    }

    // Preset Applicator
    fun applyPreset(presetName: String) {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = when (presetName) {
            "Warm Sun" -> current.copy(
                temp = 0.35f, tint = 0.05f, exposure = 0.1f, contrast = 0.12f, saturation = 0.15f,
                activePresetName = presetName
            )
            "Cool Steel" -> current.copy(
                temp = -0.4f, tint = 0.12f, exposure = 0.05f, contrast = 0.15f, saturation = -0.1f,
                activePresetName = presetName
            )
            "Cyber Neon" -> current.copy(
                temp = -0.15f, tint = 0.45f, contrast = 0.25f, saturation = 0.35f, vignette = 0.25f,
                activePresetName = presetName
            )
            "Cinematic B&W" -> current.copy(
                saturation = -1.0f, contrast = 0.4f, exposure = -0.05f, highlights = -0.15f, shadows = 0.2f,
                activePresetName = presetName
            )
            "Vintage Film" -> current.copy(
                temp = 0.22f, tint = 0.18f, contrast = -0.08f, exposure = 0.08f, vignette = 0.3f, saturation = -0.12f, texture = 0.35f,
                activePresetName = presetName
            )
            "Vivid HDR" -> current.copy(
                exposure = 0.12f, contrast = 0.28f, highlights = -0.22f, shadows = 0.45f, saturation = 0.28f, vibrance = 0.32f,
                activePresetName = presetName
            )
            "Soft Pastel" -> current.copy(
                contrast = -0.15f, exposure = 0.18f, saturation = -0.1f, vibrance = 0.08f, highlights = 0.15f, shadows = 0.15f,
                activePresetName = presetName
            )
            "Dreamy Fade" -> current.copy(
                exposure = 0.15f, contrast = -0.25f, shadows = 0.35f, saturation = -0.15f, temp = 0.1f, vignette = 0.15f,
                activePresetName = presetName
            )
            else -> current // Original / None
        }
        commitState("Applied Preset: $presetName")
    }

    // Applying a Custom Saved Preset
    fun applyCustomPreset(preset: CustomPreset) {
        val current = _currentPhoto.value ?: return
        _currentPhoto.value = current.copy(
            exposure = preset.exposure,
            contrast = preset.contrast,
            highlights = preset.highlights,
            shadows = preset.shadows,
            whites = preset.whites,
            blacks = preset.blacks,
            temp = preset.temp,
            tint = preset.tint,
            saturation = preset.saturation,
            vibrance = preset.vibrance,
            hueRed = preset.hueRed,
            hueOrange = preset.hueOrange,
            hueYellow = preset.hueYellow,
            hueGreen = preset.hueGreen,
            hueAqua = preset.hueAqua,
            hueBlue = preset.hueBlue,
            huePurple = preset.huePurple,
            hueMagenta = preset.hueMagenta,
            satRed = preset.satRed,
            satOrange = preset.satOrange,
            satYellow = preset.satYellow,
            satGreen = preset.satGreen,
            satAqua = preset.satAqua,
            satBlue = preset.satBlue,
            satPurple = preset.satPurple,
            satMagenta = preset.satMagenta,
            lumRed = preset.lumRed,
            lumOrange = preset.lumOrange,
            lumYellow = preset.lumYellow,
            lumGreen = preset.lumGreen,
            lumAqua = preset.lumAqua,
            lumBlue = preset.lumBlue,
            lumPurple = preset.lumPurple,
            lumMagenta = preset.lumMagenta,
            texture = preset.texture,
            clarity = preset.clarity,
            dehaze = preset.dehaze,
            vignette = preset.vignette,
            sharpening = preset.sharpening,
            noiseReduction = preset.noiseReduction,
            activePresetName = preset.name
        )
        commitState("Applied Custom Preset: ${preset.name}")
    }

    // Save Preset Creator
    fun saveAsPreset(name: String) {
        val current = _currentPhoto.value ?: return
        val newPreset = CustomPreset(
            name = name,
            exposure = current.exposure,
            contrast = current.contrast,
            highlights = current.highlights,
            shadows = current.shadows,
            whites = current.whites,
            blacks = current.blacks,
            temp = current.temp,
            tint = current.tint,
            saturation = current.saturation,
            vibrance = current.vibrance,
            hueRed = current.hueRed,
            hueOrange = current.hueOrange,
            hueYellow = current.hueYellow,
            hueGreen = current.hueGreen,
            hueAqua = current.hueAqua,
            hueBlue = current.hueBlue,
            huePurple = current.huePurple,
            hueMagenta = current.hueMagenta,
            satRed = current.satRed,
            satOrange = current.satOrange,
            satYellow = current.satYellow,
            satGreen = current.satGreen,
            satAqua = current.satAqua,
            satBlue = current.satBlue,
            satPurple = current.satPurple,
            satMagenta = current.satMagenta,
            lumRed = current.lumRed,
            lumOrange = current.lumOrange,
            lumYellow = current.lumYellow,
            lumGreen = current.lumGreen,
            lumAqua = current.lumAqua,
            lumBlue = current.lumBlue,
            lumPurple = current.lumPurple,
            lumMagenta = current.lumMagenta,
            texture = current.texture,
            clarity = current.clarity,
            dehaze = current.dehaze,
            vignette = current.vignette,
            sharpening = current.sharpening,
            noiseReduction = current.noiseReduction
        )
        viewModelScope.launch {
            repository.savePreset(newPreset)
        }
    }

    fun deletePreset(preset: CustomPreset) {
        viewModelScope.launch {
            repository.deletePreset(preset)
        }
    }

    // Undo / Redo / Reset Operations
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _currentPhoto.value ?: return
        redoStack.push(current)
        val previous = undoStack.pop()
        _currentPhoto.value = previous
        
        // Remove last item from history description or mark undo
        if (_editHistory.value.isNotEmpty()) {
            _editHistory.value = _editHistory.value.dropLast(1)
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _currentPhoto.value ?: return
        undoStack.push(current)
        val next = redoStack.pop()
        _currentPhoto.value = next
        _editHistory.value = _editHistory.value + "Redid Action"
    }

    fun resetEdits() {
        val current = _currentPhoto.value ?: return
        undoStack.push(current.copy())
        redoStack.clear()
        _currentPhoto.value = current.copy(
            exposure = 0f, contrast = 0f, highlights = 0f, shadows = 0f, whites = 0f, blacks = 0f,
            temp = 0f, tint = 0f, saturation = 0f, vibrance = 0f,
            hueRed = 0f, satRed = 0f, lumRed = 0f,
            hueOrange = 0f, satOrange = 0f, lumOrange = 0f,
            hueYellow = 0f, satYellow = 0f, lumYellow = 0f,
            hueGreen = 0f, satGreen = 0f, lumGreen = 0f,
            hueAqua = 0f, satAqua = 0f, lumAqua = 0f,
            hueBlue = 0f, satBlue = 0f, lumBlue = 0f,
            huePurple = 0f, satPurple = 0f, lumPurple = 0f,
            hueMagenta = 0f, satMagenta = 0f, lumMagenta = 0f,
            texture = 0f, clarity = 0f, dehaze = 0f, vignette = 0f,
            sharpening = 0f, noiseReduction = 0f,
            cropRatio = "Free", rotation = 0f, rotation90 = 0,
            isFlippedHorizontally = false, isFlippedVertically = false,
            activePresetName = "None"
        )
        _editHistory.value = _editHistory.value + "Reset All Edits"
    }

    // Save final work to local Room DB (Studio Photos)
    fun saveEditedPhotoToStudio(onSuccess: () -> Unit) {
        val current = _currentPhoto.value ?: return
        val photoToSave = current.copy(
            dateModified = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.savePhoto(photoToSave)
            onSuccess()
        }
    }

    fun deletePhotoFromStudio(id: Int) {
        viewModelScope.launch {
            repository.deletePhotoById(id)
        }
    }
}

class PhotoEditorViewModelFactory(
    private val application: Application,
    private val repository: PhotoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoEditorViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
