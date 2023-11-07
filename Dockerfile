# Use an Ubuntu-based Tomcat image with JDK 17
FROM tomcat:jdk17-openjdk

# Update package repository and install necessary packages.
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && apt-get clean

# Create an OpenOLAT user
RUN useradd -m -s /bin/bash openolat
WORKDIR /home/openolat

# Create a directory for downloads and copy OpenOLAT war file
RUN mkdir /home/downloads    
COPY target/openolat-lms-18.2-SNAPSHOT.war /home/downloads/

# Copy web.xml to Tomcat's configuration directory
COPY web.xml /usr/local/tomcat/conf/web.xml

# Unzip OpenOLAT war file and create a symbolic link
RUN unzip -d /home/openolat-18.2 /home/downloads/openolat-lms-18.2-SNAPSHOT.war 
RUN ln -s /home/openolat-18.2 /home/openolat/webapp

# Create Tomcat directories and symbolic links
RUN mkdir -p bin conf lib run logs
COPY server.xml /home/openolat/conf/
COPY setenv.sh /home/openolat/bin/
RUN ln -s /usr/local/tomcat/conf/web.xml /home/openolat/conf/web.xml
RUN ln -s /usr/local/tomcat/bin/catalina.sh /home/openolat/bin/catalina.sh

# Create symbolic links for startup and shutdown scripts
RUN ln -s /usr/local/tomcat/bin/startup.sh /home/start && \
    ln -s /usr/local/tomcat/bin/shutdown.sh /home/stop

# Create OpenOLAT configuration files
RUN mkdir -p conf/Catalina/localhost/
COPY olat.local.properties lib/
COPY ROOT.xml conf/Catalina/localhost/ 
COPY log4j2.xml lib/

# Copy files to Tomcat container
COPY setenv.sh /usr/local/tomcat/bin/
RUN chmod +x /usr/local/tomcat/bin/setenv.sh

# Copy server.xml and web.xml to replace the default configuration
COPY server.xml /usr/local/tomcat/conf/
COPY web.xml /usr/local/tomcat/conf/

# Copy properties file, log4j2.xml, and ROOT.xml
COPY olat.local.properties /usr/local/tomcat/lib
COPY log4j2.xml /usr/local/tomcat/lib/
COPY ROOT.xml /usr/local/tomcat/conf/Catalina/localhost/ROOT.xml

# Set permissions for OpenOLAT user and directories
RUN chown openolat /home/openolat/logs \
   && chmod g+rwX /usr/local/tomcat/lib/olat.local.properties
RUN chown openolat /usr/local/tomcat/lib/olat.local.properties \
  && chmod g+rwX /usr/local/tomcat/lib/olat.local.properties  

# Set permissions for Tomcat directories
RUN chown -R openolat /usr/local/tomcat \
  && chmod -R g+rwX /usr/local/tomcat

# Set environment variables
ENV JAVA_HOME=/usr/local/openjdk-17
ENV CATALINA_HOME=/usr/local/tomcat
ENV CATALINA_BASE=/home/openolat

# Switch to the OpenOLAT user
USER openolat

# Start Tomcat with the specified command
CMD ["catalina.sh", "run"]
