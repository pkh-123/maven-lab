FROM eclipse-temurin:21-jre-noble

WORKDIR /app

COPY target/calculator-0.0.1-SNAPSHOT.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]