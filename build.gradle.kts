import com.epages.restdocs.apispec.gradle.OpenApi3Extension
import org.hidetake.gradle.swagger.generator.GenerateSwaggerUI
import org.springframework.boot.gradle.plugin.ResolveMainClassName
import org.springframework.boot.gradle.tasks.bundling.BootJar

object Versions {
    const val JASYPT_VERSION = "3.0.5"
    const val P6SPY_VERSION = "1.9.2"
    const val KOTLIN_LOGGING_VERSION = "7.0.0"
    const val MOCKK_VERSION = "1.13.12"
    const val KOTEST_VERSION = "5.9.1"
    const val MAILJET_VERSION = "5.2.5"
    const val JWT_VERSION = "0.12.6"
    const val SPRINGDOC_VERSION = "2.7.0"
    const val SPRING_RESTDOCS_VERSION = "3.0.3"
    const val RESTDOCS_API_SPEC_MOCKMVC_VERSION = "0.19.4"
    const val SWAGGER_UI_VERSION = "5.18.2"
    const val KOTEST_EXTENSIONS_SPRING_VERSION = "1.3.0"
    const val SPRING_MOCKK_VERSION = "4.0.2"
    const val FIXTURE_MONKEY_VERSION = "1.1.5"
    const val AMAZON_VERSION = "2.2.6.RELEASE"
}

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("plugin.allopen") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.epages.restdocs-api-spec") version "0.19.2" // Rest Docs 플러그인 추가
    id("org.hidetake.swagger.generator") version "2.18.2" // SwaggerUI 플러그인 추가
    id("org.asciidoctor.jvm.convert") version "3.3.2"
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

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    implementation("com.querydsl:querydsl-apt:5.1.0:jakarta")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("jakarta.annotation:jakarta.annotation-api")
    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

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

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:${Versions.JWT_VERSION}")
    implementation("io.jsonwebtoken:jjwt-impl:${Versions.JWT_VERSION}")
    implementation("io.jsonwebtoken:jjwt-jackson:${Versions.JWT_VERSION}")

    // Spring OAuth2
    implementation("org.springframework.security:spring-security-oauth2-client")

    // Rest Docs
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${Versions.SPRINGDOC_VERSION}")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc:${Versions.SPRING_RESTDOCS_VERSION}")
    implementation("com.epages:restdocs-api-spec-mockmvc:${Versions.RESTDOCS_API_SPEC_MOCKMVC_VERSION}")
    implementation("org.webjars:swagger-ui:${Versions.SWAGGER_UI_VERSION}")
    swaggerUI("org.webjars:swagger-ui:${Versions.SWAGGER_UI_VERSION}")

    // MariaDB
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client")

    // AWS
    implementation("org.springframework.cloud:spring-cloud-starter-aws:${Versions.AMAZON_VERSION}")

    // Test
    implementation("io.kotest.extensions:kotest-extensions-spring:${Versions.KOTEST_EXTENSIONS_SPRING_VERSION}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.mockk:mockk:${Versions.MOCKK_VERSION}")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${Versions.KOTEST_VERSION}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${Versions.KOTEST_VERSION}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("com.ninja-squad:springmockk:${Versions.SPRING_MOCKK_VERSION}")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:${Versions.FIXTURE_MONKEY_VERSION}")
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
    systemProperty("jasypt.encryptor.password", System.getProperty("jasypt.encryptor.password"))
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

tasks.named<Jar>("jar") {
    enabled = false
}

// 생성된 API 스펙이 위치할 디렉토리 설정
swaggerSources {
    create("sample") {
        setInputFile(file(layout.buildDirectory.dir("api-spec/openapi3.yaml").get().asFile))
    }
}

// OpenAPI 3.0 설정
configure<OpenApi3Extension> {
    setServer("http://localhost:8080")
    title = "Dulian API"
    description = "Dulian API"
    version = "1.0.0"
    format = "yaml"
}

// Swagger UI 설정
tasks.withType<GenerateSwaggerUI>().configureEach {
    dependsOn("openapi3")
}

// Swagger UI 복사 작업 설정
tasks.register<Copy>("copySwaggerUI") {
    description = "Copy Swagger UI to resources"
    group = "documentation"

    dependsOn("generateSwaggerUISample")

    // 복사할 소스 파일 설정
    from(layout.buildDirectory.dir("api-spec/openapi3.yaml").get().asFile)

    // 대상 디렉토리 설정
    into(layout.buildDirectory.dir("resources/main/static/docs").get().asFile)
}

// BootJar 작업에 Swagger UI 복사 작업 추가
tasks.named<BootJar>("bootJar") {
    dependsOn("copySwaggerUI")
}

// ResolveMainClassName 작업에 Swagger UI 복사 작업 추가
tasks.named<ResolveMainClassName>("resolveMainClassName") {
    dependsOn("copySwaggerUI")
}
