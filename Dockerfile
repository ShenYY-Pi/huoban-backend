FROM maven:3.5-jdk-8-alpine as builder

#cope local code to the container image
WORKDIR /app
COPY pom.xml .
COPY src ./src

#build a release artifact
RUN mvn package -DskipTests

#run the web service on container startup
CMD ["java","-jar","/app/target/huoban-backend-0.0.1-SNAPSHOT.jar","--spring.profiles.active=prod"]