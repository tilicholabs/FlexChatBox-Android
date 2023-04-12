package `in`.tilicho.flexchatbox.audiorecorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}