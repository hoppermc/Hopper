package dev.helight.hopper.data

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ConfigComment(val comment: String)
