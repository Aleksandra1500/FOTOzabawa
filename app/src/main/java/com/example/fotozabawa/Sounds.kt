package com.example.fotozabawa

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build

class Sounds(context: Context) {
    private var soundPool: SoundPool

    init {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            var audioAttributes: AudioAttributes = AudioAttributes.Builder()
                .setUsage(
                    AudioAttributes
                        .USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(
                    AudioAttributes
                        .CONTENT_TYPE_SONIFICATION)
                .build();

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build();
        } else {
            soundPool = SoundPool(6, AudioManager.STREAM_MUSIC, 0)
        }
    }
    private var soundBip: Int = soundPool.load(context, R.raw.audio_bip, 1)
    private var soundShutter: Int = soundPool.load(context, R.raw.audio_shutter, 1)
    private var soundDone: Int = soundPool.load(context, R.raw.audio_done, 1)
    private var soundMessage: Int = soundPool.load(context, R.raw.audio_message, 1)

    fun bip() { soundPool.play(soundBip, 1F, 1F, 0, 0, 1F) }
    fun shutter() { soundPool.play(soundShutter, 1F, 1F, 0, 0, 1F) }
    fun done() { soundPool.play(soundDone, 1F, 1F, 0, 0, 1F) }
    fun message() { soundPool.play(soundMessage, 1F, 1F, 0, 0, 1F) }
}