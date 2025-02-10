package com.example.g2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class BallGameView(context: Context, attrs: AttributeSet?) : View(context, attrs), SensorEventListener {

    private val paint = Paint()
    private var ballX = 0f
    private var ballY = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    private var circleX = 0f
    private var circleY = 0f
    private var circleRadius = 0f
    private var ballRadius = 0f
    private var ballColor = Color.rgb(Random.nextInt(50, 255), Random.nextInt(50, 255), Random.nextInt(50, 255))
    private lateinit var backgroundBitmap: Bitmap
    private var speed = 4f
    private val gravity = 0.1f
    private var accelX = 0f
    private var accelY = 0f

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        paint.style = Paint.Style.FILL
        initializeGame()
        backgroundBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.background)
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    private fun initializeGame() {
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

        circleX = screenWidth / 2
        circleY = screenHeight / 2

        circleRadius = 26f
        ballRadius = 20f

        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val radius = Random.nextFloat() * (circleRadius - ballRadius)
        ballX = circleX + radius * cos(angle.toDouble()).toFloat()
        ballY = circleY + radius * sin(angle.toDouble()).toFloat()

        velocityX = speed * if (Random.nextBoolean()) 1 else -1
        velocityY = speed * if (Random.nextBoolean()) 1 else -1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(backgroundBitmap, (width - backgroundBitmap.width) / 2f, (height - backgroundBitmap.height) / 2f, paint)

        paint.color = Color.BLACK
        canvas.drawCircle(circleX, circleY, circleRadius, paint)

        paint.color = ballColor
        canvas.drawCircle(ballX, ballY, ballRadius, paint)

        updatePhysics()
        postInvalidateDelayed(1)
    }

    private fun updatePhysics() {
        velocityX += accelX * 0.5f
        velocityY += accelY * 0.5f
        velocityY += gravity

        ballX += velocityX
        ballY += velocityY

        val distX = ballX - circleX
        val distY = ballY - circleY
        val distance = sqrt((distX * distX + distY * distY).toDouble()).toFloat()

        if (distance + ballRadius >= circleRadius) {
            val normX = distX / distance
            val normY = distY / distance
            val dotProduct = velocityX * normX + velocityY * normY
            velocityX -= 2 * dotProduct * normX
            velocityY -= 2 * dotProduct * normY

            ballColor = Color.rgb(Random.nextInt(50, 255), Random.nextInt(50, 255), Random.nextInt(50, 255))
            circleRadius += 1
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelX = -it.values[0] / 10
                accelY = it.values[1] / 10
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun resetGame() {
        initializeGame()
        invalidate()
    }

    fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }
}
