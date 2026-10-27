FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar

LABEL authors="Max_Limur"

ENTRYPOINT ["java", "-jar", "app.jar"]