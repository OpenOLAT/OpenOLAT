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

package org.olat.admin;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.admin.sysinfo.InfoMessageManager;
import org.olat.admin.sysinfo.SysInfoMessage;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.session.UserSessionManager;

/**
 * 
 * This dispatcher acts as proxy to receive the message.
 * @see org.olat.admin.AdminModule#setMaintenanceMessage(HttpServletRequest, HttpServletResponse) 
 * 
 * <P>
 * Initial Date:  13.06.2006 <br>
 * @author patrickb
 * @author christian guretzki
 */
public class AdminModuleDispatcher implements Dispatcher {
	private static final Logger log = Tracing.createLoggerFor(AdminModuleDispatcher.class);

	private static final String PARAMETER_CMD          = "cmd"; 
	private static final String PARAMETER_MSG          = "msg";
	private static final String PARAMETER_START        = "start";
	private static final String PARAMETER_END          = "end";
	private static final String PARAMETER_CLEAR_ON_RESTART  = "clearOnRestart";
	private static final String PARAMETER_MAX_MESSAGE  = "maxsessions";
	private static final String PARAMETER_NBR_SESSIONS = "nbrsessions";
	private static final String PARAMETER_SESSIONTIMEOUT ="sec";
	
	private static final String CMD_GET_MESSAGES	           = "getmessages";
	private static final String CMD_SET_MAINTENANCE_MESSAGE    = "setmaintenancemessage";
	private static final String CMD_SET_INFO_MESSAGE    	   = "setinfomessage"; 
	private static final String CMD_SET_LOGIN_BLOCKED          = "setloginblocked";
	private static final String CMD_SET_LOGIN_NOT_BLOCKED      = "setloginnotblocked";
	private static final String CMD_SET_MAX_SESSIONS           = "setmaxsessions";
	private static final String CMD_INVALIDATE_ALL_SESSIONS    = "invalidateallsessions";
	private static final String CMD_INVALIDATE_OLDEST_SESSIONS = "invalidateoldestsessions";
	private static final String CMD_SET_SESSIONTIMEOUT         = "sessiontimeout";
	
