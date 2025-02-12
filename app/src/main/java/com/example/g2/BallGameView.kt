package com.example.g2

import android.content.Context
import android.graphics.*
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
    private lateinit var backgroundBitmap: Bitmap
    private lateinit var ballBitmap: Bitmap
    private var speed = 4f
    private val gravity = 0.1f
    private var accelX = 0f
    private var accelY = 0f
    private var lastFrameTime = System.nanoTime()
    private var fps = 0

    // След из точек
    private val trail = mutableListOf<Pair<Float, Float>>()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        paint.style = Paint.Style.FILL
        initializeGame()

        // Загружаем фон и мяч
        backgroundBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.background)
        val originalBallBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.bols)
        ballBitmap = Bitmap.createScaledBitmap(originalBallBitmap, 80, 80, true) // Масштабируем мяч

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    private fun initializeGame() {
        val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = context.resources.displayMetrics.heightPixels.toFloat()

        circleX = screenWidth / 2
        circleY = screenHeight / 2

        circleRadius = 100f
        ballRadius = 40f

        val angle = Random.nextFloat() * 2 * PI.toFloat()
        val radius = Random.nextFloat() * (circleRadius - ballRadius)
        ballX = circleX + radius * cos(angle.toDouble()).toFloat()
        ballY = circleY + radius * sin(angle.toDouble()).toFloat()

        velocityX = speed * if (Random.nextBoolean()) 1 else -1
        velocityY = speed * if (Random.nextBoolean()) 1 else -1
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Рассчитываем FPS
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000.0 // в секундах
        lastFrameTime = currentTime
        fps = (1 / deltaTime).toInt()

        updatePhysics()

        // Рисуем фон
        canvas.drawBitmap(backgroundBitmap, (width - backgroundBitmap.width) / 2f, (height - backgroundBitmap.height) / 2f, paint)

        // Рисуем круг
        paint.color = Color.BLACK
        canvas.drawCircle(circleX, circleY, circleRadius, paint)

        // Рисуем градиентный след
        drawTrail(canvas)

        // Рисуем мяч как изображение
        canvas.drawBitmap(ballBitmap, ballX - ballRadius, ballY - ballRadius, paint)

        // Отображаем FPS
        paint.color = Color.WHITE
        paint.textSize = 50f
        canvas.drawText("FPS: $fps", 50f, 80f, paint)

        postInvalidateDelayed(2)
    }

    private fun drawTrail(canvas: Canvas) {
        if (trail.size < 2) return

        val gradientPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 20f
            isAntiAlias = true
        }

        val path = Path()
        path.moveTo(trail[0].first, trail[0].second)

        for (i in 1 until trail.size) {
            path.lineTo(trail[i].first, trail[i].second)
        }

        val gradient = LinearGradient(
            trail.first().first, trail.first().second,
            trail.last().first, trail.last().second,
            Color.argb(255, 255, 255, 255), // Яркий белый в начале
            Color.argb(0, 255, 255, 255), // Прозрачный в конце
            Shader.TileMode.CLAMP
        )

        gradientPaint.shader = gradient
        canvas.drawPath(path, gradientPaint)
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

            ballX = circleX + (circleRadius - ballRadius) * normX
            ballY = circleY + (circleRadius - ballRadius) * normY

            val dotProduct = velocityX * normX + velocityY * normY
            velocityX -= 2 * dotProduct * normX
            velocityY -= 2 * dotProduct * normY

            circleRadius += 1
        }

        // Добавляем позицию в след (максимум 20 точек)
        if (trail.size > 20) trail.removeAt(0)
        trail.add(Pair(ballX, ballY))
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelX = -it.values[0]
                accelY = it.values[1]
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun resetGame() {
        initializeGame()
        trail.clear() // Очистка следа при перезапуске
        invalidate()
    }

    fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }
}
