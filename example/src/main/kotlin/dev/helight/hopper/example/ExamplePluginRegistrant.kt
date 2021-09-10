package dev.helight.hopper.example

import dev.helight.hopper.AnnotatedPluginRegistrant

@AnnotatedPluginRegistrant
class ExamplePluginRegistrant {

    init {
        println("ExamplePluginRegistrant constructed!")
    }

}