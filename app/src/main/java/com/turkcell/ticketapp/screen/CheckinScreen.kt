package com.turkcell.ticketapp.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.core.util.formatEventDate
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.CheckinViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
fun CheckinScreen(
    onBackClick: () -> Unit,
    viewModel: CheckinViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = onBackClick) {
                Text(text = stringResource(R.string.back))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.checkin_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.qrCode,
                onValueChange = viewModel::onQrCodeChange,
                label = {
                    Text(text = stringResource(R.string.checkin_qr_label))
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 3,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = viewModel::scanTicket,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isLoading) {
                        stringResource(R.string.checkin_scanning)
                    } else {
                        stringResource(R.string.checkin_scan_button)
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = viewModel::toggleCamera,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.isCameraVisible) {
                        stringResource(R.string.checkin_camera_close)
                    } else {
                        stringResource(R.string.checkin_camera_scan)
                    }
                )
            }

            if (state.isCameraVisible) {
                Spacer(modifier = Modifier.height(12.dp))
                if (hasCameraPermission) {
                    QrCameraScanner(
                        onQrCodeScanned = viewModel::onQrCodeScanned,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    )
                } else {
                    CameraPermissionContent(
                        onPermissionClick = {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                }
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.result?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                CheckinResultCard(result = result)
            }
        }
    }
}

@Composable
private fun CameraPermissionContent(
    onPermissionClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.checkin_camera_permission),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onPermissionClick) {
            Text(text = stringResource(R.string.checkin_camera_permission_button))
        }
    }
}

@Composable
private fun QrCameraScanner(
    onQrCodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val hasScanned = remember { AtomicBoolean(false) }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener(
                {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .build()
                        .also { preview ->
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val qrCode = imageProxy.decodeQrCode()
                                imageProxy.close()

                                if (qrCode != null && hasScanned.compareAndSet(false, true)) {
                                    ContextCompat.getMainExecutor(context).execute {
                                        onQrCodeScanned(qrCode)
                                    }
                                }
                            }
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                },
                ContextCompat.getMainExecutor(context)
            )
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).get().unbindAll()
            cameraExecutor.shutdown()
        }
    }
}

private fun ImageProxy.decodeQrCode(): String? {
    val yPlane = planes.firstOrNull() ?: return null
    val imageWidth = width
    val imageHeight = height
    val rowStride = yPlane.rowStride
    val buffer = yPlane.buffer
    val yData = ByteArray(buffer.remaining())
    buffer.get(yData)

    val luminanceData = if (rowStride == imageWidth) {
        yData
    } else {
        ByteArray(imageWidth * imageHeight).also { output ->
            for (row in 0 until imageHeight) {
                val inputOffset = row * rowStride
                val outputOffset = row * imageWidth
                if (inputOffset + imageWidth <= yData.size) {
                    System.arraycopy(yData, inputOffset, output, outputOffset, imageWidth)
                }
            }
        }
    }

    val source = PlanarYUVLuminanceSource(
        luminanceData,
        imageWidth,
        imageHeight,
        0,
        0,
        imageWidth,
        imageHeight,
        false
    )

    val reader = MultiFormatReader().apply {
        setHints(
            mapOf(
                DecodeHintType.POSSIBLE_FORMATS to listOf(BarcodeFormat.QR_CODE)
            )
        )
    }

    return try {
        reader.decode(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: NotFoundException) {
        null
    } finally {
        reader.reset()
    }
}

@Composable
private fun CheckinResultCard(result: CheckinResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.checkin_success),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = result.event.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.ticket_type_label, result.ticketType),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = stringResource(R.string.venue_label, result.event.venue),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = formatEventDate(result.event.startsAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.checkin_time, formatEventDate(result.checkedInAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
