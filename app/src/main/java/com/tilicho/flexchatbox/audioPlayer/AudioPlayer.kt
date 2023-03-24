package com.tilicho.flexchatbox.audioPlayer

import android.media.MediaPlayer
import java.io.File

interface AudioPlayer {
    fun playFile()
    fun initMediaPlayer(file: File?)
    fun stop()
    fun pause()
    fun getMediaPlayer(): MediaPlayer?
}