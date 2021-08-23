package dev.helight.hopper.ecs

import dev.helight.hopper.*
import java.lang.reflect.Field
import java.lang.reflect.Parameter
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HopperComponentId(val id: Int)

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HopperComponentClass(val clazz: KClass<*>)

object ComponentDataProjectionReflectors {
    val projectors: MutableMap<Pair<Class<*>, ComponentGroup>, Projector<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> getProjector(group: ComponentGroup): Projector<T> {
        val clazz = T::class.java
        val key = clazz to group
        return when (val existing: Projector<*>? = projectors[key]) {
            null -> {
                val newProjector = Projector(clazz, group)
                projectors[key] = newProjector
                newProjector
            }
            else -> existing as Projector<T>
        }
    }

    @Suppress("DuplicatedCode", "UNCHECKED_CAST")
    class Projector<T>(
        private val clazz: Class<T>,
        private val group: ComponentGroup
    ) {
        private val fieldIdList: List<Pair<Field, ComponentID>> = clazz.declaredFields.map(this::mapField).filter { group.contains(it.second) }.sortedBy { it.second }

        private fun mapField(field: Field): Pair<Field, ComponentID> {
            field.isAccessible = true
            val intermediate = when (val idAnnotation: HopperComponentId? = field.getAnnotation(HopperComponentId::class.java)) {
                null -> null
                else -> field to customId(idAnnotation.id)
            }
            if (intermediate != null) return intermediate
            return when (val idAnnotation: HopperComponentClass? = field.getAnnotation(HopperComponentClass::class.java)) {
                null -> field to field.type.toKey()
                else -> field to idAnnotation.clazz.java.toKey()
            }
        }

        private fun mapParameter(parameter: Parameter): Pair<Parameter, ComponentID> {
            val intermediate = when (val idAnnotation: HopperComponentId? = parameter.getAnnotation(HopperComponentId::class.java)) {
                null -> null
                else -> parameter to customId(idAnnotation.id)
            }
            if (intermediate != null) return intermediate
            return when (val idAnnotation: HopperComponentClass? = parameter.getAnnotation(HopperComponentClass::class.java)) {
                null -> parameter to parameter.type.toKey()
                else -> parameter to idAnnotation.clazz.java.toKey()
            }
        }

        fun project(data: List<ComponentData>): T {
            val constructor = clazz.constructors.firstOrNull { it.parameterCount == data.size }

            if (constructor != null) {
                val parameters = constructor.parameters.map(this::mapParameter).map {
                    val index = group.indexOf(it.second)
                    println(it.second)
                    data[index]
                }.toTypedArray()
                return constructor.newInstance(*parameters) as T
            }

            val instance = clazz.newInstance()
            fieldIdList.forEachIndexed { index, pair ->
                pair.first.set(instance, data[index])
            }
            return instance
        }
    }
}