package io.cloudflight.gradle.autoconfigure.java

import org.gradle.api.Project

internal fun isJavaProject(project: Project): Boolean {
    val javaDirs = project.layout.projectDirectory.asFileTree.matching {
        include("src/*/java/")
    }
    return !javaDirs.isEmpty
}
