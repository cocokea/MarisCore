plugins { java }

group = "dev.mariscore"
version = "1.0"

java { toolchain { languageVersion.set(JavaLanguageVersion.of(25)) } }

tasks.withType<JavaCompile> { options.encoding = "UTF-8"; options.release.set(25) }

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
    compileOnly("com.zaxxer:HikariCP:7.0.2")
    compileOnly("com.mysql:mysql-connector-j:9.7.0")
    compileOnly("org.xerial:sqlite-jdbc:3.53.0.0")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.processResources { filteringCharset = "UTF-8"; filesMatching("plugin.yml") { expand("version" to project.version) } }

tasks.jar {
    archiveBaseName.set("MarisCore")
    archiveVersion.set("")
}

