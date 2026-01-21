# ETAPA 1: Construcción (Compilar el código)
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
# Damos permisos de ejecución al wrapper de Gradle
RUN chmod +x gradlew
# Compilamos saltando los tests (ya los corriste en local)
RUN ./gradlew clean build -x test --no-daemon

# ETAPA 2: Ejecución (Imagen ligera para producción)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Creamos la carpeta donde montaremos el volumen de Railway
RUN mkdir -p /app/data

# Copiamos el JAR compilado desde la etapa anterior
# NOTA: El asterisco busca cualquier jar generado
COPY --from=build /app/build/libs/*.jar app.jar

# Variables de entorno por defecto
ENV PORT=8080
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]