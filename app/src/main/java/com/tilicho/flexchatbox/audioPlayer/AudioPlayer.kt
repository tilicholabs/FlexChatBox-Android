package com.tilicho.flexchatbox.audioPlayer

import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun stop()
}