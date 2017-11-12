package com.codebutler.funwithnativecode

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat.checkSelfPermission
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.nio.ByteBuffer
import kotlin.concurrent.fixedRateTimer

@SuppressLint("SdCardPath")
class MainActivity : Activity() {

    companion object {
        private const val REQUEST_CODE_PERMISSION = 10001

        const val RETRO_DEVICE_JOYPAD = 1

        const val RETRO_DEVICE_ID_JOYPAD_START = 3
        const val RETRO_DEVICE_ID_JOYPAD_UP    = 4
        const val RETRO_DEVICE_ID_JOYPAD_DOWN  = 5
        const val RETRO_DEVICE_ID_JOYPAD_LEFT  = 6
        const val RETRO_DEVICE_ID_JOYPAD_RIGHT = 7
    }

    private val audioTrack = AudioTrack.Builder()
            .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(32040)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build())
            .build()

    private val audioBytesPerFrame = 4 // 16-bit stereo

    private val audioSampleBatch = object : retro_audio_sample_batch {
        override fun invoke(data: Pointer, frames: SizeT): SizeT {
            val length = frames * audioBytesPerFrame
            val buffer = data.getByteArray(0, length)

            val written = audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            return SizeT(written)
        }
    }

    private val button by lazy {
        findViewById<Button>(R.id.button)
    }

    private val imageView by lazy {
        findViewById<ImageView>(R.id.imageView)
    }

    private val videoBytesPerPixel = 2 // RGB 565

    private val videoRefresh = object : retro_video_refresh {
        override fun invoke(data: Pointer, width: UInt, height: UInt, pitch: SizeT) {
            val widthBytes = width * videoBytesPerPixel
            val totalBytes = width * height * videoBytesPerPixel

            val buffer = ByteArray(totalBytes)
            for (i in 0 until height.toInt()) {
                data.read(i * pitch.toLong(), buffer, i * widthBytes, widthBytes)
            }

            val bitmap = Bitmap.createBitmap(
                    width.toInt(),
                    height.toInt(),
                    Bitmap.Config.RGB_565)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(buffer))

            imageView.post {
                imageView.setImageBitmap(bitmap)
            }
        }
    }

    private val environment = object : retro_environment {
        override fun invoke(cmd: UInt, data: Pointer): Boolean = false
    }

    private val inputPoll = object : retro_input_poll {
        override fun invoke() = Unit
    }

    private val pressedKeys = mutableSetOf<Int>()

    private val inputState = object : retro_input_state {
        override fun invoke(port: UInt, device: UInt, index: UInt, id: UInt): Short {
            if (port.toInt() != 0) { // Multiplayer support
                return 0
            }
            if (device.toInt() != RETRO_DEVICE_JOYPAD) {
                return 0
            }
            val isPressed = when (id.toInt()) {
                RETRO_DEVICE_ID_JOYPAD_START ->
                    pressedKeys.contains(KeyEvent.KEYCODE_ENTER)
                RETRO_DEVICE_ID_JOYPAD_UP ->
                    pressedKeys.contains(KeyEvent.KEYCODE_DPAD_UP)
                RETRO_DEVICE_ID_JOYPAD_DOWN ->
                    pressedKeys.contains(KeyEvent.KEYCODE_DPAD_DOWN)
                RETRO_DEVICE_ID_JOYPAD_LEFT ->
                    pressedKeys.contains(KeyEvent.KEYCODE_DPAD_LEFT)
                RETRO_DEVICE_ID_JOYPAD_RIGHT ->
                    pressedKeys.contains(KeyEvent.KEYCODE_DPAD_RIGHT)
                else -> false
            }
            return if (isPressed) 1 else 0
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener { onLaunchButtonClick() }

        if (checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSION)
        } else {
            permissionReady()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults[0] == PERMISSION_GRANTED) {
            permissionReady()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        super.dispatchKeyEvent(event)
        when (event.action) {
            KeyEvent.ACTION_DOWN -> pressedKeys.add(event.keyCode)
            KeyEvent.ACTION_UP -> pressedKeys.remove(event.keyCode)
        }
        return true
    }

    private fun permissionReady() {
        button.visibility = View.VISIBLE
    }

    private fun onLaunchButtonClick() {
        button.visibility = View.GONE

        val lib = Native.loadLibrary(
                "snes9x_libretro_android",
                LibRetro::class.java)

        lib.retro_set_environment(environment)
        lib.retro_set_audio_sample_batch(audioSampleBatch)
        lib.retro_set_video_refresh(videoRefresh)
        lib.retro_set_input_poll(inputPoll)
        lib.retro_set_input_state(inputState)
        lib.retro_init()

        val gameInfo = retro_game_info()
        gameInfo.path = "/sdcard/allstars.smc"
        lib.retro_load_game(gameInfo)

        fixedRateTimer(period = 1000L / 60L) {
            lib.retro_run()
        }
    }
}
