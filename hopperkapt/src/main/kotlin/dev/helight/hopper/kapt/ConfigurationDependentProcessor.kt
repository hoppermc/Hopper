package dev.helight.hopper.kapt

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import dev.helight.hopper.AnnotatedPluginRegistrant
import dev.helight.hopper.AutoComponent
import dev.helight.hopper.AutoConfigurePlugin
import dev.helight.hopper.HopperPluginConfiguration
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@DelicateKotlinPoetApi("Yeah I kinda think I know what I'm doing")
@AutoService(Processor::class)
class ConfigurationDependentProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AutoConfigurePlugin::class.java.name, AutoComponent::class.java.name)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        //val paths = SharedUtils.buildPaths(processingEnv)
        //val configurePluginTarget = roundEnv.getElementsAnnotatedWith(AutoConfigurePlugin::class.java).firstOrNull()
        println("Entering new round with $annotations")
        if(annotations.isEmpty()) return false

        val context = beginProcessing(roundEnv)
        roundEnv.getElementsAnnotatedWith(AutoComponent::class.java)
            .forEach {
                if (it.kind != ElementKind.CLASS) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Invalid annotated kind")
                    return false
                }
                processAnnotated(it, context)
            }
        finalizeProcessing(context)
        return true
    }

    private fun beginProcessing(roundEnv: RoundEnvironment): ProcessingContext {
        val paths = SharedUtils.buildPaths(processingEnv)
        val config = when(val autoConfigureElement = roundEnv.getElementsAnnotatedWith(AutoConfigurePlugin::class.java).firstOrNull()) {
            null -> Json.decodeFromString(File(paths.inResources, "hopper.json").readText())
            else -> {
                val main = autoConfigureElement.asType().asTypeName().toString()
                val mainPathSplinted = main.split(".")
                HopperPluginConfiguration(
                    mainClass = main,
                    namespace = mainPathSplinted.subList(0, mainPathSplinted.size - 1).joinToString(separator = "."),
                    id = SharedUtils.toKebapCase(mainPathSplinted.last())
                )
            }
        }

        return ProcessingContext(config, mutableListOf(), paths.outSource, paths.outResources)
    }

    private fun finalizeProcessing(context: ProcessingContext) {
        if (context.types.isEmpty()) {
            processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "No components found. Skipping creation of registrant")
            return
        }

        generateSerializers(context)

        context.resOut.mkdirs()
        File(context.resOut, "hopper.json").writeText(Json.encodeToString(context.config))
    }

    private fun generateSerializers(context: ProcessingContext) {
        val registerStatements = context.types.map {
            val annotation = it.getAnnotation(AutoComponent::class.java)
            val serializer = annotation.toString().split("serializer=").last().removeSuffix(")") // Just don't ask
            if (serializer == "dev.helight.hopper.ecs.ComponentSerializer") {
                return@map CodeBlock.of("""
                    ecs.registerDefaultSerializerForClass(${it.asType().asTypeName()}::class.java)
                """.trimIndent())
            } else {
                return@map CodeBlock.of("""
                    ecs.serializer<${it.asType().asTypeName()}>($serializer())
                """.trimIndent())
            }
        }.joinToString(separator = "\n", postfix = "\n")

        val typeSpec = TypeSpec.classBuilder("ComponentSerializerRegistrant")
            .addAnnotation(AnnotatedPluginRegistrant::class.java)
            .addInitializerBlock(
                CodeBlock.of("""
                    $registerStatements
                    println("Registered compiler generated default serializers for plugin '${context.config.id}'")
                    
                """.trimIndent())
            )
            .build()
        val fileSpec = FileSpec.builder("${context.config.namespace}.generated", "ComponentSerializerRegistrantContainer")
        fileSpec.addImport("dev.helight.hopper", "ecs")
        fileSpec.addType(typeSpec)
        fileSpec.build().writeTo(context.srcOut)
    }

    private fun processAnnotated(element: Element, context: ProcessingContext) {
        println("${element.simpleName} is a processing target")

        context.types.add(element)
    }

    class ProcessingContext(
        val config: HopperPluginConfiguration,
        val types: MutableList<Element>,
        val srcOut: File,
        val resOut: File
    )
}