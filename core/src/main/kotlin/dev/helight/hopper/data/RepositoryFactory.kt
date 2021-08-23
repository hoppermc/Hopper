package dev.helight.hopper.data


object RepositoryFactory {

    val suppliers: MutableMap<String, RepositorySupplier> = mutableMapOf()
    var defaultDatasource: String = "mongo"

    @Suppress("UNCHECKED_CAST")
    fun <T: PersistentEntity> getRepository(type: Class<T>, datasource: String = defaultDatasource) : SimpleCrudRepository<T> {
        return suppliers[datasource]!!.supply(type)
    }

    inline fun getCborEntity() {

    }

}

interface RepositorySupplier {

    fun <T: PersistentEntity> supply(type: Class<T>): SimpleCrudRepository<T>

}