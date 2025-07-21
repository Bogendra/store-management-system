FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven configuration
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Build dependencies separately (for better caching)
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copy and build source code
COPY src src
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Run stage
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency

# Copy application
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Run the application
ENTRYPOINT ["java","-cp","app:app/lib/*","com.store.inventory.InventoryServiceApplication"]
