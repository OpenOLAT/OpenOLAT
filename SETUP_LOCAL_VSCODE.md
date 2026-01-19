# OpenOLAT Local Setup Guide for VS Code

This guide walks you through running OpenOLAT locally on macOS/Linux with VS Code, Tomcat, and PostgreSQL.

## Prerequisites

Ensure these are installed and working:

```bash
# Check Java (need JDK 17+)
java -version

# Check Maven (need 3.8+)
mvn -v

# Check Git
git --version

# Check PostgreSQL (should be running)
psql --version
```

---

## Step 1: Database Setup (PostgreSQL)

Run these commands in your terminal (macOS/Linux):

```bash
# Connect to PostgreSQL and create user/database
psql -h localhost -U postgres -c "CREATE USER openolat WITH PASSWORD 'openolat';"
psql -h localhost -U postgres -c "CREATE DATABASE openolat OWNER openolat;"
psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE openolat TO openolat;"

# Import the schema (from OpenOLAT project root)
cd /Users/pohsharon/Downloads/OpenOLAT
psql -h localhost -U openolat -d openolat -f src/main/resources/database/postgresql/setupDatabase.sql
# When prompted, enter password: openolat
```

**Verify DB is ready:**
```bash
psql -h localhost -U openolat -d openolat -c "SELECT COUNT(*) FROM o_user;"
# Should return a number (e.g., 0 or more rows)
```

---

## Step 2: Configure OpenOLAT Properties

The file `src/main/java/olat.local.properties` already exists. **Verify these settings:**

```bash
# Check the file exists
cat src/main/java/olat.local.properties | grep "db\."
```

Expected output should show:
```properties
db.vendor=postgresql
db.host=localhost
db.host.port=5432
db.name=openolat
db.user=openolat
db.pass=openolat
```

If these values are missing or wrong, edit `src/main/java/olat.local.properties` manually.

Also ensure:
```properties
userdata.dir=/Users/pohsharon/OpenOLATData
server.domainname=localhost
server.port=8080
```

Create the userdata directory if it doesn't exist:
```bash
mkdir -p /Users/pohsharon/OpenOLATData
chmod 755 /Users/pohsharon/OpenOLATData
```

---

## Step 3: Set Up Tomcat

### Download and Extract Tomcat

```bash
# Download Apache Tomcat 10.1 (example path; adjust as needed)
cd /opt  # or any location where you want to keep Tomcat
curl -O https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.18/bin/apache-tomcat-10.1.18.tar.gz
tar -xzf apache-tomcat-10.1.18.tar.gz
ln -s apache-tomcat-10.1.18 tomcat  # Convenience symlink

# Or if you prefer to keep it in your user home:
mkdir -p ~/tomcat-instances
cd ~/tomcat-instances
curl -O https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.18/bin/apache-tomcat-10.1.18.tar.gz
tar -xzf apache-tomcat-10.1.18.tar.gz
```

### Configure Tomcat Memory (setenv.sh)

Copy the sample setenv.sh from the OpenOLAT repo:

```bash
# For Tomcat in /opt/tomcat:
cp /Users/pohsharon/Downloads/OpenOLAT/scripts/tomcat-setenv.sh /opt/tomcat/bin/setenv.sh
chmod +x /opt/tomcat/bin/setenv.sh

# Or for Tomcat in ~/tomcat-instances/apache-tomcat-10.1.18:
cp /Users/pohsharon/Downloads/OpenOLAT/scripts/tomcat-setenv.sh ~/tomcat-instances/apache-tomcat-10.1.18/bin/setenv.sh
chmod +x ~/tomcat-instances/apache-tomcat-10.1.18/bin/setenv.sh
```

### Edit server.xml (Optional but Recommended)

For better performance, edit Tomcat's `conf/server.xml` and add this inside the `<Host>` tag:

```xml
<Context docBase="olat" path="/olat" reloadable="false">
    <Resources allowLinking="true" cacheMaxSize="100000" cachingAllowed="true"/>
</Context>
```

**Location:** `/opt/tomcat/conf/server.xml` (or equivalent)

---

## Step 4: Build OpenOLAT (Maven)

### Quick Build (Skip Tests)

```bash
cd /Users/pohsharon/Downloads/OpenOLAT

# This builds the WAR file quickly (skips unit tests)
mvn clean package -Pcompressjs,tomcat -DskipTests
```

**Expected output:**
```
[INFO] BUILD SUCCESS
```

The WAR file will be at: `target/openolat.war`

### With Tests (Optional, Takes Longer)

