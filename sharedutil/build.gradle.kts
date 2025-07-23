import com.android.build.api.dsl.androidLibrary
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    id("maven-publish")
    id("signing")
}

// Load sensitive properties from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

fun requireLocalProperty(name: String): String =
    localProperties.getProperty(name) ?: throw GradleException("Property '$name' is missing from local.properties")

val xcfName = project.findProperty("POM_ARTIFACT_ID") as String

kotlin {
    androidLibrary {
        namespace = "com.mohit.sharedutil"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = xcfName
            isStatic = true
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
            }
        }

        androidMain {
            dependencies {}
        }

        iosMain {
            dependencies {}
        }
    }

}

group = project.findProperty("GROUP_ID") as String
version = project.findProperty("VERSION_NAME") as String

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = xcfName
            version = project.version.toString()
            from(components["kotlin"])

            pom {
                name.set(project.findProperty("POM_NAME") as String)
                description.set(project.findProperty("POM_DESCRIPTION") as String)
                url.set(project.findProperty("POM_URL") as String)

                licenses {
                    license {
                        name.set(project.findProperty("POM_LICENSE_NAME") as String)
                        url.set(project.findProperty("POM_LICENSE_URL") as String)
                    }
                }

                developers {
                    developer {
                        id.set(project.findProperty("POM_DEVELOPER_ID") as String)
                        name.set(project.findProperty("POM_DEVELOPER_NAME") as String)
                        email.set(project.findProperty("POM_DEVELOPER_EMAIL") as String)
                    }
                }

                scm {
                    url.set(project.findProperty("POM_SCM_URL") as String)
                    connection.set(project.findProperty("POM_SCM_CONNECTION") as String)
                    developerConnection.set(project.findProperty("POM_SCM_DEV_CONNECTION") as String)
                }
            }
        }
    }

    repositories {
        maven {
            name = "localTest"
            val releasesRepoUrl = layout.buildDirectory.dir("repos/releases")
            val snapshotsRepoUrl = layout.buildDirectory.dir("repos/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }

        maven {
            //name = xcfName
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = uri(
                if (isSnapshot)
                    "https://s01.oss.sonatype.org/content/repositories/snapshots"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
            )
            credentials {
                username = requireLocalProperty("mavenCentralUserName")
                password = requireLocalProperty("mavenCentralPassword")
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        requireLocalProperty("signing.keyId"),
        file(requireLocalProperty("signing.secretKeyRingFile")).readText(),
        requireLocalProperty("signing.password")
    )
    sign(publishing.publications["release"])
}
