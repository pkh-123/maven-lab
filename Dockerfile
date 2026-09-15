# Jenkins가 이 이미지를 빌드해 Ubuntu VM에 배포하는 실습

FROM eclipse-temurin:21-jre-noble

WORKDIR /app

COPY target/calculator-0.0.1-SNAPSHOT.war app.war

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.war"]