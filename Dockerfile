FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY . .
RUN javac AgeWiseServer.java
EXPOSE 8080
CMD ["java", "AgeWiseServer"]
