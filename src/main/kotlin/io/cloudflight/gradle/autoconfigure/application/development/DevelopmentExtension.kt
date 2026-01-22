package io.cloudflight.gradle.autoconfigure.application.development

import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.WriteProperties
import org.gradle.language.jvm.tasks.ProcessResources

object DevelopmentExtension {

    fun create(project: Project) {
        val propertiesTask =
            project.tasks.register("clfDevelopmentProperties", WriteProperties::class.java) {
                property("development.name", project.name)
                property("development.group", project.group.toString())
                property("development.version", project.version.toString())
                encoding = "UTF-8"
                group = AutoConfigureGradlePlugin.TASK_GROUP
                destinationFile.set(project.layout.buildDirectory.file("generated/resources/development/development.properties"))
            }
        project.tasks.named(JavaPlugin.PROCESS_RESOURCES_TASK_NAME, ProcessResources::class.java).get()
            .from(propertiesTask.get())
    }
}
