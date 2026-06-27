package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CustomPreset
import com.example.data.EditedPhoto
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: PhotoEditorViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentPhoto by viewModel.currentPhoto.collectAsState()
    val originalPhoto by viewModel.originalPhoto.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val editHistory by viewModel.editHistory.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()
    
    // UI Local state
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var isComparing by remember { mutableStateOf(false) } // Touch and hold comparison

    if (currentPhoto == null) {
        Column(
            modifier = Modifier.fillMaxSize().background(DarkBg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = CyanAccent)
        }
        return
    }

    val photo = currentPhoto!!

    // Computed Color Matrix for actual filter and color rendering
    val colorMatrix = rememberAdjustedColorMatrix(if (isComparing) originalPhoto!! else photo)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = photo.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextLight
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextLight
                        )
                    }
                },
                actions = {
                    // Auto-enhance (Sparkles icon)
                    IconButton(
                        onClick = { viewModel.autoEnhance() },
                        modifier = Modifier.testTag("auto_enhance_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Auto-Enhance",
                            tint = CyanAccent
                        )
                    }
                    
                    // Undo
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = viewModel.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (viewModel.canUndo) TextLight else TextGray.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Redo
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = viewModel.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (viewModel.canRedo) TextLight else TextGray.copy(alpha = 0.3f)
                        )
                    }

                    // Save / Export
                    Button(
                        onClick = {
                            viewModel.saveEditedPhotoToStudio {
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(32.dp)
                            .testTag("save_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Canvas viewport (occupies top 55% of editor space)
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(16.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isComparing = true
                                tryAwaitRelease()
                                isComparing = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Apply crop aspect ratio scaling box
                val ratioModifier = when (photo.cropRatio) {
                    "1:1" -> Modifier.aspectRatio(1f)
                    "4:3" -> Modifier.aspectRatio(4f / 3f)
                    "16:9" -> Modifier.aspectRatio(16f / 9f)
                    "3:2" -> Modifier.aspectRatio(3f / 2f)
                    "5:7" -> Modifier.aspectRatio(5f / 7f)
                    else -> Modifier // Free / unrestricted
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(ratioModifier)
                        .clip(RoundedCornerShape(8.dp))
                        .graphicsLayer {
                            // Rotation and flips
                            rotationZ = photo.rotation + photo.rotation90.toFloat()
                            scaleX = if (photo.isFlippedHorizontally) -1f else 1f
                            scaleY = if (photo.isFlippedVertically) -1f else 1f
                        }
                        .drawBehind {
                            // Apply Vignette overlay dynamically
                            if (photo.vignette > 0f && !isComparing) {
                                val radius = size.minDimension * 0.85f
                                val brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = photo.vignette * 0.92f)
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = radius
                                )
                                drawRect(brush = brush)
                            }
                        }
                ) {
                    if (photo.isLocalUri) {
                        AsyncImage(
                            model = photo.imagePath,
                            contentDescription = "Editing Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        )
                    } else {
                        val resId = when (photo.imagePath) {
                            "img_sample_landscape" -> R.drawable.img_sample_landscape
                            "img_sample_portrait" -> R.drawable.img_sample_portrait
                            "img_sample_urban" -> R.drawable.img_sample_urban
                            else -> R.drawable.img_app_icon
                        }
                        Image(
                            painter = painterResource(id = resId),
                            contentDescription = "Editing Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.colorMatrix(colorMatrix)
                        )
                    }
                }
                
                // Compare hint overlay at the top of canvas
                Text(
                    text = if (isComparing) "BEFORE (ORIGINAL)" else "HOLD SCREEN TO COMPARE",
                    color = if (isComparing) OrangeAccent else TextGray.copy(alpha = 0.6f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                if (photo.activePresetName != "None" && !isComparing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Preset: ${photo.activePresetName}",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Adjustment Sliders Panel (occupies bottom 35% of editor space)
            Box(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .border(1.dp, DarkCard)
                    .padding(16.dp)
            ) {
                when (activeCategory) {
                    "Presets" -> PresetsPanel(
                        viewModel = viewModel,
                        currentPhoto = photo,
                        customPresets = customPresets,
                        onSavePresetClick = { showSavePresetDialog = true }
                    )
                    "Light" -> LightPanel(viewModel = viewModel, photo = photo)
                    "Color" -> ColorPanel(viewModel = viewModel, photo = photo)
                    "Effects" -> EffectsPanel(viewModel = viewModel, photo = photo)
                    "Detail" -> DetailPanel(viewModel = viewModel, photo = photo)
                    "Crop" -> CropPanel(viewModel = viewModel, photo = photo)
                    "History" -> HistoryPanel(viewModel = viewModel, editHistory = editHistory)
                }
            }

            // Horizontal Category Selector Bar (occupies bottom 10% of editor space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(DarkBg)
                    .border(1.dp, DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryItem(
                        title = "Presets",
                        icon = Icons.Default.FilterBAndW,
                        isActive = activeCategory == "Presets",
                        onClick = { viewModel.updateActiveCategory("Presets") }
                    )
                    CategoryItem(
                        title = "Crop",
                        icon = Icons.Default.Crop,
                        isActive = activeCategory == "Crop",
                        onClick = { viewModel.updateActiveCategory("Crop") }
                    )
                    CategoryItem(
                        title = "Light",
                        icon = Icons.Default.LightMode,
                        isActive = activeCategory == "Light",
                        onClick = { viewModel.updateActiveCategory("Light") }
                    )
                    CategoryItem(
                        title = "Color",
                        icon = Icons.Default.Palette,
                        isActive = activeCategory == "Color",
                        onClick = { viewModel.updateActiveCategory("Color") }
                    )
                    CategoryItem(
                        title = "Effects",
                        icon = Icons.Default.Lens,
                        isActive = activeCategory == "Effects",
                        onClick = { viewModel.updateActiveCategory("Effects") }
                    )
                    CategoryItem(
                        title = "Detail",
                        icon = Icons.Default.Details,
                        isActive = activeCategory == "Detail",
                        onClick = { viewModel.updateActiveCategory("Detail") }
                    )
                    CategoryItem(
                        title = "History",
                        icon = Icons.Default.History,
                        isActive = activeCategory == "History",
                        onClick = { viewModel.updateActiveCategory("History") }
                    )
                    CategoryItem(
                        title = "Reset",
                        icon = Icons.Default.Refresh,
                        isActive = false,
                        onClick = { viewModel.resetEdits() }
                    )
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Save Custom Preset", color = TextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Enter a name for your custom preset. It will capture all current slider adjustments.",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            focusedLabelColor = CyanAccent,
                            cursorColor = CyanAccent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            viewModel.saveAsPreset(presetNameInput.trim())
                            presetNameInput = ""
                            showSavePresetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun CategoryItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(56.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = if (isActive) CyanAccent else TextGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) CyanAccent else TextGray,
            textAlign = TextAlign.Center
        )
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: PRESETS
// -----------------------------------------------------
@Composable
fun PresetsPanel(
    viewModel: PhotoEditorViewModel,
    currentPhoto: EditedPhoto,
    customPresets: List<CustomPreset>,
    onSavePresetClick: () -> Unit
) {
    val presets = listOf(
        "Original", "Warm Sun", "Cool Steel", "Cyber Neon", "Cinematic B&W",
        "Vintage Film", "Vivid HDR", "Soft Pastel", "Dreamy Fade"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PRESETS",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = TextGray
            )
            
            Button(
                onClick = onSavePresetClick,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.height(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Save Custom Preset",
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Current", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Custom Saved Presets list (if any exists)
        if (customPresets.isNotEmpty()) {
            Text(
                text = "YOUR PRESETS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(customPresets) { preset ->
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .height(42.dp)
                            .clickable { viewModel.applyCustomPreset(preset) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentPhoto.activePresetName == preset.name) CyanAccent.copy(alpha = 0.15f) else DarkCard
                        ),
                        border = if (currentPhoto.activePresetName == preset.name) BorderStroke(1.dp, CyanAccent) else null
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentPhoto.activePresetName == preset.name) CyanAccent else TextLight,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Preset",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable { viewModel.deletePreset(preset) }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Standard built-in presets
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(presets) { preset ->
                val isActive = currentPhoto.activePresetName == preset || (preset == "Original" && currentPhoto.activePresetName == "None")
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clickable { viewModel.applyPreset(preset) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                when (preset) {
                                    "Warm Sun" -> Brush.radialGradient(listOf(Color(0xFFFF9100), Color(0xFFE65100)))
                                    "Cool Steel" -> Brush.radialGradient(listOf(Color(0xFF90A4AE), Color(0xFF37474F)))
                                    "Cyber Neon" -> Brush.radialGradient(listOf(Color(0xFF00E5FF), Color(0xFFD500F9)))
                                    "Cinematic B&W" -> Brush.radialGradient(listOf(Color(0xFFE0E0E0), Color(0xFF212121)))
                                    "Vintage Film" -> Brush.radialGradient(listOf(Color(0xFF8D6E63), Color(0xFF3E2723)))
                                    "Vivid HDR" -> Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFFF4500)))
                                    "Soft Pastel" -> Brush.radialGradient(listOf(Color(0xFFF8BBD0), Color(0xFFB2DFDB)))
                                    "Dreamy Fade" -> Brush.radialGradient(listOf(Color(0xFFCE93D8), Color(0xFF80DEEA)))
                                    else -> Brush.radialGradient(listOf(Color.Gray, Color.DarkGray))
                                }
                            )
                            .border(
                                width = if (isActive) 2.dp else 1.dp,
                                color = if (isActive) CyanAccent else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = if (preset == "Original" || preset == "Cinematic B&W" || preset == "Soft Pastel") Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset,
                        fontSize = 9.sp,
                        color = if (isActive) CyanAccent else TextLight,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: LIGHT
// -----------------------------------------------------
@Composable
fun LightPanel(viewModel: PhotoEditorViewModel, photo: EditedPhoto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "LIGHT ADJUSTMENTS",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        AdjustmentSlider(
            label = "Exposure",
            value = photo.exposure,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateExposure(it) },
            onValueChangeFinished = { viewModel.finishExposureChange() }
        )
        AdjustmentSlider(
            label = "Contrast",
            value = photo.contrast,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateContrast(it) },
            onValueChangeFinished = { viewModel.finishContrastChange() }
        )
        AdjustmentSlider(
            label = "Highlights",
            value = photo.highlights,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateHighlights(it) },
            onValueChangeFinished = { viewModel.finishHighlightsChange() }
        )
        AdjustmentSlider(
            label = "Shadows",
            value = photo.shadows,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateShadows(it) },
            onValueChangeFinished = { viewModel.finishShadowsChange() }
        )
        AdjustmentSlider(
            label = "Whites",
            value = photo.whites,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateWhites(it) },
            onValueChangeFinished = { viewModel.finishWhitesChange() }
        )
        AdjustmentSlider(
            label = "Blacks",
            value = photo.blacks,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateBlacks(it) },
            onValueChangeFinished = { viewModel.finishBlacksChange() }
        )
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: COLOR & HSL MIX
// -----------------------------------------------------
@Composable
fun ColorPanel(viewModel: PhotoEditorViewModel, photo: EditedPhoto) {
    var showColorMix by remember { mutableStateOf(false) }
    val selectedMixColor by viewModel.selectedMixColor.collectAsState()

    if (showColorMix) {
        val colorsList = listOf("Red", "Orange", "Yellow", "Green", "Aqua", "Blue", "Purple", "Magenta")
        val activeMixColorVal = when (selectedMixColor) {
            "Red" -> Triple(photo.hueRed, photo.satRed, photo.lumRed)
            "Orange" -> Triple(photo.hueOrange, photo.satOrange, photo.lumOrange)
            "Yellow" -> Triple(photo.hueYellow, photo.satYellow, photo.lumYellow)
            "Green" -> Triple(photo.hueGreen, photo.satGreen, photo.lumGreen)
            "Aqua" -> Triple(photo.hueAqua, photo.satAqua, photo.lumAqua)
            "Blue" -> Triple(photo.hueBlue, photo.satBlue, photo.lumBlue)
            "Purple" -> Triple(photo.huePurple, photo.satPurple, photo.lumPurple)
            "Magenta" -> Triple(photo.hueMagenta, photo.satMagenta, photo.lumMagenta)
            else -> Triple(0f, 0f, 0f)
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLOR MIX (HSL)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextGray
                )
                
                TextButton(
                    onClick = { showColorMix = false },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("Back to Color", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // 8 Color Selector dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                colorsList.forEach { color ->
                    val colorHex = when (color) {
                        "Red" -> Color(0xFFFF1744)
                        "Orange" -> Color(0xFFFF9100)
                        "Yellow" -> Color(0xFFFFEA00)
                        "Green" -> Color(0xFF00E676)
                        "Aqua" -> Color(0xFF00E5FF)
                        "Blue" -> Color(0xFF2979FF)
                        "Purple" -> Color(0xFFD500F9)
                        "Magenta" -> Color(0xFFF50057)
                        else -> Color.Gray
                    }
                    val isSelected = selectedMixColor == color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colorHex)
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { viewModel.updateMixColor(color) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // HSL Sliders
            AdjustmentSlider(
                label = "$selectedMixColor Hue",
                value = activeMixColorVal.first,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateHslHue(selectedMixColor, it) },
                onValueChangeFinished = { viewModel.finishHslHueChange(selectedMixColor) }
            )
            AdjustmentSlider(
                label = "$selectedMixColor Saturation",
                value = activeMixColorVal.second,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateHslSat(selectedMixColor, it) },
                onValueChangeFinished = { viewModel.finishHslSatChange(selectedMixColor) }
            )
            AdjustmentSlider(
                label = "$selectedMixColor Luminance",
                value = activeMixColorVal.third,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateHslLum(selectedMixColor, it) },
                onValueChangeFinished = { viewModel.finishHslLumChange(selectedMixColor) }
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COLOR ADJUSTMENTS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = TextGray
                )
                
                Button(
                    onClick = { showColorMix = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(26.dp).testTag("mix_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Color Mix",
                        tint = OrangeAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mix (HSL)", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            AdjustmentSlider(
                label = "Temp (Cool to Warm)",
                value = photo.temp,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateTemp(it) },
                onValueChangeFinished = { viewModel.finishTempChange() }
            )
            AdjustmentSlider(
                label = "Tint (Green to Magenta)",
                value = photo.tint,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateTint(it) },
                onValueChangeFinished = { viewModel.finishTintChange() }
            )
            AdjustmentSlider(
                label = "Saturation",
                value = photo.saturation,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateSaturation(it) },
                onValueChangeFinished = { viewModel.finishSaturationChange() }
            )
            AdjustmentSlider(
                label = "Vibrance",
                value = photo.vibrance,
                valueRange = -1f..1f,
                onValueChange = { viewModel.updateVibrance(it) },
                onValueChangeFinished = { viewModel.finishVibranceChange() }
            )
        }
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: EFFECTS
// -----------------------------------------------------
@Composable
fun EffectsPanel(viewModel: PhotoEditorViewModel, photo: EditedPhoto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "EFFECTS & TEXTURES",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        AdjustmentSlider(
            label = "Texture",
            value = photo.texture,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateTexture(it) },
            onValueChangeFinished = { viewModel.finishTextureChange() }
        )
        AdjustmentSlider(
            label = "Clarity",
            value = photo.clarity,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateClarity(it) },
            onValueChangeFinished = { viewModel.finishClarityChange() }
        )
        AdjustmentSlider(
            label = "Dehaze",
            value = photo.dehaze,
            valueRange = -1f..1f,
            onValueChange = { viewModel.updateDehaze(it) },
            onValueChangeFinished = { viewModel.finishDehazeChange() }
        )
        AdjustmentSlider(
            label = "Vignette Amount",
            value = photo.vignette,
            valueRange = 0f..1f,
            onValueChange = { viewModel.updateVignette(it) },
            onValueChangeFinished = { viewModel.finishVignetteChange() }
        )
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: DETAIL
// -----------------------------------------------------
@Composable
fun DetailPanel(viewModel: PhotoEditorViewModel, photo: EditedPhoto) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "DETAILS & NOISE",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        AdjustmentSlider(
            label = "Sharpening",
            value = photo.sharpening,
            valueRange = 0f..1f,
            onValueChange = { viewModel.updateSharpening(it) },
            onValueChangeFinished = { viewModel.finishSharpeningChange() }
        )
        AdjustmentSlider(
            label = "Noise Reduction",
            value = photo.noiseReduction,
            valueRange = 0f..1f,
            onValueChange = { viewModel.updateNoiseReduction(it) },
            onValueChangeFinished = { viewModel.finishNoiseReductionChange() }
        )
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: CROP & ROTATE
// -----------------------------------------------------
@Composable
fun CropPanel(viewModel: PhotoEditorViewModel, photo: EditedPhoto) {
    val aspectRatios = listOf("Free", "1:1", "4:3", "16:9", "3:2", "5:7")

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "CROP, ROTATE & FLIP",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Ratio Selector
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(aspectRatios) { ratio ->
                val isSelected = photo.cropRatio == ratio
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) CyanAccent else DarkCard,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { viewModel.updateCropRatio(ratio) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = ratio,
                        color = if (isSelected) Color.Black else TextLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))

        // Actions row (Rotate 90, Flip H, Flip V)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.rotate90() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.RotateRight, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Rotate 90", color = TextLight, fontSize = 10.sp)
            }
            Button(
                onClick = { viewModel.flipHorizontal() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f).height(36.dp)
            ) {
                Icon(Icons.Default.Flip, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip H", color = TextLight, fontSize = 10.sp)
            }
            Button(
                onClick = { viewModel.flipVertical() },
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f).height(36.dp)
) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Flip V", color = TextLight, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fine Rotation Slider
        AdjustmentSlider(
            label = "Fine Straighten / Rotation",
            value = photo.rotation,
            valueRange = -45f..45f,
            onValueChange = { viewModel.updateRotation(it) },
            onValueChangeFinished = { viewModel.finishRotationChange() }
        )
    }
}

