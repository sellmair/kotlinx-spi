plugins {
    kotlin("jvm")
    `maven-publish`
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

dependencies {
    implementation(kotlin("compiler-embeddable:2.0.20"))
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    implementation("org.ow2.asm:asm:9.7")
}
