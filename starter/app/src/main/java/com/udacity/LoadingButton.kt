package com.udacity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var circleSize = 0F

    private lateinit var buttonText: String

    private val valueAnimator = ValueAnimator.ofFloat(0F, 1F)
        .setDuration(5000)

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (buttonState) {
            ButtonState.Clicked -> {
                buttonText = context.getString(R.string.loading_button_text)
                buttonState = ButtonState.Loading
            }
            ButtonState.Loading -> {
                beginAnimation()
            }
            ButtonState.Completed -> {
                stopAnimation()
                buttonText = context.getString(R.string.download_button_text)
            }
        }
        invalidate()
    }

    private val paint = Paint().apply {
        textSize = resources.getDimension(R.dimen.default_text_size)
        isAntiAlias = true
    }

    init {
        buttonText = context.getString(R.string.download_button_text)
    }

    fun buttonClicked() {
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Clicked
    }

    var animationProgress = 0F

    private var loadingBackgroundWidth: Float = 0F
    private var loadingCircleArc: Float = 0F

    private fun beginAnimation() {
        valueAnimator
            .addUpdateListener {
                loadingBackgroundWidth = (it.animatedValue as Float).times(widthSize.toFloat())
                loadingCircleArc = (it.animatedValue as Float).times(365F)
                Log.d(TAG, "Animator Update Listener: $loadingBackgroundWidth")
                invalidate()
            }
        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator?) {
                buttonState = ButtonState.Completed
            }
        })
        valueAnimator.start()
    }


    private fun stopAnimation() {
        animationProgress = 0F
        loadingBackgroundWidth = 0F
        loadingCircleArc = 0F
        valueAnimator.cancel()
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawBackground()

            drawLoadingBackground()
            drawLoadingCircle()

            drawTitle()
        }
    }

    private fun Canvas.drawBackground() {
        paint.color = context.getColor(R.color.colorPrimary)
        this.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun Canvas.drawTitle() {
        paint.color = context.getColor(R.color.white)
        this.drawText(
            buttonText,
            (width) / 2.8F,
            (height / 2) - ((paint.descent() + paint.ascent()) / 2),
            paint
        )
    }

    private fun Canvas.drawLoadingBackground() {
        paint.color = context.getColor(R.color.colorPrimaryDark)
        this.drawRect(0F, 0F, loadingBackgroundWidth, heightSize.toFloat(), paint)
    }

    private fun Canvas.drawLoadingCircle() {
        this.save() // so we can reset back after translating
        paint.color = context.getColor(R.color.colorAccent)
        this.translate(widthSize - (circleSize * 1.5F), (heightSize / 2F) - (circleSize / 2F))
        this.drawArc(
            RectF(0F, 0F, circleSize, circleSize), 0F,
            loadingCircleArc, true, paint
        )
        this.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        circleSize = h / 2F
        setMeasuredDimension(w, h)
    }

}