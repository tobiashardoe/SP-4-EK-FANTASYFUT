FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S fantasy && adduser -S fantasy -G fantasy && mkdir /data && chown fantasy:fantasy /data
WORKDIR /app
COPY --chown=fantasy:fantasy target/fantasyfut-web.jar /app/app.jar
USER fantasy
EXPOSE 7080
ENTRYPOINT ["java", "-Xms24m", "-Xmx64m", "-XX:MaxMetaspaceSize=64m", "-XX:ReservedCodeCacheSize=24m", "-Xss512k", "-jar", "/app/app.jar"]
