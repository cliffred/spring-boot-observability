plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jgit)
}

gradlePlugin {
    plugins {
        create("releasePlugin") {
            id = "release-plugin"
            implementationClass = "red.cliff.gradle.ReleasePlugin"
        }
    }
}
