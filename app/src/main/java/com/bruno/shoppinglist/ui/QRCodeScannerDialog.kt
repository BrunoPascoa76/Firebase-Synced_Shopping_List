package com.bruno.shoppinglist.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bruno.shoppinglist.R
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

@Composable
fun QrScannerDialog(
    onDismiss: () -> Unit,
    onCodeDetected: (String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        QrScannerContent(
            onDismiss = onDismiss,
            onCodeDetected = onCodeDetected
        )
    }
}

@Composable
private fun QrScannerContent(
    onDismiss: () -> Unit,
    onCodeDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var hasCameraPermission by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Surface(
        modifier = Modifier
            .size(width = 340.dp, height = 480.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF1C2526),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.Scan_Title),
                color = Color.White,
                fontSize = 20.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Camera Preview Box
            Box(
                modifier = Modifier
                    .size(280.dp, 280.dp)
                    .background(Color.Black, RoundedCornerShape(16.dp))
            ) {
                if (!hasCameraPermission) {
                    // Permission UI
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text(stringResource(R.string.Camera_Permission))
                    }
                } else {
                    // Camera Preview
                    CameraPreview(
                        cameraProviderFuture = cameraProviderFuture,
                        lifecycleOwner = lifecycleOwner,
                        onCodeDetected = onCodeDetected,
                        onDismiss = onDismiss
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            // Instructions text below preview
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.QRCodeScan_Instructions),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }


        }
    }
}

@Composable
private fun CameraPreview(
    cameraProviderFuture: com.google.common.util.concurrent.ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onCodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
            }
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val scanner = BarcodeScanning.getClient()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build().also {
                        it.setAnalyzer(executor) { imageProxy ->
                            processImageProxy(scanner, imageProxy, onCodeDetected, onDismiss)
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onCodeDetected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    imageProxy.image?.let { mediaImage ->
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { code ->
                    onCodeDetected(code)
                    onDismiss()
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } ?: run {
        imageProxy.close()
    }
}
