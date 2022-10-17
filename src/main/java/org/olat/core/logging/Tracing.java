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

package org.olat.core.logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.spi.LoggerContext;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;

/**
 * There is helper methods to save save in the thread context of
 * Log4J 2 the following informations
 * <ul>
 * <li>identity</li>
 * <li>ip</li>
 * <li>userAgent</li>
 * <li>referer</li>
 * </ul>
 * <ul>
 * <li>The user session fingerprint is initialized by calling the
 * <code>setUreq(...)</code> method.</li>
 * <li>The session fingerprint consists of the identity, ip, userAgent, referer</li>
 * <li>This information is stored in a <code>ThreadContext</code> and can thus
 * be accessed in a static way.</li>
 * </ul>
 * 
 * @author Felix Jost
 */
public class Tracing {

	private static final String N_A = "n/a";
	private static final String AUDIT = "AUDIT";

	public static final Marker M_AUDIT = MarkerManager.getMarker(AUDIT);

	private Tracing() {
		//
	}

	/**
	 * Factory method to create a logger object for the given class. For a certain
	 * class always the same logger is returned (shared)
	 * 
	 * @param loggingClass
	 * @return
	 */
	public static Logger createLoggerFor(Class<?> loggingClass) {
		return  LogManager.getLogger(loggingClass.getName());
	}

	/**
	 * sets the HttpServletRequest for the actual click/user request. This method
	 * should only be called once per thread(/servlet invocation) and also be the
	 * first method call in the
	 * <code>void service(HttpServletRequest req, HttpServletResponse resp)</code>
	 * or the respective
	 * <code>void doXYZ(HttpServletRequest request, HttpServletResponse response)</code>
	 * methods.<br>
	 * This method accesses the thread local data store.
	 * 
	 * @param ureq
	 */
	public static void setHttpRequest(HttpServletRequest httpRequest) {
		if(httpRequest == null) {
			ThreadContext.clearAll();
		} else {
			String remoteIp = httpRequest.getRemoteAddr();
			String userAgent = httpRequest.getHeader("User-Agent");
			String referer = httpRequest.getHeader("Referer");
			ThreadContext.put("ip", remoteIp == null ? N_A : remoteIp);
			ThreadContext.put("userAgent", userAgent == null ? N_A : userAgent);
			ThreadContext.put("referer", referer == null ? N_A : referer);
			ThreadContext.put("nodeId", Integer.toString(WebappHelper.getNodeId()));
		}
	}
	
	public static void setUserSession(UserSession usess) {
		if(usess != null) {
			setIdentity(usess.getIdentity());
		}
	}
	
	public static void setIdentity(Identity identity) {
		ThreadContext.put("identityKey", identity == null ? N_A : identity.getKey().toString());
	}
	
	public static void setUuid(String uuid) {
		ThreadContext.put("ref", uuid == null ? N_A : uuid);
	}
	
	public static void clearHttpRequest() {
		ThreadContext.clearAll();
	}

	/**
	 * @return list all current loggers
	 */
	public static List<Logger> getLoggers() {
		LoggerContext cxt = LogManager.getContext(Tracing.class.getClassLoader(), false);
		if(cxt instanceof org.apache.logging.log4j.core.LoggerContext) {
			Collection<org.apache.logging.log4j.core.Logger> loggers = ((org.apache.logging.log4j.core.LoggerContext)cxt).getLoggers();
			return new ArrayList<>(loggers);
		}
		return Collections.emptyList();
	}

	/**
	 * set provided log level for all active loggers.
	 * 
	 * @param logLevel
	 */
	public static boolean resetLevelForAllLoggers() {
		LoggerContext cxt = LogManager.getContext(Tracing.class.getClassLoader(), false);
		if(cxt instanceof org.apache.logging.log4j.core.LoggerContext) {
			((org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false)).reconfigure();
			return true;
		}
		return false;
	}

	/**
	 * set log level of specified logger
	 * 
	 * @param logLevel
	 * @param name
	 */
	public static void setLevelForLogger(Level logLevel, String name) {
		Logger logger = LogManager.getLogger(name);
		if (logger != null) {
			Configurator.setLevel(name, logLevel);
		}
	}

	/**
	 * generates active loggers list sorted by name.
	 * 
	 * @return
	 */
	public static List<Logger> getLoggersSortedByName() {
		List<Logger> loggers = getLoggers();
		Collections.sort(loggers, (a, b) ->  a.getName().compareTo(b.getName()));
		return loggers;
	}
}