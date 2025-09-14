FROM tomcat:10-jdk17

# Clean and prepare Tomcat webapps directory
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file directly to webapps
COPY target/Code-Diagramerz-Mark2.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]