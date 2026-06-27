package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.EditedPhoto
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioScreen(
    viewModel: PhotoEditorViewModel,
    onNavigateToEditor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val studioPhotos by viewModel.studioPhotos.collectAsState()
    var showAboutDialog by remember { mutableStateOf(false) }
    
    // System photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            // Start editing selected user photo
            viewModel.startNewEditSession(
                title = "Imported Photo",
                imagePath = uri.toString(),
                isLocalUri = true
            )
            onNavigateToEditor()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_icon),
                            contentDescription = "Infinity Edit Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "INFINITY EDIT",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = CyanAccent,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBg
                ),
                actions = {
                    IconButton(onClick = { showAboutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About Developer & Company",
                            tint = CyanAccent
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                containerColor = CyanAccent,
                contentColor = Color.Black,
                modifier = Modifier.testTag("import_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Import Photo")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Photo", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(DarkCard, DarkBg)
                            )
                        )
                        .border(1.dp, Brush.horizontalGradient(listOf(CyanAccent, OrangeAccent)), RoundedCornerShape(16.dp))
                ) {
                    // Dark atmospheric image as backdrop
                    Image(
                        painter = painterResource(id = R.drawable.img_sample_urban),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.25f
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(CyanAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO PHOTO STUDIO",
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Infinite adjustments.\nPro grading in seconds.",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextLight,
                            lineHeight = 26.sp
                        )
                    }
                }
            }

            // Presets / Samples Section
            item {
                Text(
                    text = "SAMPLE PRESETS & TEMPLATES",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = TextGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SampleCard(
                        title = "Mountain Sun",
                        imageRes = R.drawable.img_sample_landscape,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.startNewEditSession("Mountain Sun", "img_sample_landscape")
                            onNavigateToEditor()
                        }
                    )
                    SampleCard(
                        title = "Vogue Portrait",
                        imageRes = R.drawable.img_sample_portrait,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.startNewEditSession("Vogue Portrait", "img_sample_portrait")
                            onNavigateToEditor()
                        }
                    )
                    SampleCard(
                        title = "Tokyo Neon",
                        imageRes = R.drawable.img_sample_urban,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.startNewEditSession("Tokyo Neon", "img_sample_urban")
                            onNavigateToEditor()
                        }
                    )
                }
            }

            // My Studio creations section
            item {
                Text(
                    text = "MY STUDIO CREATIONS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = TextGray
                )
            }

            if (studioPhotos.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = TextGray.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Creations Yet",
                            fontWeight = FontWeight.Bold,
                            color = TextLight,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Import a photo or select a template to start editing with Light, Color, and HSL sliders.",
                            color = TextGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                items(studioPhotos) { photo ->
                    StudioPhotoItem(
                        photo = photo,
                        onEdit = {
                            viewModel.loadPhoto(photo)
                            onNavigateToEditor()
                        },
                        onDelete = {
                            viewModel.deletePhotoFromStudio(photo.id)
                        }
                    )
                }
            }
            
            // Extra spacing at bottom
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

@Composable
fun SampleCard(
    title: String,
    imageRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Text(
                text = title,
                color = TextLight,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StudioPhotoItem(
    photo: EditedPhoto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DarkCard, RoundedCornerShape(12.dp))
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCard)
            ) {
                if (photo.isLocalUri) {
                    AsyncImage(
                        model = photo.imagePath,
                        contentDescription = photo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
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
                        contentDescription = photo.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = photo.title,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = TextGray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    val dateStr = sdf.format(Date(photo.dateModified))
                    Text(
                        text = dateStr,
                        color = TextGray,
                        fontSize = 11.sp
                    )
                }
                if (photo.activePresetName != "None") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .background(CyanAccent.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Preset: ${photo.activePresetName}",
                            color = CyanAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Delete Action
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Photo",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = CyanAccent)
            ) {
                Text("Dismiss", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkSurface,
        titleContentColor = CyanAccent,
        textContentColor = TextLight,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "Infinity Edit Logo",
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Infinity Edit",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = TextLight
                    )
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Divider
                HorizontalDivider(color = DarkCard, thickness = 1.dp)

                // About Developer
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "About Developer",
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Prince AR Abdur Rahman",
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Independent App Developer passionate about building modern Android applications, productivity tools, AI-powered experiences, media players, educational apps, and next-generation digital products.",
                        color = TextGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }

                // Contacts
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Contact Information",
                        fontWeight = FontWeight.Bold,
                        color = TextLight,
                        fontSize = 13.sp
                    )
                    
                    // WhatsApp 1
                    Button(
                        onClick = { launchUrl(context, "https://wa.me/8801707424006") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💬", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp: 01707424006",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // WhatsApp 2
                    Button(
                        onClick = { launchUrl(context, "https://wa.me/8801796951709") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💬", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp: 01796951709",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Facebook
                    Button(
                        onClick = { launchUrl(context, "https://www.facebook.com/share/1BNn32qoJo/") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📘", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Facebook Page",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Instagram
                    Button(
                        onClick = { launchUrl(context, "https://www.instagram.com/ur___abdur____rahman__2008") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📸", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instagram Profile",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Divider
                HorizontalDivider(color = DarkCard, thickness = 1.dp)

                // About Company
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "About Company",
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "NexVora Lab's Ofc",
                        fontWeight = FontWeight.SemiBold,
                        color = TextLight,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "NexVora Lab's Ofc focuses on creating innovative Android applications designed to improve productivity, entertainment, learning, and digital experiences.",
                        color = TextGray,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Mission: Build fast, beautiful, privacy-friendly, and user-focused applications accessible to everyone.",
                        color = TextLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }

                // Divider
                HorizontalDivider(color = DarkCard, thickness = 1.dp)

                // Credits
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Developed by Prince AR Abdur Rahman",
                        fontSize = 11.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Published by NexVora Lab's Ofc",
                        fontSize = 11.sp,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "© 2026 NexVora Lab's Ofc. All Rights Reserved.",
                        fontSize = 10.sp,
                        color = TextGray.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}

private fun launchUrl(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback or ignore
    }
}
