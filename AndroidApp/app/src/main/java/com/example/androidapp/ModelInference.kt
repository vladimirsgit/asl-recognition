package com.example.androidapp

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.core.graphics.scale
import com.example.androidapp.ui.overlays.FFACameraOverlayView
import com.example.androidapp.objects.MediapipeDetectionBBoxCoords
import com.example.androidapp.ui.overlays.LearnCameraOverlayView
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.exp


class ModelInference(private var context: Context) {
    val alphabetMap = mapOf(
        0 to "A", 1 to "B", 2 to "C", 3 to "D", 4 to "E", 5 to "F", 6 to "G", 7 to "H",
        8 to "I", 9 to "K", 10 to "L", 11 to "M", 12 to "N", 13 to "O", 14 to "P",
        15 to "Q", 16 to "R", 17 to "S", 18 to "T", 19 to "U", 20 to "V", 21 to "W",
        22 to "X", 23 to "Y"
    )
    val vladimirNetPath = assetFilePath(context, "VladimirNet.ptl")
    val mobileNetV3LargePath = assetFilePath(context, "MobileNetV3Large.ptl")

    var model: Module = LiteModuleLoader.load(vladimirNetPath)

    var mean: FloatArray = floatArrayOf(0.5797f, 0.5104f, 0.4846f)
    var std: FloatArray = floatArrayOf(0.1804f, 0.1845f, 0.1883f)

    fun FFAInference(imageProxy: ImageProxy, bitmap: Bitmap, FFACameraOverlayView: FFACameraOverlayView, mediapipeDetectionBBoxCoords: MediapipeDetectionBBoxCoords) {
        val start_time = SystemClock.uptimeMillis()
        // Copy out RGB bits from the frame to a bitmap buffer
        val cropped_bitmap = Bitmap.createBitmap(bitmap,
            mediapipeDetectionBBoxCoords.x_min.toInt(),
            mediapipeDetectionBBoxCoords.y_min.toInt(),
            (mediapipeDetectionBBoxCoords.x_max - mediapipeDetectionBBoxCoords.x_min).toInt(),
            (mediapipeDetectionBBoxCoords.y_max - mediapipeDetectionBBoxCoords.y_min).toInt())
        val scaled_bitmap = cropped_bitmap.scale(224, 224)


        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            scaled_bitmap,
            mean, std
        )
        val outputTensor: Tensor = model.forward(IValue.from(inputTensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        var maxScore = -Float.Companion.MAX_VALUE
        var maxScoreIdx = -1
        for (i in 0 until scores.size) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        val expScores = scores.map { exp((it - maxScore).toDouble()) }
        val sumExp = expScores.sum()
        val probs = expScores.map { (it/sumExp).toFloat() }
        var className: String = alphabetMap[maxScoreIdx].toString()
        if (probs[maxScoreIdx] < 0.5f) {
            className = "-"
        }


        FFACameraOverlayView.guessedLetter = className
        FFACameraOverlayView.modelInferenceMs = SystemClock.uptimeMillis() - start_time
    }

    fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return file.absolutePath
    }

    fun LearningInference(imageProxy: ImageProxy, bitmap: Bitmap, learnCameraOverlayView: LearnCameraOverlayView, mediapipeDetectionBBoxCoords: MediapipeDetectionBBoxCoords) {
        // Copy out RGB bits from the frame to a bitmap buffer
        if (learnCameraOverlayView.letterToShow >= learnCameraOverlayView.letters.size) return
        val cropped_bitmap = Bitmap.createBitmap(bitmap,
            mediapipeDetectionBBoxCoords.x_min.toInt(),
            mediapipeDetectionBBoxCoords.y_min.toInt(),
            (mediapipeDetectionBBoxCoords.x_max - mediapipeDetectionBBoxCoords.x_min).toInt(),
            (mediapipeDetectionBBoxCoords.y_max - mediapipeDetectionBBoxCoords.y_min).toInt())
        val scaled_bitmap = cropped_bitmap.scale(224, 224)


        val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            scaled_bitmap,
            mean, std
        )
        val outputTensor: Tensor = model.forward(IValue.from(inputTensor)).toTensor()
        val scores = outputTensor.dataAsFloatArray

        var maxScore = -Float.Companion.MAX_VALUE
        var maxScoreIdx = -1
        for (i in 0 until scores.size) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        val expScores = scores.map { exp((it - maxScore).toDouble()) }
        val sumExp = expScores.sum()
        val probs = expScores.map { (it/sumExp).toFloat() }
        var className: String = alphabetMap[maxScoreIdx].toString()
        if (probs[maxScoreIdx] < 0.5f) {
            className = "-"
        }
        if (className == learnCameraOverlayView.letters[learnCameraOverlayView.letterToShow]) {
            learnCameraOverlayView.letterToShow += 1
        }
    }
}
