package com.example.androidapp.ui.overlays

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.camera.view.PreviewView
import android.graphics.Typeface
import com.example.androidapp.objects.MediapipeDetectionBBoxCoords
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class LearnCameraOverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: HandLandmarkerResult? = null
    private var boxPaint = Paint()
    private var letterPaint = Paint()
    private var letterToShowPaint = Paint()
    private var finalTextPaint = Paint()
    private var startTextPaint = Paint()

    private var x_max_mediapipe: Float = 0f
    private var x_min_mediapipe: Float = 0f
    private var y_max_mediapipe: Float = 0f
    private var y_min_mediapipe: Float = 0f

    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    var previewViewHeight: Int = 0
    var previewViewWidth: Int = 0

    var letterToShow: Int = 0

    var letters: Array<String> = arrayOf<String>(
        "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "K", "L", "M", "N", "O", "P", "Q", "R", "S",
        "T", "U", "V", "W", "X", "Y"
    )


    init {
        initPaints()
    }

    fun clear() {

        results = null
        boxPaint.reset()
        letterPaint.reset()
        letterToShowPaint.reset()
        initPaints()
    }

    private fun initPaints() {
        boxPaint.color = Color.GREEN
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 4f

        letterPaint.color = Color.GREEN
        letterPaint.textSize = 100f
        letterPaint.textAlign = Paint.Align.CENTER
        letterPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        letterPaint.setShadowLayer(15f, 5f, 5f, Color.BLACK)
        letterPaint.strokeWidth = 3f

        finalTextPaint.color = Color.GREEN
        finalTextPaint.textSize = 50f
        finalTextPaint.textAlign = Paint.Align.CENTER
        finalTextPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        finalTextPaint.setShadowLayer(15f, 5f, 5f, Color.BLACK)

        startTextPaint.color = Color.GREEN
        startTextPaint.textSize = 35f
        startTextPaint.textAlign = Paint.Align.CENTER
        startTextPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        startTextPaint.setShadowLayer(15f, 5f, 5f, Color.BLACK)

    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        if (x_max_mediapipe != 0f){
            if (letterToShow >= letters.size) {
                val text = "Good job!"
                val textX = previewViewWidth / 2f
                val textY = previewViewHeight * 0.15f
                canvas.drawText(text, textX, textY, finalTextPaint)
            } else {
                val letterBitmap = BitmapFactory.decodeStream(context.assets.open("handsigns/${letters[letterToShow]}.png"))
                val resizedLetterBitmap = Bitmap.createScaledBitmap(letterBitmap, 224, 224, true)
                canvas.drawBitmap(resizedLetterBitmap, (previewViewWidth - resizedLetterBitmap.width) / 2f, previewViewHeight * 0.15f, null)
                canvas.drawText(letters[letterToShow], previewViewWidth * 0.5f, previewViewHeight * 0.12f, letterPaint)
            }
        } else if (previewViewWidth != 0){
            val text = "Show your hand to start learning."

            val textX = previewViewWidth / 2f
            val textY = previewViewHeight / 2f
            canvas.drawText(text, textX, textY, startTextPaint)
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

    fun invalidateOwn(){
        invalidate()
    }
    }
