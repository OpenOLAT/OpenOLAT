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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.session.UserSessionManager;

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
	private static long __auditRefNum__ = 0;
	private static long __errorRefNum__ = 0;
	private static long __warnRefNum__  = 0;
	private static long __infoRefNum__  = 0;
	private static long __debugRefNum__ = 0;
	
	// VM local cache to have one logger object per class
	private static final ConcurrentMap<Class<?>, OLog> loggerLookupMap = new ConcurrentHashMap<>();

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
	public static OLog createLoggerFor(Class<?> loggingClass) {
		// Share logger object to reduce memory footprint
		OLog logger = loggerLookupMap.get(loggingClass);
		if (logger == null) {
			OLog newLogger = new OLogImpl(loggingClass);
			logger = loggerLookupMap.putIfAbsent(loggingClass, newLogger);
			if(logger == null) {
				logger = newLogger;
			}
		}
		return logger;
	}
	
	
	/**
	 * @return long the number of errors since last reboot. 
	 */
	public static long getErrorCount() {
		return __errorRefNum__;
	}

	/**
	 * Add error log entry. See package.html for propper usage!
	 * 
	 * @param category
	 * @param logMsg
	 * @param cause
	 * @return Log entry identifier.
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	public static long logError(String logMsg, Throwable cause, Class<?> callingClass) {
		long refNum = getErrorRefNum();
		getLogger(callingClass).error(assembleThrowableMessage(ERROR, 'E',refNum, callingClass, logMsg, cause));
		return refNum;
	}
	
	public static long logError(String logMsg, Throwable cause, Logger logger, Class<?> callingClass) {
		long refNum = getErrorRefNum();
		logger.error(assembleThrowableMessage(ERROR, 'E',refNum, callingClass, logMsg, cause));
		return refNum;
	}

	/**
	 * @param callingClass
	 * @param logMsg
	 * @return
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	protected static long logError(String logMsg, Class<?> callingClass) {
		return logError(logMsg, null, callingClass);
	}

	/**
	 * See package.html for propper usage!
	 * @param callingClass
	 * @param logMsg
	 * @param cause
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 * @return
	 */
	protected static long logWarn(String logMsg, Throwable cause, Class<?> callingClass) {
		long refNum = getWarnRefNum();
		getLogger(callingClass).warn(assembleThrowableMessage(WARN, 'W', refNum, callingClass, logMsg, cause));
		return refNum;
	}
	
	protected static long logWarn(String logMsg, Throwable cause, Logger logger, Class<?> callingClass) {
		long refNum = getWarnRefNum();
		logger.warn(assembleThrowableMessage(WARN, 'W', refNum, callingClass, logMsg, cause));
		return refNum;
	}

	/**
	 * @param callingClass
	 * @param logMsg
	 * @return
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	protected static long logWarn(String logMsg, Class<?> callingClass) {
		return logWarn(logMsg, null, callingClass);
	}

	/**
	 * Add debug log entry. Alwasy use together with
	 * if(Tracing.isDebugEnabled()) Tracing.logDebug(...) to let the compiler
	 * optimize it for a performance gain
	 * 
	 * @param callingClass
	 * @param userObj
	 * @param logMsg
	 * @return
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	protected static long logDebug(String logMsg, String userObj, Class<?> callingClass) {
		long refNum = getDebugRefNum();
		if (isDebugEnabled(callingClass)) {
			getLogger(callingClass).debug(assembleMsg(DEBUG, 'D', refNum, callingClass, userObj, logMsg));
		}
		return refNum;
	}
	
	protected static long logDebug(String logMsg, String userObj, Logger logger, Class<?> callingClass) {
		long refNum = getDebugRefNum();
		if (logger.isDebugEnabled()) {
			logger.debug(assembleMsg(DEBUG, 'D', refNum, callingClass, userObj, logMsg));
		}
		return refNum;
	}

	/**
	 * Add debug log entry
	 * 
	 * @param callingClass
	 * @param logMsg
	 * @return
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	protected static long logDebug(String logMsg, Class<?> callingClass) {
		return logDebug(logMsg, null, callingClass);
	}

	/**
	 * @param callingClass
	 * @param logMsg
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 * @return
	 */
	protected static long logInfo(String logMsg, String userObject, Class<?> callingClass) {
		long refNum = getInfoRefNum();
		getLogger(callingClass).info(assembleMsg(INFO, 'I', refNum, callingClass, userObject, logMsg));
		return refNum;
	}
	
	protected static long logInfo(String logMsg, String userObject, Logger logger, Class<?> callingClass) {
		long refNum = getInfoRefNum();
		logger.info(assembleMsg(INFO, 'I', refNum, callingClass, userObject, logMsg));
		return refNum;
	}

	/**
	 * @param callingClass
	 * @param logMsg
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 * @return
	 */
	protected static long logInfo(String logMsg, Class<?> callingClass) {
		return logInfo(logMsg, null, callingClass);

	}

	/**
	 * Add audit log entry.
	 * 
	 * @param callingClass
	 * @param logMsg
	 * @return Log entry identifier.
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 */
	protected static long logAudit(String logMsg, Class<?> callingClass) {
		return logAudit(logMsg, null, callingClass);
	}

	/**
	 * Add audit log entry with a user object.
	 * 
	 * @param callingClass
	 * @param userObj
	 * @param logMsg
	 * @deprecated please use OLog log = Tracing.createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 * @return Log entry identifier.
	 */
	protected static long logAudit(String logMsg, String userObj, Class<?> callingClass) {
		long refNum = getAuditRefNum();
		getLogger(callingClass).info(assembleMsg(AUDIT, 'A', refNum, callingClass, userObj, logMsg));
		return refNum;
	}
	
	protected static long logAudit(String logMsg, String userObj, Logger logger, Class<?> callingClass) {
		long refNum = getAuditRefNum();
		logger.info(assembleMsg(AUDIT, 'A', refNum, callingClass, userObj, logMsg));
		return refNum;
	}

	/**
	 * Method getStackTrace returns the first few (stacklen) lines of the
	 * stacktrace
	 * 
	 * @param cause
	 * @return String
	 */
	private static StringBuilder getStackTrace(Throwable cause) {
		StackTraceElement[] st = cause.getStackTrace();
		StringBuilder stackTrace = new StringBuilder(500);
		int max = (st.length < stacklen ? st.length : stacklen);
		for (int i = 0; i < max; i++) {
			stackTrace.append(" at ");
			stackTrace.append(st[i]);
		}
		return stackTrace;
	}

	private synchronized static long getAuditRefNum() { //o_clusterOK by:fj
		return ++__auditRefNum__;
	}

	private synchronized static long getErrorRefNum() { //o_clusterOK by:fj
		return ++__errorRefNum__;
	}
	
	private static long getWarnRefNum() {
		return ++__warnRefNum__;
	}

	private synchronized static long getDebugRefNum() { //o_clusterOK by:fj
		return ++__debugRefNum__;
	}

	private synchronized static long getInfoRefNum() { //o_clusterOK by:fj
		return ++__infoRefNum__;
	}

	private static String assembleMsg(String category, char prefix, long refNum, Class<?> callingClass, String userObj, String logMsg) {

		HttpServletRequest ureq = null;
		if(tld != null){
			//thread local data is not initialized so far if Tracing is called from
			//e.g. a worker thread like in Search or UpdateEfficiency worker
			//TODO:pb:check if this was also a problem with IM threads.
			ureq = tld.getHttpServletRequest();
		}
		UserSession usess = null;
		Identity identity = null;
		String remoteIp = null;
		String userAgent = null;
		String referer = null;
		if (ureq != null) {
			usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSessionIfAlreadySet(ureq);
			if (usess != null) {
				identity = usess.getIdentity();
				remoteIp = ureq.getRemoteAddr();
				userAgent = ureq.getHeader("User-Agent");
				referer = ureq.getHeader("Referer");
			}
		}

		StringBuilder sb = new StringBuilder(256);
		if (Settings.isDebuging()) {
			// Short version for console output during debugging
			if (userObj != null) {
				sb.append(userObj).append(" ");
			}
		} else {
			sb.append(PREFIX);
			sb.append(category);
			sb.append(SEPARATOR);
			try {
				// Node-Id + Error number e.g. N1-E17
				sb.append("N");
				sb.append(WebappHelper.getNodeId());
				sb.append("-");
			} catch (Throwable th) {
				//ok
				sb.append(N_A);
			}
				
			sb.append(prefix);
			sb.append(refNum);
			sb.append(SEPARATOR);
			sb.append(callingClass == null ? N_A : callingClass.getPackage().getName());
			sb.append(SEPARATOR);
			sb.append(identity == null ? N_A : identity.getKey());
			sb.append(SEPARATOR);
			sb.append(remoteIp == null ? N_A : remoteIp);
			sb.append(SEPARATOR);
			sb.append(referer == null ? N_A : referer);
			sb.append(SEPARATOR);
			sb.append(userAgent == null ? N_A : userAgent);
			sb.append(SEPARATOR);
			sb.append(userObj == null ? N_A : userObj);
			sb.append(SEPARATOR);
		}
		sb.append(logMsg == null ? N_A : logMsg.replaceAll("[\\r\\f]", "").replaceAll("[/^]M", "").replaceAll("[\\r\\n]", ""));
		return sb.toString();
	}

	private static String assembleThrowableMessage(String category, char prefix,long refNum, Class<?> callingClass, String logMsg, Throwable cause) {

		HttpServletRequest ureq = null;
		if(tld != null){
			//thread local data is not initialized so far if Tracing is called from
			//e.g. a worker thread like in Search or UpdateEfficiency worke
			ureq = tld.getHttpServletRequest();
		}
		UserSession usess = null;
		Identity identity = null;
		String remoteIp = null;
		String userAgent = null;
		String referer = null;
		if (ureq != null) {
			usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(ureq);
			identity = usess.getIdentity();
			remoteIp = ureq.getRemoteAddr();
			userAgent = ureq.getHeader("User-Agent");
			referer = ureq.getHeader("Referer");
		}

		StringBuilder sb = new StringBuilder(2048);
		if (!Settings.isDebuging()) {
			sb.append(PREFIX);
			sb.append(category);
			sb.append(SEPARATOR);
			try {
				// Node-Id + Error number e.g. N1-E17
				sb.append("N");
				//FIXME:gs remove access to coordinator: gs accessing coordinator here loads the corespring factory. This means we cannot do unit testing without olat 
				// as the first log call will start the whole OLAT stuff.
				sb.append(nodeId);
				sb.append("-");
			} catch (Throwable th) {
				//ok
				sb.append(N_A);
			}
			sb.append(prefix);
			sb.append(refNum);
			sb.append(SEPARATOR);
			sb.append(callingClass == null ? N_A : callingClass.getPackage().getName());
			sb.append(SEPARATOR);
			sb.append(identity == null ? N_A : identity.getKey());
			sb.append(SEPARATOR);
			sb.append(remoteIp == null ? N_A : remoteIp);
			sb.append(SEPARATOR);
			sb.append(referer == null ? N_A : referer);
			sb.append(SEPARATOR);
			sb.append(userAgent == null ? N_A : userAgent);
			sb.append(SEPARATOR);
			// olat:::::
			// for effiency reasons, do not recompile the pattern each time. see javadoc:
			// public String replaceAll(String regex, String replacement) {
			//    return Pattern.compile(regex).matcher(this).replaceAll(replacement);
	    	// }
		}
		sb.append(logMsg == null ? N_A : logMsg.replaceAll("[\\r\\f]", "").replaceAll("[/^]M", "").replaceAll("[\\r\\n]", ""));
		sb.append(SEPARATOR);

		if (cause == null) {
			sb.append(CAUSE_N_A);
		} else {
			Throwable ca = cause;
			int i = 1;
			while (ca != null && i < 10) {
				sb.append(STACK_OF);
				sb.append(i);
				sb.append(CAUSE);
				sb.append(ca.getClass().getName());
				sb.append(DOUBLEPOINT);
				sb.append(ca.getMessage());
				sb.append(REFERS_TO);
				sb.append(getStackTrace(ca));
				i++;
				ca = ca.getCause();
			}
		}
		return sb.toString();
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
	public static void setUreq(HttpServletRequest ureq) {
		tld.setHttpServletRequest(ureq);
	}

	/**
	 * Returns a log4j logger for this class
	 * @deprecated do use createLoggerFor(..) instead
	 * @param clazz
	 * @return the log4 logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		return Logger.getLogger(clazz.getName());
	}

	/**
	 * if debug log level is enabled for argument
	 * 
	 * @param clazz
	 * @deprecated please use OLog log = createLoggerFor(MySample.class) as a private static field in your class and use this log.
	 * @return
	 */
	protected static boolean isDebugEnabled(Class<?> clazz) {
		return Logger.getLogger(clazz).isDebugEnabled();
	}

	/**
	 * @return list all current loggers
	 */
	public static List<Logger> getLoggers() {
		return Collections.list(LogManager.getCurrentLoggers());
	}

	/**
	 * set provided log level for all active loggers.
	 * 
	 * @param logLevel
	 */
	public static void setLevelForAllLoggers(Level logLevel) {
		List<Logger> loggers = getLoggers();
		Iterator<Logger> iter = loggers.iterator();
		while (iter.hasNext()) {
			Logger lo = iter.next();
			lo.setLevel(logLevel);
		}
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
			logger.setLevel(logLevel);
		}
	}

	/**
	 * generates active loggers list sorted by name.
	 * 
	 * @return
	 */
	public static List<Logger> getLoggersSortedByName() {
		List<Logger> loggers = getLoggers();
		Collections.sort(loggers, new Comparator<Logger>() {
			public int compare(Logger a, Logger b) {
				return a.getName().compareTo(b.getName());
			}
		});
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
		/**
		 * @see java.lang.ThreadLocal#initialValue()
		 */
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
