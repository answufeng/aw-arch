apply(plugin = "maven-publish")

extensions.configure<org.gradle.api.publish.PublishingExtension> {
    publications {
        register<org.gradle.api.publish.maven.MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }

            groupId = "com.github.answufeng"
            artifactId = "aw-arch"
            version = property("VERSION_NAME")?.toString() ?: "1.0.0"

            pom {
                name.set("aw-arch")
                description.set("Android architecture library based on Kotlin + MVVM/MVI + Hilt")
                url.set("https://github.com/answufeng/aw-arch")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("answufeng")
                        name.set("answufeng")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/answufeng/aw-arch.git")
                    url.set("https://github.com/answufeng/aw-arch")
                }
            }
        }
    }
}
