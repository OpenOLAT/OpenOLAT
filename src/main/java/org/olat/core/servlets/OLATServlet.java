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
* <p>
*/ 

package org.olat.core.servlets;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherAction;
import org.olat.core.gui.GUIInterna;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.threadlog.RequestBasedLogLevelManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;

/**
 * Initial Date:  Apr 28, 2004
  *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public class OLATServlet extends HttpServlet {
	
	private static final long serialVersionUID = 4146352020009404834L;
	private static OLog log = Tracing.createLoggerFor(OLATServlet.class);
	private Dispatcher dispatcher;
	private SessionStatsManager sessionStatsManager;
	private RequestBasedLogLevelManager requestBasedLogLevelManager;
	
	/**
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
		if (requestBasedLogLevelManager==null) {
			log.info("init: RequestBasedLogLevelManager is not configured on this system.");
		} else {
			log.info("init: RequestBasedLogLevelManager is configured and will be used.");
		}
		if (UserBasedLogLevelManager.getInstance()==null) {
			log.info("init: UserBasedLogLevelManager is not configured on this system.");
		} else {
			log.info("init: UserBasedLogLevelManager is configured and will be used.");
		}
		
		//the servlet.init method gets called after the spring stuff and all the stuff in web.xml is done
		log.info("Framework has started, sending event to listeners of FrameworkStartupEventChannel");
		FrameworkStartupEventChannel.fireEvent();
		log.info("FrameworkStartupEvent processed by alle listeners. Webapp has started.");
		sessionStatsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
	}
	
	/**
	 * @see javax.servlet.Servlet#destroy()
	 */
	public void destroy() {
		//
	}

	/**
	 * Called when the HTTP request method is GET. This method just calls the
	 * doPost() method.
	 * 
	 * @param request
	 *          The HTTP request
	 * @param response
	 *          The HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Called when the HTTP request method is POST. This method provides the main
	 * control logic.
	 * 
	 * @param request
	 *          The HTTP request
	 * @param response
	 *          The HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		GUIInterna.begin(request);
		// initalize tracing with request, this allows debugging information as IP, User-Agent.
		Tracing.setUreq(request);
		I18nManager.attachI18nInfoToThread(request);
		ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
		//fxdiff FXOLAT-97: high CPU load tracker
		WorkThreadInformations.set("Serve request: " + request.getRequestURI());

		try{
			if(sessionStatsManager != null) sessionStatsManager.incrementRequest();
		  if (requestBasedLogLevelManager!=null) requestBasedLogLevelManager.activateRequestBasedLogLevel(request);
		  if (dispatcher == null) dispatcher = (Dispatcher) CoreSpringFactory.getBean(DispatcherAction.class);
			dispatcher.execute(request, response, null);
		} finally {
			if (requestBasedLogLevelManager!=null) requestBasedLogLevelManager.deactivateRequestBasedLogLevel();
			//fxdiff FXOLAT-97: high CPU load tracker
			WorkThreadInformations.unset();
			ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
			I18nManager.remove18nInfoFromThread();
			Tracing.setUreq(null);
			GUIInterna.end(request);
			DBFactory.getInstanceForClosing().closeSession();
		}
	}

}
