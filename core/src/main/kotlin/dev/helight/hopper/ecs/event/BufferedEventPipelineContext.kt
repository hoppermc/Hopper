package dev.helight.hopper.ecs.event

import dev.helight.hopper.ecs.BufferedEntity

@ExperimentalUnsignedTypes
data class BufferedEventPipelineContext<T>(val entity: BufferedEntity)