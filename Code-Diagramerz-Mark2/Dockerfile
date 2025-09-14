FROM tomcat:10-jdk17

# Copy the WAR file directly to webapps
COPY target/Code-Diagramerz-Mark2.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080
CMD ["catalina.sh", "run"]