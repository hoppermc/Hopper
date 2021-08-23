package dev.helight.hopper.data.configurations

import dev.helight.hopper.data.ConfigComment
import dev.helight.hopper.data.ConfigName

@ConfigName(group = "hopper", name = "data-config")
data class HopperDataConfiguration(

    @ConfigComment("selects the datasource that will be used. Available: [mongo, memory]")
    val datasource: String = "memory",

    @ConfigComment("enables or disables the mongodb datasource")
    val mongo: Boolean = true,

    @ConfigComment("the connection string used to connect to the mongodb datasource")
    val mongoConnectionString: String = "mongodb://localhost:27017",

    @ConfigComment("the default database that will be used by the engine")
    val mongoDatabase: String = "test"

)