	private static final DateFormat FORMAT_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");
	private static final DateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		String cmd = request.getParameter(PARAMETER_CMD);
		if (CMD_SET_MAINTENANCE_MESSAGE.equalsIgnoreCase(cmd) || CMD_SET_INFO_MESSAGE.equalsIgnoreCase(cmd)) {
			handleSetMaintenanceOrInfoMessage(request, response, cmd);
		} else if (cmd.equalsIgnoreCase(CMD_GET_MESSAGES)) {	
			InfoMessageManager mrg = (InfoMessageManager) CoreSpringFactory.getBean(InfoMessageManager.class);
			StringBuilder result = new StringBuilder();
			SysInfoMessage2XML(mrg.getInfoMessage(), result);
			SysInfoMessage2XML(mrg.getMaintenanceMessage(), result);
			if ("Cluster".equals(Settings.getClusterMode())) {				
				SysInfoMessage2XML(mrg.getInfoMessageNodeOnly(), result);
				SysInfoMessage2XML(mrg.getMaintenanceMessageNodeOnly(), result);
			}
			ServletUtil.serveStringResource(response, result.toString());
			
		} else {
			if (CoreSpringFactory.getImpl(AdminModule.class).checkSessionAdminToken(request)) {
				handleSessionsCommand(request, response, cmd);
			} else {
				DispatcherModule.sendForbidden(request.getPathInfo(), response);
			}
		}
	}

	/**
	 * Handle session-administration commands (setLoginBlocked, setLoginNotBlocked, setMaxSession, invalidateAllSessions,
	 * ïnvalidateOldestSessions).
	 */
	private void handleSessionsCommand(HttpServletRequest request, HttpServletResponse response, String cmd) {
		if (cmd.equalsIgnoreCase(CMD_SET_LOGIN_BLOCKED)) {
			CoreSpringFactory.getImpl(AdminModule.class).setLoginBlocked(true, false);
			ServletUtil.serveStringResource(response, "Ok, login blocked");
		} else if (cmd.equalsIgnoreCase(CMD_SET_LOGIN_NOT_BLOCKED)) {
			CoreSpringFactory.getImpl(AdminModule.class).setLoginBlocked(false, false);
			ServletUtil.serveStringResource(response, "Ok, login no more blocked");
		}else if (cmd.equalsIgnoreCase(CMD_SET_MAX_SESSIONS)) {
			handleSetMaxSessions(request, response);
		}else if (cmd.equalsIgnoreCase(CMD_INVALIDATE_ALL_SESSIONS)) {
			CoreSpringFactory.getImpl(UserSessionManager.class).invalidateAllSessions();
			ServletUtil.serveStringResource(response, "Ok, Invalidated all sessions");
		}else if (cmd.equalsIgnoreCase(CMD_INVALIDATE_OLDEST_SESSIONS)) {
			handleInvidateOldestSessions(request, response);
		}else if(cmd.equalsIgnoreCase(CMD_SET_SESSIONTIMEOUT)) {
			handleSetSessiontimeout(request, response);
		} else {
			ServletUtil.serveStringResource(response, "NOT OK, unknown command=" + cmd);
		}
	}

	/**
	 * Handle setMaxSessions command, extract parameter maxsessions form request and call method on AdminModule.
	 * @param request
	 * @param response
	 */
	private void handleSetMaxSessions(HttpServletRequest request, HttpServletResponse response) {
		String maxSessionsString = request.getParameter(PARAMETER_MAX_MESSAGE);
		if (maxSessionsString == null || maxSessionsString.equals("")) {
			ServletUtil.serveStringResource(response, "NOT_OK, missing parameter " + PARAMETER_MAX_MESSAGE);
		} else {
			try {
				int maxSessions = Integer.parseInt(maxSessionsString);
				CoreSpringFactory.getImpl(AdminModule.class).setMaxSessions(maxSessions);
				ServletUtil.serveStringResource(response, "Ok, max-session=" + maxSessions);
			} catch (NumberFormatException nbrException) {
				ServletUtil.serveStringResource(response, "NOT_OK, parameter " + PARAMETER_MAX_MESSAGE + " must be a number");
			}
		}
	}
	
	private void handleSetSessiontimeout(HttpServletRequest request, HttpServletResponse response) {
		String paramStr = request.getParameter(PARAMETER_SESSIONTIMEOUT);
		if (paramStr == null || paramStr.equals("")) {
			ServletUtil.serveStringResource(response, "NOT_OK, missing parameter " + PARAMETER_SESSIONTIMEOUT);
		} else {
			try {
				int sessionTimeout = Integer.parseInt(paramStr);
				CoreSpringFactory.getImpl(UserSessionManager.class).setGlobalSessionTimeout(sessionTimeout);
				ServletUtil.serveStringResource(response, "Ok, sessiontimeout=" + sessionTimeout);
			} catch (NumberFormatException nbrException) {
				ServletUtil.serveStringResource(response, "NOT_OK, parameter " + PARAMETER_SESSIONTIMEOUT + " must be a number");
			}
		}
	}
	

	/**
	 * Handle invalidateOldestSessions command, extract parameter nbrsessions form request and call method on AdminModule.
	 * @param request
	 * @param response
	 */
	private void handleInvidateOldestSessions(HttpServletRequest request, HttpServletResponse response) {
		String nbrSessionsString = request.getParameter(PARAMETER_NBR_SESSIONS);
		if (nbrSessionsString == null || nbrSessionsString.equals("")) {
			ServletUtil.serveStringResource(response, "NOT_OK, missing parameter " + PARAMETER_NBR_SESSIONS);
		} else {
			try {
				int nbrSessions = Integer.parseInt(nbrSessionsString);
				CoreSpringFactory.getImpl(UserSessionManager.class).invalidateOldestSessions(nbrSessions);
				ServletUtil.serveStringResource(response, "Ok, Invalidated oldest sessions, nbrSessions=" + nbrSessions);
			} catch (NumberFormatException nbrException) {
				ServletUtil.serveStringResource(response, "NOT_OK, parameter " + PARAMETER_NBR_SESSIONS + " must be a number");
			}
		}
	}

	/**
	 * Handle setMaintenanceMessage command, extract parameter msg form request and call method on AdminModule.
	 * @param request
	 * @param response
	 */
	private void handleSetMaintenanceOrInfoMessage(HttpServletRequest request, HttpServletResponse response, String cmd) {
		AdminModule adminModule = CoreSpringFactory.getImpl(AdminModule.class);
		if (adminModule.checkMaintenanceMessageToken(request)) {
			String message = request.getParameter(PARAMETER_MSG);
			if (cmd.equalsIgnoreCase(CMD_SET_INFO_MESSAGE)){				
				Date start = parseDateFromRequest(request, PARAMETER_START);
				Date end = parseDateFromRequest(request, PARAMETER_END);				
				boolean clearOnRestart = false; // Default
				if (request.getParameter(PARAMETER_CLEAR_ON_RESTART) != null) {
					clearOnRestart = Boolean.parseBoolean(request.getParameter(PARAMETER_CLEAR_ON_RESTART));					
				}
				InfoMessageManager mrg = (InfoMessageManager) CoreSpringFactory.getBean(InfoMessageManager.class);
				mrg.setInfoMessage(message, start, end, clearOnRestart);
				ServletUtil.serveStringResource(response, "Ok, new infoMessage is::" + message);
			} else if (cmd.equalsIgnoreCase(CMD_SET_MAINTENANCE_MESSAGE)){
				Date start = parseDateFromRequest(request, PARAMETER_START);
				Date end = parseDateFromRequest(request, PARAMETER_END);				
				boolean clearOnRestart = true; // Default
				if (request.getParameter(PARAMETER_CLEAR_ON_RESTART) != null) {
					clearOnRestart= Boolean.parseBoolean(request.getParameter(PARAMETER_CLEAR_ON_RESTART));
				}
				InfoMessageManager mrg = (InfoMessageManager) CoreSpringFactory.getBean(InfoMessageManager.class);
				mrg.setMaintenanceMessage(message, start, end, clearOnRestart);
				ServletUtil.serveStringResource(response, "Ok, new maintenanceMessage is::" + message);
			} 
		} else {
			DispatcherModule.sendForbidden(request.getPathInfo(), response);
		}
	}
	
	
	private void SysInfoMessage2XML(SysInfoMessage sim, StringBuilder result) {
		if (sim.hasMessage()) {
			result.append("<").append(sim.getType()).append(">");
			result.append("<message>").append(sim.getMessage()).append("</message>");
			if (sim.getStart() != null) {
				result.append("<start>").append(FORMAT_DATETIME.format(sim.getStart())).append("</start>");				
			}
			if (sim.getEnd() != null) {
				result.append("<end>").append(FORMAT_DATETIME.format(sim.getEnd())).append("</end>");
			}
			result.append("<clearOnRestart>").append(sim.isClearOnRestart()).append("</clearOnRestart>");			
			result.append("</").append(sim.getType()).append(">\n");
		}
		
	}
	
	/**
	 * Helper to parse the date from the request using the datetime
	 * yyyy-MM-ddTHH-mm or date yyyy-MM-dd format
	 * 
	 * @param request
	 * @param paramName
	 * @return The date representing the request parameter or NULL if not available
	 *         or can't be parsed
	 */
	private Date parseDateFromRequest(final HttpServletRequest request, final String paramName) {		
		Date date = null;
		String dateParam = request.getParameter(paramName);
		if (StringHelper.containsNonWhitespace(dateParam)) {
			dateParam = StringHelper.escapeHtml(dateParam); // prevent XSS attacks
			try {
				if (dateParam.length() == 10) {
					date = FORMAT_DATE.parse(dateParam);
				} else if (dateParam.length() == 16) {
					date = FORMAT_DATETIME.parse(dateParam);
				}
			} catch (ParseException e) {
				log.error("Can not parse date parameter::" + paramName + " with value::" + dateParam, e);
			}
		}
		return date;					
	}
	
	
}
