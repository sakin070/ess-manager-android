package com.ess.manager_ui_native

import com.squareup.tape2.QueueFile
import java.io.File

class QueueManager {
    private val file = File("ess_network_writes")
    val queueFile = QueueFile.Builder(file).build()
    companion object {
        private val instance = QueueManager()
        fun instance() = instance
    }
}