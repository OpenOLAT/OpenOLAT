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
package org.olat.admin.sysinfo;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 16.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionSnoopController extends BasicController {

	private final VelocityContainer mySnoop;
	
	public UserSessionSnoopController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mySnoop = createVelocityContainer("snoop");
		loadModel(ureq);
		putInitialPanel(mySnoop);
	}
	
	private void loadModel(UserRequest ureq) {
		mySnoop.contextPut("snoop", getSnoop(ureq));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	/**
	 * @param ureq
	 * @return Formatted HTML
	 */
	private String getSnoop(UserRequest ureq) {
		StringBuilder sb = new StringBuilder();
		HttpServletRequest hreq = ureq.getHttpReq();
		sb.append("<h4>Request attributes:</h4>");
		Enumeration<String> e = hreq.getAttributeNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			Object value = hreq.getAttribute(key);
			appendFormattedKeyValue(sb, key, value);
		}

		appendFormattedKeyValue(sb, "Protocol", hreq.getProtocol());
		appendFormattedKeyValue(sb, "Scheme", hreq.getScheme());
		appendFormattedKeyValue(sb, "Server Name", hreq.getServerName());
		appendFormattedKeyValue(sb, "Server Port", new Integer(hreq.getServerPort()));
		appendFormattedKeyValue(sb, "Remote Addr", hreq.getRemoteAddr());
		appendFormattedKeyValue(sb, "Remote Host", hreq.getRemoteHost());
		appendFormattedKeyValue(sb, "Character Encoding", hreq.getCharacterEncoding());
		appendFormattedKeyValue(sb, "Content Length", new Integer(hreq.getContentLength()));
		appendFormattedKeyValue(sb, "Content Type", hreq.getContentType());
		appendFormattedKeyValue(sb, "Locale", hreq.getLocale());

		sb.append("<h4>Parameter names in this hreq:</h4>");
		e = hreq.getParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String[] values = hreq.getParameterValues(key);
			String value = "";
			for (int i = 0; i < values.length; i++) {
				value = value + " " + values[i];
			}
			appendFormattedKeyValue(sb, key, value);
		}
		
		sb.append("<h4>Headers in this hreq:</h4>");
		e = hreq.getHeaderNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = hreq.getHeader(key);
			appendFormattedKeyValue(sb, key, value);
		}
		sb.append("<h4>Cookies in this hreq:</h4>");
		Cookie[] cookies = hreq.getCookies();
		if (cookies != null) {
			for (int i = 0; i < cookies.length; i++) {
				Cookie cookie = cookies[i];
				appendFormattedKeyValue(sb, cookie.getName(), cookie.getValue());
			}
		}

		sb.append("<h4>Hreq parameters:</h4>");
		appendFormattedKeyValue(sb, "Request Is Secure", new Boolean(hreq.isSecure()));
		appendFormattedKeyValue(sb, "Auth Type", hreq.getAuthType());
		appendFormattedKeyValue(sb, "HTTP Method", hreq.getMethod());
		appendFormattedKeyValue(sb, "Remote User", hreq.getRemoteUser());
		appendFormattedKeyValue(sb, "Request URI", hreq.getRequestURI());
		appendFormattedKeyValue(sb, "Context Path", hreq.getContextPath());
		appendFormattedKeyValue(sb, "Servlet Path", hreq.getServletPath());
		appendFormattedKeyValue(sb, "Path Info", hreq.getPathInfo());
		appendFormattedKeyValue(sb, "Path Trans", hreq.getPathTranslated());
		appendFormattedKeyValue(sb, "Query String", hreq.getQueryString());

		HttpSession hsession = hreq.getSession();
		appendFormattedKeyValue(sb, "Requested Session Id", hreq.getRequestedSessionId());
		appendFormattedKeyValue(sb, "Current Session Id", hsession.getId());
		appendFormattedKeyValue(sb, "Session Created Time", new Long(hsession.getCreationTime()));
		appendFormattedKeyValue(sb, "Session Last Accessed Time", new Long(hsession.getLastAccessedTime()));
		appendFormattedKeyValue(sb, "Session Max Inactive Interval Seconds",	new Long(hsession.getMaxInactiveInterval()));
		
		sb.append("<h4>Session values:</h4> ");
		Enumeration<String> names = hsession.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			appendFormattedKeyValue(sb, name, hsession.getAttribute(name));
		}
		return sb.toString();
	}
	
	private void appendFormattedKeyValue(StringBuilder sb, String key, Object value) {
		sb.append("&nbsp;&nbsp;&nbsp;<b>");
		sb.append(key);
		sb.append(":</b>&nbsp;");
		sb.append(value);
		sb.append("<br />");
	}
	
	

}
