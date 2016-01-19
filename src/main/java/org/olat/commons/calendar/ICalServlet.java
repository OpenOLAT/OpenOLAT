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

package org.olat.commons.calendar;

import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.commons.calendar.model.CalendarFileInfos;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.Strings;


/**
 * Description:<BR>
 * Servlet that serves the ical document.
 * <P>
 * Initial Date:  June 1, 2008
 *
 * @author Udit Sajjanhar
 */
public class ICalServlet extends HttpServlet {

	private static final long serialVersionUID = -155266285395912535L;
	private static final OLog log = Tracing.createLoggerFor(ICalServlet.class);
	
	/** collection of iCal feed prefixs **/
	public static final String[] SUPPORTED_PREFIX = {
			CalendarManager.ICAL_PREFIX_AGGREGATED,
			CalendarManager.ICAL_PREFIX_PERSONAL,
			CalendarManager.ICAL_PREFIX_COURSE,
			CalendarManager.ICAL_PREFIX_GROUP
	};

	/**
	 * Default constructor.
	 */
	public ICalServlet() {
		//
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Tracing.setUreq(req);
		try {
			super.service(req, resp);
		} finally {
			//consume the userrequest.
			Tracing.setUreq(null);
			I18nManager.remove18nInfoFromThread();
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws IOException {
		String requestUrl = request.getPathInfo();
		try {
			if (log.isDebug()) {
				log.debug("doGet pathInfo=" + requestUrl);
			}
			if ((requestUrl == null) || (requestUrl.equals(""))) {
				return; // error
			}

			getIcalDocument(requestUrl, response);
		} catch (ValidationException e) {
			log.warn("Validation Error when generate iCal stream for path::" + request.getPathInfo(), e);
			response.sendError(HttpServletResponse.SC_CONFLICT, requestUrl);
		} catch (IOException e) {
			log.warn("IOException Error when generate iCal stream for path::" + request.getPathInfo(), e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, requestUrl);
		} catch (Exception e) {
			log.warn("Unknown Error in icalservlet", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, requestUrl);
		}
	}
  
	/**
	 * Reads in the appropriate ics file, depending upon the pathInfo:<br>
	 * <ul>
	 * 	<li>/aggregated/<config key>/AUTH_TOKEN.ics</li>
	 *  <li>/user/<user_name>/AUTH_TOKEN.ics</li>
	 *  <li>/group/<user_name>/AUTH_TOKEN/<group_id>.ics</li>
	 *  <li>/course/<user_name>/AUTH_TOKEN/<course_unique_id>.ics</li>
	 * </ul>
	 * 
	 * @param pathInfo
	 * @return Calendar
	 */
	private void getIcalDocument(String requestUrl, HttpServletResponse response)
	throws ValidationException, IOException {
		// get the individual path tokens
		String pathInfo = requestUrl.replaceAll(".ics", "");
		String[] pathInfoTokens = pathInfo.split("/");
		if(pathInfoTokens.length < 4) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, requestUrl);
			return;
		}
		
		String calendarType = pathInfoTokens[1];
		String userName = pathInfoTokens[2];
		String authToken = pathInfoTokens[3];

		String calendarID;
		if(CalendarManager.TYPE_COURSE.equals(calendarType) || CalendarManager.TYPE_GROUP.equals(calendarType)) {
			if(pathInfoTokens.length < 5) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, requestUrl);
				return;
			}
			calendarID = pathInfoTokens[4];
		} else if(CalendarManager.TYPE_USER.equals(calendarType)) {
			if(pathInfoTokens.length < 5) {
				calendarID = userName;
			} else {
				calendarID = pathInfoTokens[4];
			}
		} else if(CalendarManager.TYPE_USER_AGGREGATED.equals(calendarType)) {
			calendarID = userName;
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, requestUrl);
			log.warn("Type not supported: " + pathInfo);
			return;
		}
		
		try {
			response.setCharacterEncoding("UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		if(CalendarManager.TYPE_USER_AGGREGATED.equals(calendarType)) {
			// check the authentication token
			CalendarUserConfiguration config = calendarManager.getCalendarUserConfiguration(Long.parseLong(userName));
			String savedToken = config.getToken();
			if (authToken == null || savedToken == null || !savedToken.equals(authToken)) {
				log.warn("Authenticity Check failed for the ical feed path: " + pathInfo);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, requestUrl);
			} else {
				generateAggregatedCalendar(config.getIdentity(), response);
			}
		} else if (calendarManager.calendarExists(calendarType, calendarID)) {
			// check the authentication token
			String savedToken = calendarManager.getCalendarToken(calendarType, calendarID, userName);
			if (authToken == null || savedToken == null || !savedToken.equals(authToken)) {
				log.warn("Authenticity Check failed for the ical feed path: " + pathInfo);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, requestUrl);
			} else {
				// read and return the calendar file
				Calendar calendar = calendarManager.readCalendar(calendarType, calendarID);
				updateUrlProperties(calendar);
				DBFactory.getInstance().commitAndCloseSession();
				new CalendarOutputter(false).output(calendar, response.getOutputStream());
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, requestUrl);
		}
	}
	
	private void generateAggregatedCalendar(Identity identity, HttpServletResponse response) throws IOException {
		PersonalCalendarManager homeCalendarManager = CoreSpringFactory.getImpl(PersonalCalendarManager.class);
		if(identity == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			List<CalendarFileInfos> iCalFiles = homeCalendarManager.getListOfCalendarsFiles(identity);
			DBFactory.getInstance().commitAndCloseSession();
			
			Writer out = response.getWriter();
			out.write(Calendar.BEGIN);
			out.write(':');
			out.write(Calendar.VCALENDAR);
			out.write(Strings.LINE_SEPARATOR);

			int numOfFiles = iCalFiles.size();
			for(int i=0; i<numOfFiles; i++) {
				outputCalendar(iCalFiles.get(i), out);
			}
			
			out.write(Calendar.END);
			out.write(':');
			out.write(Calendar.VCALENDAR);
		}
	}
	
	private void outputCalendar(CalendarFileInfos fileInfos, Writer out) throws IOException {
		try {
			CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
			Calendar calendar = calendarManager.readCalendar(fileInfos.getCalendarFile());
			updateUrlProperties(calendar);
			
			String prefix = fileInfos.getType() + "-" + fileInfos.getCalendarId() + "-";
			updateUUID(calendar, prefix);
			
			ComponentList events = calendar.getComponents();
			for (final Iterator<?> i = events.iterator(); i.hasNext();) {
				String event = i.next().toString();
				out.write(event);
			}
		} catch (IOException | OLATRuntimeException e) {
			log.error("", e);
		}
	}
	
	private void updateUUID(Calendar calendar, String prefix) {
		for (Iterator<?> eventIter = calendar.getComponents().iterator(); eventIter.hasNext();) {
			Object comp = eventIter.next();
			if (comp instanceof VEvent) {
				VEvent event = (VEvent)comp;
				Uid uid = event.getUid();
				String newUid = prefix.concat(uid.getValue());
				uid.setValue(newUid);
			}
		}
	}
	
	private void updateUrlProperties(Calendar calendar) {
		for (Iterator<?> eventIter = calendar.getComponents().iterator(); eventIter.hasNext();) {
			Object comp = eventIter.next();
			if (comp instanceof VEvent) {
				VEvent event = (VEvent)comp;
				
				PropertyList ooLinkProperties = event.getProperties(CalendarManager.ICAL_X_OLAT_LINK);
				if(ooLinkProperties.isEmpty()) {
					continue;
				}
				
				Url currentUrl = event.getUrl();
				if(currentUrl != null) {
					continue;
				}
				
				for (Iterator<?> iter = ooLinkProperties.iterator(); iter.hasNext();) {
					XProperty linkProperty = (XProperty) iter.next();
					if (linkProperty != null) {
						String encodedLink = linkProperty.getValue();
						StringTokenizer st = new StringTokenizer(encodedLink, "§", false);
						if (st.countTokens() >= 4) {
							st.nextToken();//provider
							st.nextToken();//id
							st.nextToken();//displayname
							
							String uri = st.nextToken();
							try {
								Url urlProperty = new Url();
								urlProperty.setValue(uri);
								event.getProperties().add(urlProperty);
								break;
							} catch (URISyntaxException e) {
								log.error("Invalid URL:" + uri);
							}
						}
					}
				}
			}
		}
	}
}