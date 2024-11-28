object Versions {
    const val JASYPT_VERSION = "3.0.5"
    const val P6SPY_VERSION = "1.9.2"
    const val KOTLIN_LOGGING_VERSION = "7.0.0"
    const val MOCKK_VERSION = "1.13.12"
    const val KOTEST_VERSION = "5.9.1"
    const val MAILJET_VERSION = "5.2.5"
}

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.allopen") version "1.9.25"
}

apply(plugin = "kotlin-jpa")

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

group = "dulian"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

configurations {
    configureEach {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    }
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Jasypt
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:${Versions.JASYPT_VERSION}")

    // Log4j2
    implementation("org.springframework.boot:spring-boot-starter-log4j2")

    // P6Spy
    implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:${Versions.P6SPY_VERSION}")

    // Kotlin-Logging
    implementation("io.github.oshai:kotlin-logging-jvm:${Versions.KOTLIN_LOGGING_VERSION}")

    // MailJet
    implementation("com.mailjet:mailjet-client:${Versions.MAILJET_VERSION}")

    // Thymeleaf
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    // MariaDB
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:${Versions.MOCKK_VERSION}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${Versions.KOTEST_VERSION}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${Versions.KOTEST_VERSION}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// 환경별 설정 파일을 사용하기 위한 설정
val profile: String by project
val activeProfile = if (!project.hasProperty("profile") || profile.isEmpty()) "local" else profile
project.ext.set("profile", activeProfile)

sourceSets {
    main {
        resources {
            setSrcDirs(listOf("src/main/resources", "src/main/resources-$activeProfile"))
        }
    }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        filesMatching("**/application.yaml") {
            expand(project.properties)
        }
    }
}
