import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    //alias(libs.plugins.android.kotlin.multiplatform.library)
    //id("maven-publish")
    //id("signing")
    alias(libs.plugins.vanniktech.mavenPublish)
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

android {
    namespace = "com.mohit.sharedutil"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

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
/*publishing {
    repositories {
        maven {
            // Local test repo inside your build directory
            //name = "localRelease"
            val releasesRepoUrl = layout.buildDirectory.dir("repos/releases")
            val snapshotsRepoUrl = layout.buildDirectory.dir("repos/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
        // Maven Central/Sonatype OSSRH
        maven {
            name = xcfName
            val isSnapshot = version.toString().endsWith("SNAPSHOT")
            url = uri(
                if (!isSnapshot) "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
                else "https://central.sonatype.com/repository/maven-snapshots/"
            )
            credentials {
                username = project.findProperty("mavenCentralUserName") as String?
                password = project.findProperty("mavenCentralPassword") as String?
            }
        }
    }

}*/
/*signing {
    useInMemoryPgpKeys(
        project.findProperty("signing.keyId"),
        file(project.findProperty("signing.secretKeyRingFile") as String).readText()
        project.findProperty("signing.password")
    )
    sign(publishing.publications["release"])
}*/
