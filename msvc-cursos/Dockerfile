# STAGE 1
FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR application/msvc-cursos
COPY ./msvc-cursos/mvnw .
COPY ./msvc-cursos/.mvn .mvn
COPY pom.xml ..
COPY ./msvc-cursos/pom.xml .
# Resolución de las dependencias del proyecto
RUN ./mvnw dependency:go-offline
# Nota: Tenemos que copiar el código después de la resolución de las dependencias del proyecto porque, si lo hacemos
# antes, por cada cambio que haya en el código, disparará de forma automática la resolución de dependencias otra vez
# sin tener en cuenta la caché de capas de Docker
COPY ./msvc-cursos/src src
RUN ./mvnw clean package -DskipTests && \
    java -Djarmode=layertools -jar target/msvc-cursos-*-SNAPSHOT.jar extract

# STAGE 2
FROM eclipse-temurin:17-jre-alpine
WORKDIR application
EXPOSE 8002
COPY --from=builder application/msvc-cursos/dependencies/ ./
COPY --from=builder application/msvc-cursos/spring-boot-loader/ ./
COPY --from=builder application/msvc-cursos/snapshot-dependencies/ ./
COPY --from=builder application/msvc-cursos/application/ ./
# Nota: Como hemos indicado un directorio de trabajo con la instrucción "WORKDIR", este comando se ejecutará
# por defecto dentro de ese directorio de trabajo
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]