# Variable para todos los stages
ARG MSVC_NAME=msvc-gateway
# STAGE 1
FROM eclipse-temurin:17-jdk-alpine as builder
# No hace falta indicar de nuevo el valor porque estamos reutilizando la variable MSVC_NAME definida previamente
ARG MSVC_NAME
WORKDIR application/$MSVC_NAME
COPY ./$MSVC_NAME/mvnw .
COPY ./$MSVC_NAME/.mvn .mvn
COPY pom.xml ..
COPY ./$MSVC_NAME/pom.xml .
# Resolución de las dependencias del proyecto
RUN ./mvnw dependency:go-offline
# Nota: Tenemos que copiar el código después de la resolución de las dependencias del proyecto porque, si lo hacemos
# antes, por cada cambio que haya en el código, disparará de forma automática la resolución de dependencias otra vez
# sin tener en cuenta la caché de capas de Docker
COPY ./$MSVC_NAME/src src
RUN ./mvnw clean package -DskipTests && \
    java -Djarmode=layertools -jar target/$MSVC_NAME-*-SNAPSHOT.jar extract

# STAGE 2
FROM eclipse-temurin:17-jre-alpine
WORKDIR application
# No hace falta indicar de nuevo el valor porque estamos reutilizando la variable MSVC_NAME definida previamente
ARG MSVC_NAME
ARG EXTRACT_FOLDER=application/$MSVC_NAME
COPY --from=builder $EXTRACT_FOLDER/dependencies/ ./
COPY --from=builder $EXTRACT_FOLDER/spring-boot-loader/ ./
COPY --from=builder $EXTRACT_FOLDER/snapshot-dependencies/ ./
COPY --from=builder $EXTRACT_FOLDER/application/ ./
EXPOSE 8090
# Nota: Como hemos indicado un directorio de trabajo con la instrucción "WORKDIR", este comando se ejecutará
# por defecto dentro de ese directorio de trabajo
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]