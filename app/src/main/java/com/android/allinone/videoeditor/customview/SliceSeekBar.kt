package com.android.allinone.videoeditor.customview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import com.android.allinone.videoeditor.R
import android.graphics.Bitmap

import android.graphics.Canvas

import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat


class SliceSeekBar : AppCompatImageView {

    companion object {
        private const val SELECT_THUMB_LEFT = 1
        private const val SELECT_THUMB_RIGHT = 2
        private const val SELECT_THUMB_NON = 0
    }

    private var thumbSlice =
        BitmapFactory.decodeResource(resources, R.drawable.cutter)
    private var thumbCurrentVideoPosition = getBitmap (R.drawable.sb_thumb)
    private var progressMinDiff = 15 //percentage
    private var progressColor = resources.getColor(R.color.seek_bar_unselect)
    private var secondaryProgressColor = resources.getColor(R.color.seek_bar_select)
    private var progressHalfHeight = 3
    private var thumbPadding = resources.getDimensionPixelOffset(R.dimen.default_margin)
    private var maxValue = 100
    private var progressMinDiffPixels = 0
    private var thumbSliceLeftX = 0
    private var thumbSliceRightX = 0
    private var thumbCurrentVideoPositionX = 0
    private var thumbSliceLeftValue = 0
    private var thumbSliceRightValue = 0
    private var thumbSliceY = 0
    private var thumbCurrentVideoPositionY = 0
    private val paint = Paint()
    private val paintThumb = Paint()
    var selectedThumb = 0
    private var thumbSliceHalfWidth = 0
    private var thumbCurrentVideoPositionHalfWidth = 0
    private var scl: SeekBarChangeListener? = null
    private var progressTop = 0
    private var progressBottom = 0
    private var blocked = false
    private var isVideoStatusDisplay = false

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)
    constructor(context: Context?) : super(context!!)

    private fun getBitmap(drawableRes: Int): Bitmap {
        val drawable = resources.getDrawable(R.drawable.sb_thumb,null)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        init()
    }

    private fun init() {
        if (thumbSlice.height > height) layoutParams.height = thumbSlice.height
        thumbSliceY = height / 2 - thumbSlice.height / 2
        thumbCurrentVideoPositionY = height / 2 - thumbCurrentVideoPosition.height / 2
        thumbSliceHalfWidth = thumbSlice.width / 2
        thumbCurrentVideoPositionHalfWidth = thumbCurrentVideoPosition.width / 2
        if (thumbSliceLeftX == 0 || thumbSliceRightX == 0) {
            thumbSliceLeftX = thumbPadding
            thumbSliceRightX = width - thumbPadding
        }
        progressMinDiffPixels = calculateCorrds(progressMinDiff) - 2 * thumbPadding
        progressTop = height / 2 - progressHalfHeight
        progressBottom = height / 2 + progressHalfHeight
        invalidate()
    }

    fun setSeekBarChangeListener(scl: SeekBarChangeListener?) {
        this.scl = scl
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //generate and draw progress
        paint.color = progressColor
        var rect: Rect = Rect(thumbPadding, progressTop, thumbSliceLeftX, progressBottom)
        canvas.drawRect(rect, paint)
        rect = Rect(thumbSliceRightX, progressTop, width - thumbPadding, progressBottom)
        canvas.drawRect(rect, paint)

        //generate and draw secondary progress
        paint.color = secondaryProgressColor
        rect = Rect(thumbSliceLeftX, progressTop, thumbSliceRightX, progressBottom)
        canvas.drawRect(rect, paint)
        if (!blocked) {
            //generate and draw thumbs pointer
            canvas.drawBitmap(
                thumbSlice,
                (thumbSliceLeftX - thumbSliceHalfWidth).toFloat(),
                thumbSliceY.toFloat(),
                paintThumb
            )
            canvas.drawBitmap(
                thumbSlice,
                (thumbSliceRightX - thumbSliceHalfWidth).toFloat(),
                thumbSliceY.toFloat(),
                paintThumb
            )
        }
        if (isVideoStatusDisplay) {
            //generate and draw video thump pointer
            canvas.drawBitmap(
                thumbCurrentVideoPosition,
                (thumbCurrentVideoPositionX - thumbCurrentVideoPositionHalfWidth).toFloat(),
                thumbCurrentVideoPositionY.toFloat(),
                paintThumb
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!blocked) {
            val mx = event.x.toInt()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> if (mx >= thumbSliceLeftX - thumbSliceHalfWidth
                    && mx <= thumbSliceLeftX + thumbSliceHalfWidth || mx < thumbSliceLeftX - thumbSliceHalfWidth
                ) {
                    selectedThumb = SELECT_THUMB_LEFT
                } else if (mx >= thumbSliceRightX - thumbSliceHalfWidth
                    && mx <= thumbSliceRightX + thumbSliceHalfWidth || mx > thumbSliceRightX + thumbSliceHalfWidth
                ) {
                    selectedThumb = SELECT_THUMB_RIGHT
                } else if (mx - thumbSliceLeftX + thumbSliceHalfWidth < thumbSliceRightX - thumbSliceHalfWidth - mx) {
                    selectedThumb = SELECT_THUMB_LEFT
                } else if (mx - thumbSliceLeftX + thumbSliceHalfWidth > thumbSliceRightX - thumbSliceHalfWidth - mx) {
                    selectedThumb = SELECT_THUMB_RIGHT
                }
                MotionEvent.ACTION_MOVE -> {
                    if (mx <= thumbSliceLeftX + thumbSliceHalfWidth + progressMinDiffPixels && selectedThumb == SELECT_THUMB_RIGHT ||
                        mx >= thumbSliceRightX - thumbSliceHalfWidth - progressMinDiffPixels && selectedThumb == SELECT_THUMB_LEFT
                    ) {
                        selectedThumb = SELECT_THUMB_NON
                    }
                    if (selectedThumb == SELECT_THUMB_LEFT) {
                        thumbSliceLeftX = mx
                    } else if (selectedThumb == SELECT_THUMB_RIGHT) {
                        thumbSliceRightX = mx
                    }
                }
                MotionEvent.ACTION_UP -> selectedThumb = SELECT_THUMB_NON
            }
            notifySeekBarValueChanged()
        }
        return true
    }

    private fun notifySeekBarValueChanged() {
        if (thumbSliceLeftX < thumbPadding) thumbSliceLeftX = thumbPadding
        if (thumbSliceRightX < thumbPadding) thumbSliceRightX = thumbPadding
        if (thumbSliceLeftX > width - thumbPadding) thumbSliceLeftX = width - thumbPadding
        if (thumbSliceRightX > width - thumbPadding) thumbSliceRightX = width - thumbPadding
        invalidate()
        scl?.let {
            calculateThumbValue()
            it.SeekBarValueChanged(thumbSliceLeftValue, thumbSliceRightValue)
        }
    }

    private fun calculateThumbValue() {
        thumbSliceLeftValue =
            maxValue * (thumbSliceLeftX - thumbPadding) / (width - 2 * thumbPadding)
        thumbSliceRightValue =
            maxValue * (thumbSliceRightX - thumbPadding) / (width - 2 * thumbPadding)
    }

    private fun calculateCorrds(progress: Int): Int {
        return ((width - 2.0 * thumbPadding) / maxValue * progress).toInt() + thumbPadding
    }

    var leftProgress: Int
        get() = thumbSliceLeftValue
        set(progress) {
            if (progress < thumbSliceRightValue - progressMinDiff) {
                thumbSliceLeftX = calculateCorrds(progress)
            }
            notifySeekBarValueChanged()
        }
    var rightProgress: Int
        get() = thumbSliceRightValue
        set(progress) {
            if (progress > thumbSliceLeftValue + progressMinDiff) {
                thumbSliceRightX = calculateCorrds(progress)
            }
            notifySeekBarValueChanged()
        }

    fun setProgress(leftProgress: Int, rightProgress: Int) {
        if (rightProgress - leftProgress > progressMinDiff) {
            thumbSliceLeftX = calculateCorrds(leftProgress)
            thumbSliceRightX = calculateCorrds(rightProgress)
        }
        notifySeekBarValueChanged()
    }

    fun videoPlayingProgress(progress: Int) {
        isVideoStatusDisplay = true
        thumbCurrentVideoPositionX = calculateCorrds(progress)
        invalidate()
    }

    fun removeVideoStatusThumb() {
        isVideoStatusDisplay = false
        invalidate()
    }

    fun setSliceBlocked(isBLock: Boolean) {
        blocked = isBLock
        invalidate()
    }

    fun setMaxValue(maxValue: Int) {
        this.maxValue = maxValue
    }

    fun setProgressMinDiff(progressMinDiff: Int) {
        this.progressMinDiff = progressMinDiff
        progressMinDiffPixels = calculateCorrds(progressMinDiff)
    }

    fun setProgressHeight(progressHeight: Int) {
        progressHalfHeight = progressHalfHeight / 2
        invalidate()
    }

    fun setProgressColor(progressColor: Int) {
        this.progressColor = progressColor
        invalidate()
    }

    fun setSecondaryProgressColor(secondaryProgressColor: Int) {
        this.secondaryProgressColor = secondaryProgressColor
        invalidate()
    }

    fun setThumbSlice(thumbSlice: Bitmap) {
        this.thumbSlice = thumbSlice
        init()
    }

    fun setThumbCurrentVideoPosition(thumbCurrentVideoPosition: Bitmap) {
        this.thumbCurrentVideoPosition = thumbCurrentVideoPosition
        init()
    }

    fun setThumbPadding(thumbPadding: Int) {
        this.thumbPadding = thumbPadding
        invalidate()
    }

    interface SeekBarChangeListener {
        fun SeekBarValueChanged(leftThumb: Int, rightThumb: Int)
    }
}