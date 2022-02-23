package dev.helight.hopper.ecs

import dev.helight.hopper.ecs
import kotlinx.serialization.ExperimentalSerializationApi
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.Duration
import java.time.Instant

@ExperimentalUnsignedTypes
@ExperimentalSerializationApi
class EcsBackupJob : Job {

    override fun execute(context: JobExecutionContext) {
        println("Backing up ECS System")
        val t1 = Instant.now()
        ecs.storeSnapshot()
        val t2 = Instant.now()
        println("Backup completed in ${Duration.between(t1, t2).toMillis()}ms")
    }

}