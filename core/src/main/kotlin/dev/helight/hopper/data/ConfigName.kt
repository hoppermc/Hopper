package dev.helight.hopper.data

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class ConfigName(val name: String, val group: String = "odysseus")
