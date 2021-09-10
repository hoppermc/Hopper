package dev.helight.hopper.kapt

import java.io.File
import javax.annotation.processing.ProcessingEnvironment

object SharedUtils {

    const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    const val KAPT_HOPPER_RESOURCE_PATH_OPTION_NAME = "resources"
    const val KAPT_HOPPER_SOURCE_PATH_OPTION_NAME = "source"

    fun buildPaths(processingEnv: ProcessingEnvironment): BuildPaths {
        val srcOut = File(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]!!)
        val resOut = when (val resourceLocation = processingEnv.options[KAPT_HOPPER_RESOURCE_PATH_OPTION_NAME]) {
            null -> File(srcOut.parentFile.parentFile.parentFile.parentFile, "resources/main")
            else -> File(resourceLocation)
        }
        val inDirectory = File(processingEnv.options[KAPT_HOPPER_SOURCE_PATH_OPTION_NAME] ?: "src/main")
        return BuildPaths(
            srcOut,
            resOut,
            File(inDirectory, "kotlin"),
            File(inDirectory, "resources")
        )
    }

    tailrec fun toKebapCase(camelCase: String): String {
        val firstUppercaseIndex = camelCase.indexOfFirst{ it.isUpperCase() }
        return if (firstUppercaseIndex == -1) {
            camelCase
        } else {
            val updated = camelCase.toMutableList()
            updated[firstUppercaseIndex] = updated[firstUppercaseIndex].lowercaseChar()
            if (firstUppercaseIndex != 0) updated.add(firstUppercaseIndex, '-')
            toKebapCase(updated.joinToString(""))
        }
    }
}

data class BuildPaths(
    val outSource: File,
    val outResources: File,
    val inSource: File,
    val inResources: File
)