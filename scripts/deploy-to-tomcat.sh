#!/bin/bash
# Deploy OpenOLAT WAR to Tomcat
# Usage: bash scripts/deploy-to-tomcat.sh /path/to/tomcat

if [ -z "$1" ]; then
  echo "Usage: bash scripts/deploy-to-tomcat.sh /path/to/tomcat"
  echo "Example: bash scripts/deploy-to-tomcat.sh /opt/tomcat"
  exit 1
fi

TOMCAT_HOME="$1"

if [ ! -d "$TOMCAT_HOME" ]; then
  echo "ERROR: Tomcat directory not found: $TOMCAT_HOME"
  exit 1
fi

if [ ! -f "target/openolat.war" ]; then
  echo "ERROR: WAR file not found. Run 'mvn clean package -Pcompressjs,tomcat -DskipTests' first."
  exit 1
fi

echo "Deploying OpenOLAT to $TOMCAT_HOME..."

# Stop Tomcat if running
if [ -f "$TOMCAT_HOME/bin/shutdown.sh" ]; then
  echo "Stopping Tomcat..."
  $TOMCAT_HOME/bin/shutdown.sh > /dev/null 2>&1
  sleep 3
fi

# Remove old deployment
rm -f "$TOMCAT_HOME/webapps/olat.war"
rm -rf "$TOMCAT_HOME/webapps/olat"

# Deploy new WAR
cp target/openolat.war "$TOMCAT_HOME/webapps/olat.war"

echo "✓ Deployment complete!"
echo ""
echo "Next steps:"
echo "  1. Start Tomcat: $TOMCAT_HOME/bin/catalina.sh run"
echo "  2. Open browser: http://localhost:8080/olat"
echo "  3. Login: administrator / olat"
