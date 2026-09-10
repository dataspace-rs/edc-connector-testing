import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

plugins {
    `java-library`
    alias(libs.plugins.shadow)
    alias(libs.plugins.docker)
    alias(libs.plugins.edc.build)
}


val edcBuildId = libs.plugins.edc.build.get().pluginId

allprojects {
    apply(plugin = edcBuildId)
}

subprojects {
    afterEvaluate {
        if (project.plugins.hasPlugin("com.gradleup.shadow") &&
                file("${project.projectDir}/src/main/resources/docker/Dockerfile").exists()
        ) {

            //actually apply the plugin to the (sub-)project
            apply(plugin = "com.bmuschko.docker-remote-api")
            // configure the "dockerize" task
            val dockerTask: DockerBuildImage = tasks.create("dockerize", DockerBuildImage::class) {
                val dockerContextDir = project.projectDir
                dockerFile.set(file("$dockerContextDir/src/main/resources/docker/Dockerfile"))
                images.add("${project.name}:${project.version}")
                images.add("${project.name}:latest")
                // specify platform with the -Dplatform flag:
                if (System.getProperty("platform") != null)
                    platform.set(System.getProperty("platform"))
                buildArgs.put("JAR", "build/libs/${project.name}.jar")
                inputDir.set(file(dockerContextDir))
            }
            // make sure  always runs after "dockerize" and after "copyOtel"
            dockerTask.dependsOn(tasks.named("shadowJar"))
        }
    }
}
