package de.cwtrainer.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.KeyboardFocusManager
import java.beans.PropertyChangeListener

fun main() = application {
    val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
    val focusListener = PropertyChangeListener { event ->
        notifyAppVisibilityChanged(event.newValue != null)
    }
    focusManager.addPropertyChangeListener("activeWindow", focusListener)
    Window(onCloseRequest = {
        focusManager.removePropertyChangeListener("activeWindow", focusListener)
        exitApplication()
    }, title = "CW-Trainer") {
        CwTrainerApp()
    }
}
