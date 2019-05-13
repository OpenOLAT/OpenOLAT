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

import javax.servlet.http.HttpServletRequest;

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
 * This is the central place where all log information should pass.
 * <p>
 * It acts as a facade to the <code>log4j.Logger</code>. Each piece of code,
 * interested to log some error, warning, info, debug message has to do so with
 * the help of the resepective <code>logXYZ(..)</code> method found here.
 * <p>
 * Using the Tracing class helps to ensure all log messages are built in the
 * same format, and also that they contain all important information like:
 * <ul>
 * <li>identity</li>
 * <li>remoteIp</li>
 * <li>userAgent</li>
 * <li>referer</li>
 * </ul>
 * The drawback so far is, that the start up information is flooded with
 * "useless" <code>n/a</code> as all code logging outside of a user request
 * does not contain the former listed information.<br>
 * However, the positive effect of having an easy to use tracing facility from
 * within the code - one line is enough to leave a trace - , and also the fact
 * that each trace is enriched with the user sessions fingerprint, justifies the
 * former described drawback.
 * <p>
 * Implementation note:<br>
 * <ul>
 * <li>The user session fingerprint is initialized by calling the
 * <code>setUreq(...)</code> method.</li>
 * <li>The session fingerprint consists of the identity, remoteIp, userAgent,
 * referer</li>
 * <li>This information is stored in a <code>ThreadLocal</code> and can thus
 * be accessed in a static way.</li>
 * </ul>
 * 
 * @author Felix Jost
 */
public class Tracing {
	private static final String REFERS_TO = " -> ";
	private static final String DOUBLEPOINT = ": ";
	private static final String CAUSE = ".cause::";
	private static final String STACK_OF = ">>>stack of ";
	private static final String CAUSE_N_A = "cause:n/a";
	private static final String N_A = "n/a";

	// main categories for log
	protected static final String PREFIX = "OLAT::";
	protected static final String AUDIT = "AUDIT";
	protected static final String ERROR = "ERROR";
	protected static final String WARN  = "WARN";
	protected static final String INFO  = "INFO";
	protected static final String DEBUG = "DEBUG";
	protected static final String PERFORMANCE = "PERF";

	private static final int stacklen = 11;
	private static final String SEPARATOR = " ^%^ ";
	private static long auditRefNum = 0;
	private static long errorRefNum = 0;
	private static long warnRefNum  = 0;
	private static long infoRefNum  = 0;
	private static long debugRefNum = 0;
	
	
	public static final Marker M_AUDIT = MarkerManager.getMarker(AUDIT);

	/**
	 * per-thread singleton holding the actual HttpServletRequest which is the
	 * starting point for various information like the UserSession, Identity,
	 * User-Agent, IP etc.
	 * 
	 * FIXME:pb: tld data should extract data from ureq and save this data instead of ureq
	 * and as a next step ThreadLocalData should become InheritedThreadLocalData, that
	 * subthreads created from the users click-thread also have the parents ureq data.
	 */
	private static ThreadLocalData tld = new ThreadLocalData();
	private static String nodeId = "";
	
	
	private Tracing(String nodeId) {
		Tracing.nodeId = nodeId;
	}

	/**
	 * Factory method to create a logger object for the given class. For a certain
	 * class always the same logger is returned (shared)
	 * 
	 * @param loggingClass
	 * @return
	 */
	public static Logger createLoggerFor(Class<?> loggingClass) {
		/*
		// Share logger object to reduce memory footprint
		OLog logger = loggerLookupMap.get(loggingClass);
		if (logger == null) {
			OLog newLogger = new OLogImpl(loggingClass);
			logger = loggerLookupMap.putIfAbsent(loggingClass, newLogger);
			if(logger == null) {
				logger = newLogger;
			}
		}
		*/
		return  LogManager.getLogger(loggingClass.getName());
	}
	
	
	/**
	 * @return long the number of errors since last reboot. 
	 */
	public static long getErrorCount() {
		return errorRefNum;
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
		tld.setHttpServletRequest(httpRequest);
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
		Identity identity = usess.getIdentity();
		ThreadContext.put("identity", identity == null ? N_A : identity.getKey().toString());
	}
	
	public static void setUuid(String uuid) {
		ThreadContext.put("ref", uuid == null ? N_A : uuid);
	}
	
	public static void clearHttpRequest() {
		tld.setHttpServletRequest(null);
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

	/**
	 * Description:<br>
	 * ThreadLocalData implements the per-thread Singleton to store the
	 * HttpServletRequest valid for the click processed. On one hand it is then
	 * more convenient to use the logXyz methods, as one has not to specify the
	 * Identity as before, on the other hand it gives the possibility having the
	 * identity included in logXyz messages where an Identity is not available.
	 * Moreover the HttpServletRequest contains also the remote ip address and
	 * user agent. Both information are valuable for log file analyzing purposes.
	 * <P>
	 * Initial Date: Oct 21, 2005 <br>
	 * 
	 * @author patrick
	 */
	private static class ThreadLocalData extends ThreadLocal<HttpServletRequest> {

		@Override
		public HttpServletRequest initialValue() {
			return null;
		}

		/**
		 * @param ureq
		 */
		public void setHttpServletRequest(HttpServletRequest ureq) {
			super.set(ureq);
		}

		/**
		 * @return
		 */
		public HttpServletRequest getHttpServletRequest() {
			return super.get();
		}
	}
}