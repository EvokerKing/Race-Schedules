# Stage 1: Cache Gradle dependencies
FROM eclipse-temurin:21-jdk-jammy AS cache
COPY --from=gradle:8.11-jdk21 /opt/gradle /opt/gradle
ENV PATH="/opt/gradle/bin:${PATH}"
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME=/home/gradle/cache_home
COPY build.gradle.* gradle.properties /home/gradle/app/
COPY gradle /home/gradle/app/gradle
WORKDIR /home/gradle/app
RUN gradle dependencies

# Stage 2: Build Application
FROM gradle:8.11-jdk21 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Build the fat JAR, Gradle also supports shadow
# and boot JAR by default.
RUN gradle buildFatJar

# Stage 3: Create the Runtime Image
FROM eclipse-temurin:21-jre-jammy AS runtime
EXPOSE 80
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/ktor-docker-sample.jar
ENTRYPOINT ["java","-jar","/app/ktor-docker-sample.jar"]
