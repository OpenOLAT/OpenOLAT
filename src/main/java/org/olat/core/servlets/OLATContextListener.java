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