// -----------------------------------------------------
// CONTEXTUAL PANEL: HISTORY LOG
// -----------------------------------------------------
@Composable
fun HistoryPanel(viewModel: PhotoEditorViewModel, editHistory: List<String>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "EDIT LOG & HISTORIC STEPS",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = TextGray
        )
        Spacer(modifier = Modifier.height(6.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(editHistory.asReversed().take(6)) { step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkCard, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Applied",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = step,
                        color = TextLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------
// COMMON COMPOSABLE: SLIDER UNIT
// -----------------------------------------------------
@Composable
fun AdjustmentSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = String.format("%.2f", value),
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = CyanAccent,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier
                .height(24.dp)
                .testTag("slider_${label.lowercase().replace(" ", "_")}")
        )
    }
}

// -----------------------------------------------------
// COLOR MATRIX BUILDER (REAL-TIME ADAPTIVE HSL)
// -----------------------------------------------------
@Composable
fun rememberAdjustedColorMatrix(
    photo: EditedPhoto
): ColorMatrix {
    return remember(
        photo.exposure, photo.contrast, photo.highlights, photo.shadows, photo.whites, photo.blacks,
        photo.temp, photo.tint, photo.saturation, photo.vibrance,
        photo.hueRed, photo.satRed, photo.lumRed,
        photo.hueOrange, photo.satOrange, photo.lumOrange,
        photo.hueYellow, photo.satYellow, photo.lumYellow,
        photo.hueGreen, photo.satGreen, photo.lumGreen,
        photo.hueAqua, photo.satAqua, photo.lumAqua,
        photo.hueBlue, photo.satBlue, photo.lumBlue,
        photo.huePurple, photo.satPurple, photo.lumPurple,
        photo.hueMagenta, photo.satMagenta, photo.lumMagenta
    ) {
        // Contrast Scale (Contrast range -1 to 1, neutral is 1.0)
        val c = photo.contrast + 1f
        val t = 128f * (1f - c)
        
        // Saturation Scale (Saturation range -1 to 1, neutral is 1.0)
        val s = photo.saturation + 1f
        val lr = 0.213f
        val lg = 0.715f
        val lb = 0.072f
        
        val rSum = (1f - s) * lr
        val gSum = (1f - s) * lg
        val bSum = (1f - s) * lb
        
        // Base Channel Scales derived from HSL adjustments
        val rScale = 1.0f + photo.satRed * 0.4f + photo.lumRed * 0.25f
        val gScale = 1.0f + photo.satGreen * 0.4f + photo.lumGreen * 0.25f
        val bScale = 1.0f + photo.satBlue * 0.4f + photo.lumBlue * 0.25f
        
        // Orange & Yellow Scales
        val oSatScale = 1.0f + photo.satOrange * 0.35f + photo.lumOrange * 0.2f
        val ySatScale = 1.0f + photo.satYellow * 0.35f + photo.lumYellow * 0.2f
        
        // Aqua Scale
        val aSatScale = 1.0f + photo.satAqua * 0.35f + photo.lumAqua * 0.2f
        
        // Offsets combined (Exposure + Whites/Blacks + Highlights/Shadows + Temp/Tint)
        val exposureOffset = photo.exposure * 90f + photo.whites * 40f + photo.blacks * 40f
        
        val rOffset = t + exposureOffset + (photo.temp * 24f) + (photo.tint * 10f) + (photo.highlights * 20f) + (photo.lumRed * 20f)
        val gOffset = t + exposureOffset - (photo.tint * 15f) + (photo.highlights * 15f) + (photo.lumGreen * 20f)
        val bOffset = t + exposureOffset - (photo.temp * 24f) + (photo.tint * 10f) + (photo.shadows * 25f) + (photo.lumBlue * 20f)
        
        // Build the combined float array matrix
        val array = floatArrayOf(
            c * (rSum + s) * rScale * oSatScale * ySatScale, c * gSum * gScale,                               c * bSum * bScale,                             0f, rOffset,
            c * rSum * rScale,                               c * (gSum + s) * gScale * oSatScale * ySatScale * aSatScale, c * bSum * bScale,                             0f, gOffset,
            c * rSum * rScale,                               c * gSum * gScale,                               c * (bSum + s) * bScale * aSatScale,               0f, bOffset,
            0f,                                              0f,                                              0f,                                            1f, 0f
        )
        ColorMatrix(array)
    }
}
