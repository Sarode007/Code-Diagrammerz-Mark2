
# working but warnings are there
FROM tomcat:10-jdk17

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*
RUN mkdir -p /usr/local/tomcat/webapps

# Copy the WAR file - using explicit path
COPY target/Code-Diagramerz-Mark2.war /usr/local/tomcat/webapps/ROOT.war

# Set environment variable for PORT
ENV PORT=8080
EXPOSE ${PORT}

# Start Tomcat
CMD ["catalina.sh", "run"]