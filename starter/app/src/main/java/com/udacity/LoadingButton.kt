package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

private const val TAG = "LoadingButton"

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private var circleSize = 0F

    private lateinit var downloadText: String
    private var buttonText: String
    private var backgroundColour: Int
    private var textColour: Int

    private val valueAnimator = ValueAnimator.ofFloat(0F, 1F)
        .setDuration(5000)

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (buttonState) {
            ButtonState.Clicked -> {
                buttonState = ButtonState.Loading
            }
            ButtonState.Loading -> {
                beginAnimation()
                buttonText = context.getString(R.string.loading_button_text)
            }
            ButtonState.Completed -> {
                stopAnimation()
                buttonText = downloadText
            }
        }
        invalidate()
    }

    private val paint = Paint().apply {
        textSize = resources.getDimension(R.dimen.default_text_size)
        isAntiAlias = true
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.LoadingButton).apply {
            downloadText = getString(R.styleable.LoadingButton_buttonText)
                ?: context.getString(R.string.download_button_text)
            backgroundColour = getColor(
                R.styleable.LoadingButton_backgroundColour,
                context.getColor(R.color.colorPrimary)
            )
            textColour = getColor(
                R.styleable.LoadingButton_textColour,
                context.getColor(R.color.colorPrimary)
            )
        }.recycle()

        buttonText = downloadText
    }

    fun buttonClicked() {
        if (buttonState == ButtonState.Completed) buttonState = ButtonState.Clicked
    }

    fun downloadComplete() {
        if (buttonState == ButtonState.Loading) buttonState = ButtonState.Completed
    }

    var animationProgress = 0F

    private var loadingBackgroundWidth: Float = 0F
    private var loadingCircleArc: Float = 0F

    private fun beginAnimation() {
        valueAnimator
            .addUpdateListener {
                loadingBackgroundWidth = (it.animatedValue as Float).times(widthSize.toFloat())
                loadingCircleArc = (it.animatedValue as Float).times(365F)
                invalidate()
            }
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
        paint.color = backgroundColour
        this.drawRect(0F, 0F, widthSize.toFloat(), heightSize.toFloat(), paint)
    }

    private fun Canvas.drawTitle() {
        paint.color = textColour
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