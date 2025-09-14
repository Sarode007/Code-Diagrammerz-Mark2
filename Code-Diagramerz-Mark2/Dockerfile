# Use a stable Tomcat + OpenJDK base
FROM tomcat:9.0-jdk11-openjdk

# ARG allows override at build time if needed
ARG WAR_FILE=target/*.war

# Copy the application WAR into Tomcat webapps.
# If you want the app served at root '/', rename the WAR to ROOT.war.
# We'll copy it as-is; if you want root, you can add a build step to rename.
# Replace COPY line with:
COPY ${WAR_FILE} /usr/local/tomcat/webapps/ROOT.war


# Copy the entrypoint script that will patch server.xml at container start
COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Keep an original backup of server.xml so the entrypoint can work from it
RUN cp /usr/local/tomcat/conf/server.xml /usr/local/tomcat/conf/server.xml.orig

# Default PORT fallback (Render will supply its own PORT env)
ENV PORT=8080

# Expose the default Tomcat port (not strictly required, but useful)
EXPOSE 8080

# Entrypoint will update ports and start Tomcat
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
