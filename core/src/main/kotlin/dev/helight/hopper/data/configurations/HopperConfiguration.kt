package dev.helight.hopper.data.configurations

import dev.helight.hopper.data.ConfigComment
import dev.helight.hopper.data.ConfigName

@ConfigName(group = "hopper", name = "common-config")
data class HopperConfiguration(

    @ConfigComment("sets the internal name-slug of this instance. Primarily not visible to users.")
    val instanceName: String = "odysseus-server",

    @ConfigComment("enables and disables automatic plugin updates for odysseus and connected plugins.")
    val autoUpdater: Boolean = true,

    @ConfigComment("sets the default language code in ISO-639-1 format.")
    val langCode: String = "en",

    @ConfigComment("sets the default configuration source of odysseus and its " +
            "connected plugins. The changes dont affect this specific configuration.")
    val configurationSource: String = "JsonFile",

    @ConfigComment("changes whether or not the ecs system shall persist component data and the entity id counter")
    val persistEcs: Boolean = true,

    @ConfigComment("changes how the ecs system will generate new ids. Available: [incremental, snowflake]")
    val ecsEntityIdGenerationStrategy: String = "incremental",

)