FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

COPY . .
# Grant execute permission for gradlew
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/libs
# Copy the built jar file. We assume the jar is created in build/libs. 
# We might need to adjust if the version naming is strict, but usually 'app.jar' or '*.jar' works with wildcard 
# or we just copy the specific one. Let's list and copy.
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
