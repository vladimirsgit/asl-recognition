package com.example.androidapp.ui.overlays

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.camera.view.PreviewView
import com.example.androidapp.objects.MediapipeDetectionBBoxCoords
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class FFACameraOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var boxPaint = Paint()
    private var letterPaint = Paint()

    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    private var x_max_mediapipe: Float = 0f
    private var x_min_mediapipe: Float = 0f
    private var y_max_mediapipe: Float = 0f
    private var y_min_mediapipe: Float = 0f

    private var x_max_preview: Float = 0f
    private var x_min_preview: Float = 0f
    private var y_max_preview: Float = 0f
    private var y_min_preview: Float = 0f

    private var previewViewHeight: Int = 0
    private var previewViewWidth: Int = 0

    var mediaPipeDetectionMs: Long = 0
    var modelInferenceMs: Long = 0

    var guessedLetter: String = ""


    init {
        initPaints()
    }

    fun clear() {
        x_max_mediapipe = 0f
        x_min_mediapipe = 0f
        y_max_mediapipe = 0f
        y_min_mediapipe = 0f

        x_max_preview = 0f
        x_min_preview = 0f
        y_max_preview = 0f
        y_min_preview = 0f

        guessedLetter = ""
        results = null
        boxPaint.reset()
        letterPaint.reset()
        initPaints()
    }

    private fun initPaints() {
        boxPaint.color = Color.GREEN
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 4f

        letterPaint.color = Color.GREEN
        letterPaint.textSize = 50f
        letterPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (x_max_mediapipe != 0f){
            canvas.drawRect(
                x_min_preview,
                y_min_preview,
                x_max_preview,
                y_max_preview,
                boxPaint
            )
            canvas.drawText("Det: ${guessedLetter}; Time: ${mediaPipeDetectionMs + modelInferenceMs}ms", x_min_preview, y_min_preview - 20, letterPaint)
        }
    }

    fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        previewView: PreviewView,
    ): MediapipeDetectionBBoxCoords?{
        if (handLandmarkerResults.landmarks().isEmpty()) {
            invalidate()
            clear()
            return null
        }
        var landmarks = handLandmarkerResults.landmarks()[0]

        x_max_mediapipe = (landmarks.maxBy { it.x() }).x().times(imageWidth)
        x_min_mediapipe = (landmarks.minBy { it.x() }).x().times(imageWidth)
        y_max_mediapipe = (landmarks.maxBy { it.y() }).y().times(imageHeight)
        y_min_mediapipe = (landmarks.minBy { it.y() }).y().times(imageHeight)

        previewViewWidth = previewView.width
        previewViewHeight = previewView.height

        var widthScaleFactor: Float = previewViewWidth.toFloat() / imageWidth
        var heightScaleFactor: Float = previewViewHeight.toFloat() / imageHeight

        x_max_preview = x_max_mediapipe * widthScaleFactor
        x_min_preview = x_min_mediapipe * widthScaleFactor
        y_max_preview = y_max_mediapipe * heightScaleFactor
        y_min_preview = y_min_mediapipe * heightScaleFactor

        var x_preview_size: Float = x_max_preview - x_min_preview
        var y_preview_size: Float = y_max_preview - y_min_preview

        x_max_preview += (x_preview_size * 0.5f)
        x_min_preview -= (x_preview_size * 0.5f)
        y_max_preview += (y_preview_size * 0.5f)
        y_min_preview -= (y_preview_size * 0.5f)

        x_min_preview = max(0f, x_min_preview)
        y_min_preview = max(0f, y_min_preview)
        x_max_preview = min(previewViewWidth.toFloat(), x_max_preview)
        y_max_preview = min(previewViewHeight.toFloat(), y_max_preview)

        var x_mediapipe_size: Float = x_max_mediapipe - x_min_mediapipe
        var y_mediapipe_size: Float = y_max_mediapipe - y_min_mediapipe
        x_max_mediapipe += (x_mediapipe_size * 0.4f)
        x_min_mediapipe -= (x_mediapipe_size * 0.4f)
        y_max_mediapipe += (y_mediapipe_size * 0.4f)
        y_min_mediapipe -= (y_mediapipe_size * 0.4f)

        x_min_mediapipe = max(0f, x_min_mediapipe)
        y_min_mediapipe = max(0f, y_min_mediapipe)
        x_max_mediapipe = min(imageWidth.toFloat(), x_max_mediapipe)
        y_max_mediapipe = min(imageHeight.toFloat(), y_max_mediapipe)
        invalidate()
        return MediapipeDetectionBBoxCoords(x_max_mediapipe, x_min_mediapipe, y_max_mediapipe, y_min_mediapipe)

    }
}