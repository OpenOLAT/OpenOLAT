/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.servlets;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.springframework.core.io.ClassPathResource;

/**
 * Description:<br>
 * special servlet for log4j initialization
 * This servlet gets only called if the log4j system is not yet configured by
 * either finding a log4j.xml file on the root of the classpath or by setting your custom
 * file with -Dlog4j.configuration="file:/tmp/mylog4j.xml"
 * 
 * <P>
 * Initial Date:  20.01.2011 <br>
 * @author guido
 */
public class Log4JInitServlet extends HttpServlet {

	private static final long serialVersionUID = -1969924962369679573L;
	
	@Override
	public void init() {
		String file = getInitParameter("log4j-init-file");
		ClassPathResource res = new ClassPathResource(file);
		if (!res.exists()) {
			//creating basic log4j configuration which writes to console out, Only called when not yet configured
			ConsoleAppender appender = new ConsoleAppender(
					new PatternLayout("%d{ABSOLUTE} %5p %c{1}:%L - %m%n"), ConsoleAppender.SYSTEM_OUT);
			appender.setThreshold(Level.INFO);
			BasicConfigurator.configure(appender);
			
			OLog log = Tracing.createLoggerFor(getClass());
			log.info("*****************************************************************************************");
			log.info("You don't provide a log4j config file for your OLAT instance. OLAT will just log to standard out (e.g. catalina.out)." +
					" Please provide a proper log config file (log4j.xml, see olat/conf for an example or read the installation guide) " +
					"and place it into the root of the classpath e.g. tomcat/lib or WEB-INF/classes");
			log.info("*****************************************************************************************");
		}
	}

}
