/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.servlets;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.LogManager;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 04.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATContextListener implements ServletContextListener {
  
	private static final OLog log = Tracing.createLoggerFor(OLATContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    LogFactory.release(contextClassLoader);
    
    log.info("*** Destroying OLAT servlet.");
		log.info("*** Shutting down the logging system - do not use logger after this point!");
		LogManager.shutdown();
  }
}