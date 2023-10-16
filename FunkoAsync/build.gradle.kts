plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    // Lombok
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor ("org.projectlombok:lombok:1.18.30")
    // Logger
    implementation("ch.qos.logback:logback-classic:1.4.11")
    // Project Reactor
    implementation("io.projectreactor:reactor-core:3.5.10")
    // H2
    implementation("com.h2database:h2:2.2.224")
    // HikaryCP para la conexion con la base de datos
    implementation("com.zaxxer:HikariCP:5.0.1")
    // Ibatis
    implementation("org.mybatis:mybatis:3.5.13")
    // Gson
    implementation ("com.google.code.gson:gson:2.8.8")
    // Mockito
    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

tasks.test {
    useJUnitPlatform()
}