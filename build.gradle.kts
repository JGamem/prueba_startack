import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion = "0.50.0"
val koinVersion = "3.5.3"

plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.1"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "stsa.kotlin-htmx"
version = "0.0.1"
val mainClassString = "stsa.kotlin_htmx.ApplicationKt"
application {
    mainClass.set(mainClassString)
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        manifest {
            attributes["Main-Class"] = mainClassString
        }
        archiveBaseName.set("kotlin-htmx")
        mergeServiceFiles()
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
}

dependencies {
    // Ktor Server
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-forwarded-header-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-caching-headers-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-sse:$ktorVersion")
    implementation("io.ktor:ktor-server-compression-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder:$ktorVersion")

    // Ktor Client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    // Exposed (ORM)
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Database
    implementation("org.postgresql:postgresql:42.7.2")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Koin (DI)
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")

    // Jackson (JSON)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.18.1")

    // HTML Builder
    implementation("org.jetbrains:kotlin-css-jvm:1.0.0-pre.156-kotlin-1.5.0")

    // Validation
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.glassfish.expressly:expressly:5.0.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.nfeld.jsonpathkt:jsonpathkt:2.0.1")

    // Testing
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.xmlunit:xmlunit-core:2.10.0")
    testImplementation("org.xmlunit:xmlunit-assertj3:2.10.0")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("com.h2database:h2:2.2.224")
}

tasks.withType<DependencyUpdatesTask> {
    resolutionStrategy {
        componentSelection {
            all {
                if (candidate.version.contains("beta", true)
                    || candidate.version.contains("-rc", true)
                    || candidate.version.endsWith("-M1")) {
                    reject("Not a release")
                }
            }
        }
    }
}