package com.listaih.app.ui.screens.shopping

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.listaih.app.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBackClick: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val scanner: BarcodeScanner = remember { BarcodeScanning.getClient() }
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var scanning by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.scanner_title), fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.scanner_back_cd))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                !hasPermission -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = stringResource(R.string.scanner_permission_title),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.scanner_permission_message),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            modifier = Modifier.padding(top = 24.dp)
                        ) {
                            Text("Conceder permissão")
                        }
                    }
                }

                else -> {
                    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            startCamera(ctx, lifecycleOwner, previewView, scanner) { value ->
                                scanning = true
                                onBarcodeScanned(value)
                            }
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (scanning) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Aponte a câmera para o código de barras",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun startCamera(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    scanner: BarcodeScanner,
    onScanned: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val executor: ExecutorService = Executors.newSingleThreadExecutor()

    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor) { imageProxy: ImageProxy ->
            processImage(imageProxy, scanner) { value ->
                onScanned(value)
            }
        }

        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            selector,
            preview,
            imageAnalysis
        )
    }, ContextCompat.getMainExecutor(context))
}

private fun processImage(
    imageProxy: ImageProxy,
    scanner: BarcodeScanner,
    onScanned: (String) -> Unit
) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }
    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    scanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            val barcode: Barcode? = barcodes.firstOrNull()
            if (barcode?.rawValue != null) {
                onScanned(barcode.rawValue!!)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
