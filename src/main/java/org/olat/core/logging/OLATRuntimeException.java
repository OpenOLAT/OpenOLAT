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
*/ 

package org.olat.core.logging;

import org.olat.core.util.filter.FilterFactory;

/**
*  Description:<br />
*	 Thrown if an unrecoverable error occurs. These Exceptions get caught by the Servlet. The user
*  will get an orange screen and a log message is recorded.
*
* @author Felix Jost
*/
public class OLATRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -1627846608356883591L;
	private final String logMsg;
	private final String usrMsgKey;
	private final String usrMsgPackage;
	private final String[] usrMsgArgs;
	private final Class<?> throwingClazz;

	/**
	 * @param throwing class
	 * @param usrMsgKey
	 * @param usrMsgArgs
	 * @param usrMsgPackage
	 * @param logMsg
	 * @param cause
	 */
	public OLATRuntimeException(Class<?> throwingClazz, String usrMsgKey, String[] usrMsgArgs, String usrMsgPackage, String logMsg, Throwable cause) {
		super(logMsg);
		this.throwingClazz = throwingClazz != null ? throwingClazz : OLATRuntimeException.class;
		this.usrMsgKey = usrMsgKey;
		this.usrMsgArgs = usrMsgArgs;
		this.usrMsgPackage = usrMsgPackage;
		this.logMsg = logMsg;
		if (cause == null) {
			cause = new Exception("olat_rtexception_stackgenerator");
		}
		initCause(cause);
	}

	/**
	 * @param usrMsgKey
	 * @param usrMsgArgs
	 * @param usrMsgPackage
	 * @param logMsg
	 * @param cause
	 */
	public OLATRuntimeException(String usrMsgKey, String[] usrMsgArgs, String usrMsgPackage, String logMsg, Throwable cause) {
		this (OLATRuntimeException.class, usrMsgKey, usrMsgArgs, usrMsgPackage, logMsg, cause);
	}

	/**
	 * @param category
	 * @param logMsg
	 * @param cause
	 */
	public OLATRuntimeException(Class<?> throwingClazz, String logMsg, Throwable cause) {
		this (throwingClazz, null, null, null, logMsg, cause);
	}

	/**
	 * @param logMsg
	 * @param cause
	 */
	public OLATRuntimeException(String logMsg, Throwable cause) {
		this (OLATRuntimeException.class, null, null, null, logMsg, cause);
	}
	
	public OLATRuntimeException(String logMsg) {
		this (OLATRuntimeException.class, null, null, null, logMsg, null);
	}

	/**
	 * Format throwable as HTML fragment.
	 * @param th
	 * @return HTML fragment.
	 */
	public static String throwableToHtml(Throwable th) {
		StringBuilder sb = new StringBuilder("<br>");
		if (th == null) {
			sb.append("n/a");
		}
		else {	
			sb.append("Throwable: "+th.getClass().getName()+"<br /><br />");
			toHtml(sb, th);
			// 1st cause:
			Throwable ca = th.getCause();
			int i=1;
			while (ca != null) {
				sb.append("<hr /><br />"+i+". cause:<br /><br />");
				toHtml(sb,ca);
				i++;
				ca = ca.getCause();	
			}
		}
		return FilterFactory.getXSSFilter().filter(sb.toString());
	}

	private static void toHtml(StringBuilder sb, Throwable th) {
		if (th instanceof OLATRuntimeException) {
			sb.append("logmsg:").append(((OLATRuntimeException)th).getLogMsg()).append("<br />");
		}
		sb.append("message:" + th.getMessage() + "," +th.getClass().getName() + "<br /><br />");
		StackTraceElement[] ste = th.getStackTrace();	
		int nr = ste.length < 10? ste.length: 10;	
		for (int i = 0; i < nr; i++) {
			StackTraceElement st = ste[i];
			sb.append("at "+ st.toString() + "<br />");
		}
	}

	/**
	 * @return the log message
	 */
	public String getLogMsg() {
		return logMsg;
	}

	public Class<?> getThrowingClazz() {
		return throwingClazz;
	}

	/**
	 * @return String key of user message in the given package
	 */
	public String getUsrMsgKey() {
		return usrMsgKey;
	}
	/**
	 * @return String package name where usr msg key is found
	 */
	public String getUsrMsgPackage() {
		return usrMsgPackage;
	}
	/**
	 * @return String[] The translator arguments or null if none available
	 */
	public String[] getUsrMsgArgs() {
		return usrMsgArgs;
	}
}
