package com.example.noisefilter

import android.app.Activity
import android.media.*
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar

class MainActivity : Activity() {

    private var isRunning = false
    private var gain = 0.8f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startBtn = findViewById<Button>(R.id.startBtn)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                gain = value / 100f
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        startBtn.setOnClickListener {
            if (!isRunning) {
                isRunning = true
                Thread { startAudio() }.start()
                startBtn.text = "Stop"
            } else {
                isRunning = false
                startBtn.text = "Start"
            }
        }
    }

    private fun startAudio() {
        val sampleRate = 16000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val track = AudioTrack(
            AudioManager.STREAM_VOICE_CALL,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize,
            AudioTrack.MODE_STREAM
        )

        val buffer = ShortArray(bufferSize)

        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor.create(record.audioSessionId)
        }

        record.startRecording()
        track.play()

        while (isRunning) {
            val read = record.read(buffer, 0, buffer.size)
            for (i in 0 until read) {
                buffer[i] = (buffer[i] * gain).toInt().toShort()
            }
            track.write(buffer, 0, read)
        }

        record.stop()
        track.stop()
    }
}
