package com.example.androidapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.camera.core.Preview as CorePreview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.androidapp.MainActivity
import com.example.androidapp.ui.theme.AndroidAppTheme
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.concurrent.Executors
import com.example.androidapp.HandLandmarkerHelper
import com.example.androidapp.ModelInference
import com.example.androidapp.objects.MediapipeDetectionBBoxCoords
import com.example.androidapp.ui.overlays.LearnCameraOverlayView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LearnCameraScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as MainActivity
    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 101)
    } else {
        Scaffold (topBar = {
            CenterAlignedTopAppBar(
                colors = centerAlignedTopAppBarColors(containerColor = Color.Transparent, navigationIconContentColor = Color.White),
                title = {
                    Text(
                        "",
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Camera",
                            tint = Color.White,
                            modifier = Modifier.size(70.dp)
                        )
                    }
                }
            )
        }, floatingActionButtonPosition = FabPosition.Center){
            StartLearnCameraPreview(activity = activity)
        }

    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun StartLearnCameraPreview(activity: MainActivity) {
    val context = activity
    val previewView = remember { PreviewView(context) }

    val learnCameraOverlayView = remember { LearnCameraOverlayView(context, null) }

    val imageAnalysisExecutor = Executors.newSingleThreadExecutor()

    val resolutionSelectorBuilder = ResolutionSelector.Builder().apply {
        setResolutionStrategy(
            ResolutionStrategy(
                Size(
                    960, 720
                ), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER
            )
        )
    }
    val imageAnalysis = ImageAnalysis.Builder().setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).setResolutionSelector(resolutionSelectorBuilder.build()).build()

    val modelInference = ModelInference(context = context)
    var mediapipeDetectionBBoxCoords: MediapipeDetectionBBoxCoords? = null
    val handLandmarkerHelper = HandLandmarkerHelper(
        context = context,
        handLandmarkerHelperListener = object : HandLandmarkerHelper.LandmarkerListener {
            override fun onError(error: String, errorCode: Int) {
                Log.e("HandLandmarker", "Error: $error, Code: $errorCode")
            }

            override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
                var results = resultBundle.results
                learnCameraOverlayView.previewViewWidth = previewView.width
                learnCameraOverlayView.previewViewHeight = previewView.height
                learnCameraOverlayView.invalidateOwn()
                mediapipeDetectionBBoxCoords = results.first().let { result ->
                    learnCameraOverlayView.setResults(
                        handLandmarkerResults = result,
                        imageHeight = resultBundle.inputImageHeight,
                        imageWidth = resultBundle.inputImageWidth,
                        previewView = previewView
                    )

                }

            }
        }
    )

    val cameraProvider = ProcessCameraProvider.getInstance(context).get()
    cameraProvider.unbindAll()
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    val preview = CorePreview.Builder().build()
    preview.surfaceProvider = previewView.surfaceProvider

    imageAnalysis.setAnalyzer (imageAnalysisExecutor, ImageAnalysis.Analyzer { imageProxy ->
        val bitmap =
            Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
        val buffer = imageProxy.planes[0].buffer
        bitmap.copyPixelsFromBuffer(buffer)
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())

            // flip image if user use front camera
            postScale(
                -1f,
                1f,
                imageProxy.width.toFloat(),
                imageProxy.height.toFloat()
            )

        }
        val rotatedBitmap = Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height,
            matrix, true
        )
        handLandmarkerHelper.detectLiveStream(rotatedBitmap)
        mediapipeDetectionBBoxCoords?.let {
            modelInference.LearningInference(imageProxy, rotatedBitmap, learnCameraOverlayView, it)
        }
        imageProxy.close()
    })

    cameraProvider.bindToLifecycle(context as LifecycleOwner, cameraSelector, preview, imageAnalysis)


    AndroidAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
            AndroidView(
                factory = { learnCameraOverlayView },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
