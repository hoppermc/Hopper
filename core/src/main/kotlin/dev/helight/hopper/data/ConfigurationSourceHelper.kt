package dev.helight.hopper.data

//Primarily internal helper for handling the annotations for ConfigurationSource
object ConfigurationSourceHelper {
    fun <T> configName(clazz: Class<T>): String {
        return if (clazz.isAnnotationPresent(ConfigName::class.java)) {
            clazz.getAnnotation(ConfigName::class.java).name
        } else {
            clazz.name
        }
    }

    fun <T> configGroup(clazz: Class<T>): String {
        return if (clazz.isAnnotationPresent(ConfigName::class.java)) {
            clazz.getAnnotation(ConfigName::class.java).group
        } else {
            clazz.`package`.name.replace(".", "")
        }
    }


    fun <T> layout(clazz: Class<T>): Collection<LayoutEntry> {
        return clazz.declaredFields.map {
            it.isAccessible = true
            if (it.isAnnotationPresent(ConfigComment::class.java)) {
                val commentAnnotation = it.getAnnotation(ConfigComment::class.java)
                return@map LayoutEntry(it.name, it.type, commentAnnotation.comment)
            } else {
                return@map LayoutEntry(it.name, it.type, null)
            }
        }.toList()
    }
}

data class LayoutEntry(
    val key: String,
    val type: Class<*>,
    val comment: String?
)