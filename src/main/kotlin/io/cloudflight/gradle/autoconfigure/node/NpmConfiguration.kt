package io.cloudflight.gradle.autoconfigure.node

import com.github.gradle.node.npm.task.NpmInstallTask
import com.github.gradle.node.npm.task.NpmTask
import io.cloudflight.gradle.autoconfigure.AutoConfigureGradlePlugin
import io.cloudflight.gradle.autoconfigure.util.BuildUtils
import io.cloudflight.gradle.autoconfigure.util.EnvironmentUtils
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.language.base.plugins.LifecycleBasePlugin

internal object NpmConfiguration {

    fun apply(project: Project, node: NodeConfigurePluginExtension) {
        val taskClass = NpmTask::class.java
        val taskPrefix = "clfNpm"

        val install = project.tasks.getByName(NpmInstallTask.NAME) as NpmInstallTask

        val updateVersion = project.tasks.register("${taskPrefix}UpdateVersion", taskClass) {
            group = AutoConfigureGradlePlugin.TASK_GROUP
            npmCommand.set(listOf("version"))
            args.set(project.provider {
                listOf(
                    project.version.toString(),
                    "--allow-same-version",
                    "--no-git-tag-version"
                )
            })
            inputs.files(node.inputFiles)
        }

        val lint = project.tasks.register(NodeConfigurePlugin.NPM_LINT_TASK_NAME, taskClass) {
            group = AutoConfigureGradlePlugin.TASK_GROUP
            args.set(listOf("run", "lint"))
            dependsOn(install)
            inputs.files(node.inputFiles)
            outputs.upToDateWhen { true }
        }

        project.tasks.register("${taskPrefix}BuildDev", taskClass) {
            group = AutoConfigureGradlePlugin.TASK_GROUP
            args.set(listOf("run", "build:dev"))
            dependsOn(install)
        }

        val build = project.tasks.register(NodeConfigurePlugin.NPM_BUILD_TASK_NAME, taskClass) {
            group = AutoConfigureGradlePlugin.TASK_GROUP
            args.set(listOf("run", "build"))
            dependsOn(install)
            inputs.files(node.inputFiles)
            outputs.dir(node.destinationDir)
        }

        project.tasks.register("${taskPrefix}Audit", taskClass) {
            group = AutoConfigureGradlePlugin.TASK_GROUP
            args.set(listOf("audit"))
            dependsOn(install)
        }

        if (BuildUtils.isIntegrationBuild() && !EnvironmentUtils.isVerifyBuild()) {
            install.dependsOn(updateVersion)
        }

        project.tasks.getByName(LifecycleBasePlugin.CHECK_TASK_NAME).dependsOn(lint)

        //this prevents running npmBuild each time when a project is started via intellij
        project.tasks.getByName(JavaPlugin.COMPILE_JAVA_TASK_NAME).mustRunAfter(build)
        project.tasks.getByName(JavaPlugin.JAR_TASK_NAME).dependsOn(build)

        val javaPluginExtension = project.extensions.getByType(JavaPluginExtension::class.java)
        val sourceSetMain = javaPluginExtension.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)

        sourceSetMain.java.srcDirs(NpmHelper.determineSourceDirs(project))
        sourceSetMain.output.dir(mapOf("builtBy" to build), node.destinationDir)

        if (NpmHelper.hasScript("test", project.file(NpmHelper.PACKAGE_JSON))) {
            val npmTest = project.tasks.register("${taskPrefix}Test", taskClass) {
                group = AutoConfigureGradlePlugin.TASK_GROUP
                args.set(listOf("run", "test"))
                dependsOn(listOf(install, build))
                inputs.files(node.inputFiles)
                environment.put("GRADLE_BUILD", true.toString())
                environment.put("INTEGRATION_BUILD", BuildUtils.isIntegrationBuild().toString())
            }
            project.tasks.getByName("test").dependsOn(npmTest)
        }
    }
}
