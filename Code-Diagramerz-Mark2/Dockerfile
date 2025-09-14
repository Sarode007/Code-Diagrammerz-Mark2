FROM maven:3.8.4-openjdk-17 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:10-jdk17
COPY --from=builder /app/target/Code-Diagramerz-Mark2.war /usr/local/tomcat/webapps/ROOT.war
ENV PORT=8080
EXPOSE ${PORT}
CMD ["catalina.sh", "run"]