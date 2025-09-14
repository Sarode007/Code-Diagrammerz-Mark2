#!/bin/sh
set -eu

# If PORT is not set, default to 8080
: "${PORT:=8080}"

CONF_DIR="/usr/local/tomcat/conf"
ORIG_CONF="${CONF_DIR}/server.xml.orig"
TARGET_CONF="${CONF_DIR}/server.xml"

# Ensure original exists (created during build)
if [ ! -f "${ORIG_CONF}" ]; then
  echo "ERROR: ${ORIG_CONF} not found!"
  exit 1
fi

# Create a working copy to modify
cp "${ORIG_CONF}" "${TARGET_CONF}.tmp"

# 1) Replace any Connector port="NNNN" with the runtime PORT
# 2) Disable the Server shutdown listener by forcing Server port="-1"
# Use perl with single invocation to avoid quoting edge cases
perl -0777 -pe '
  s/port="\d+"/port="'$PORT'"/g;
  s/<Server\s+port="-?\d+"/<Server port="-1"/g;
' "${TARGET_CONF}.tmp" > "${TARGET_CONF}"

# Clean up temp file
rm -f "${TARGET_CONF}.tmp"

echo "Starting Tomcat: binding connectors to port ${PORT}, shutdown port disabled."

# Start Tomcat foreground
exec catalina.sh run