```bash
mvn clean package -Pcompressjs,tomcat
# Tests will run; surefire reports tests are "skipped" by project config, so this is normal
```

---

## Step 5: Deploy to Tomcat

### Option A: Automated Deploy Script

We've provided a helper script. Run:

```bash
cd /Users/pohsharon/Downloads/OpenOLAT
bash scripts/deploy-to-tomcat.sh /opt/tomcat
# Or if Tomcat is elsewhere:
bash scripts/deploy-to-tomcat.sh ~/tomcat-instances/apache-tomcat-10.1.18
```

### Option B: Manual Deploy

```bash
# Copy the WAR to Tomcat
cp target/openolat.war /opt/tomcat/webapps/olat.war

# Or rename to ROOT.war if you want it at http://localhost:8080/ instead of /olat
# cp target/openolat.war /opt/tomcat/webapps/ROOT.war
```

---

## Step 6: Start Tomcat

```bash
# Start Tomcat in the foreground (so you see logs)
/opt/tomcat/bin/catalina.sh run

# Or start in the background
/opt/tomcat/bin/startup.sh

# Stop Tomcat
/opt/tomcat/bin/shutdown.sh
```

**Watch for startup messages:**
- Spring context loading
- Bean registration (you should see your new services being loaded)
- Database connection success

**Expected log line:**
```
[INFO] Spring context initialized successfully
```

---

## Step 7: Access OpenOLAT

Once Tomcat is running, open your browser and navigate to:

```
http://localhost:8080/olat
```

**Default Login:**
- Username: `administrator`
- Password: `olat` (or check the initial setup wizard)

---

## Troubleshooting

### OutOfMemoryException

If you see `OutOfMemoryException`:

**Already configured in setenv.sh, but if needed, increase:**

Edit `/opt/tomcat/bin/setenv.sh`:
```bash
export CATALINA_OPTS="-XX:+UseG1GC -Xms1024m -Xmx2048m -Djava.awt.headless=true -Dfile.encoding=UTF-8"
```

Then restart Tomcat.

### Database Connection Error

```
ERROR: could not connect to database at localhost:5432
```

**Fix:**
1. Verify PostgreSQL is running: `pg_isready -h localhost`
2. Verify user/password: `psql -h localhost -U openolat -d openolat -c "SELECT 1;"`
3. Check `src/main/java/olat.local.properties` for correct `db.pass`

### Port Already in Use

```
Address already in use: 8080
```

**Fix:**
- Stop any other Tomcat/app on port 8080, OR
- Edit `TOMCAT_HOME/conf/server.xml` and change the port:
  ```xml
  <Connector port="8081" protocol="HTTP/1.1" ... />
  ```

### Class Not Found / Spring Bean Error

Rebuild and redeploy:
```bash
mvn clean package -Pcompressjs,tomcat -DskipTests
cp target/openolat.war /opt/tomcat/webapps/olat.war
# Restart Tomcat
```

---

## VS Code Tips

### Run Build Task

1. Open VS Code Terminal: `Ctrl + ~`
2. Press `Ctrl + Shift + B` to run the default build task (Maven build)
3. Or select "Maven: package (no tests)" from the command palette

### Debug in Tomcat

See `.vscode/launch.json` for debug configuration (if set up). You can attach to a running Tomcat process or use Java debugging.

---

## Summary

| Step | Command |
|------|---------|
| **1. DB Setup** | `psql -h localhost -U postgres -c "CREATE USER openolat WITH PASSWORD 'openolat';"`  |
| **2. Import Schema** | `psql -h localhost -U openolat -d openolat -f src/main/resources/database/postgresql/setupDatabase.sql` |
| **3. Build** | `mvn clean package -Pcompressjs,tomcat -DskipTests` |
| **4. Deploy** | `cp target/openolat.war /opt/tomcat/webapps/olat.war` |
| **5. Start** | `/opt/tomcat/bin/catalina.sh run` |
| **6. Access** | `http://localhost:8080/olat` |

---

## Next Steps

Once OpenOLAT is running:

- **Test Auto-Grading**: Create a course → Add QTI test → Set auto-grading enabled → Complete test → Grade should auto-populate
- **Check Reporting**: Access assessment reports to see grade statistics (mean, median, stddev)
- **Peer Review**: Create peer-review assignments and verify the workflow
- **Templates**: Export/import assessment structures across courses

---

For further help, refer to the [OpenOLAT README](README.md) or the [OpenOLAT Documentation](https://www.openolat.org/en/help-menu/).
