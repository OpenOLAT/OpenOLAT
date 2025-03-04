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
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.model.CalendarFileInfos;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.util.ResourceLoader;
import net.fortuna.ical4j.util.Strings;
import net.fortuna.ical4j.validate.ValidationException;


/**
 * Description:<BR>
 * Servlet that serves the ical document.
 * <P>
 * Initial Date:  June 1, 2008
 *
 * @author Udit Sajjanhar
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ICalServlet extends HttpServlet {

	private static final long serialVersionUID = -155266285395912535L;
	private static final Logger log = Tracing.createLoggerFor(ICalServlet.class);
	
	private static final int TTL_MINUTES = 15;
	private static final ConcurrentMap<String,VTimeZone> outlookVTimeZones = new ConcurrentHashMap<>();
	
	private CalendarModule calendarModule;
	private CalendarManager calendarManager;
	

	/**
	 * Default constructor.
	 */
	public ICalServlet() {
		//
	}
	
	private CalendarModule getCalendarModule() {
		if(calendarModule == null) {
			calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		}
		return calendarModule;
	}
	
	private CalendarManager getCalendarManager() {
		if(calendarManager == null) {
			calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		}
		return calendarManager;
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Tracing.setHttpRequest(req);
		getCalendarModule();
		getCalendarManager();
		try {
			super.service(req, resp);
		} finally {
			//consume the userrequest.
			Tracing.clearHttpRequest();
			I18nManager.remove18nInfoFromThread();
			DBFactory.getInstance().commitAndCloseSession();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		String requestUrl = request.getPathInfo();
		try {
			//log need a session before the response is committed
			request.getSession();
			log.debug("doGet pathInfo={}", requestUrl);
			if ((requestUrl == null) || (requestUrl.equals(""))) {
				return; // error
			}

			getIcalDocument(requestUrl, request, response);
		} catch (ValidationException e) {
			log.warn("Validation Error when generate iCal stream for path::{}", request.getPathInfo(), e);
			sendError(response, HttpServletResponse.SC_CONFLICT);
		} catch (IOException e) {
			log.warn("IOException Error when generate iCal stream for path::{}", request.getPathInfo(), e);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			log.warn("Unknown Error in icalservlet", e);
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
	
	private void sendError(HttpServletResponse response, int status) {
		try {
			response.sendError(status);
		} catch (IOException e) {
			log.error("", e);
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
	private void getIcalDocument(String requestUrl, HttpServletRequest request, HttpServletResponse response)
	throws ValidationException, IOException {
		// get the individual path tokens
		String pathInfo;
		int icsIndex = requestUrl.indexOf(".ics");
		if(icsIndex > 0) {
			pathInfo = requestUrl.substring(0, icsIndex);
		} else {
			pathInfo = requestUrl;
		}
		
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
			log.warn("Type not supported: {}", pathInfo);
			return;
		}
		
		try {
			response.setCharacterEncoding("UTF-8");
			setCacheControl(response);
		} catch (Exception e) {
			log.error("", e);
		}

		CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
		if(CalendarManager.TYPE_USER_AGGREGATED.equals(calendarType)) {
			// check the authentication token
			CalendarUserConfiguration config = calendarManager.getCalendarUserConfiguration(Long.parseLong(userName));
			String savedToken = config == null ? null : config.getToken();
			if (authToken == null || savedToken == null || !savedToken.equals(authToken)) {
				log.warn("Authenticity Check failed for the ical feed path: {}", pathInfo);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, requestUrl);
			} else {
				generateAggregatedCalendar(config.getIdentity(), request, response);
			}
		} else if (calendarManager.calendarExists(calendarType, calendarID)) {
			// check the authentication token
			String savedToken = null;
			if(StringHelper.isLong(userName)) {
				CalendarUserConfiguration config = calendarManager.getCalendarUserConfiguration(Long.parseLong(userName));
				savedToken = config == null ? null : config.getToken();
			} 
			if(savedToken == null) {
				savedToken = calendarManager.getCalendarToken(calendarType, calendarID, userName);
			}
			DBFactory.getInstance().commitAndCloseSession();
			if (authToken == null || savedToken == null || !savedToken.equals(authToken)) {
				log.warn("Authenticity Check failed for the ical feed path: {}", pathInfo);
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, requestUrl);
			} else {
				// read and return the calendar file
				Calendar calendar = calendarManager.readCalendar(calendarType, calendarID);
				outputCalendar(calendar, request, response);
			}
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, requestUrl);
		}
	}
	
	private void setCacheControl(HttpServletResponse httpResponse) {
	    httpResponse.setDateHeader("Expires", 0l);
	    httpResponse.setHeader("Cache-Control", "max-age=0");
	}
	
	private void outputCalendar(Calendar calendar, HttpServletRequest request, HttpServletResponse response)
	throws ValidationException, IOException {
		Agent agent = getAgent(request);
		updateUrlProperties(calendar);
		
		try(Writer out = response.getWriter()) {
			out.write(Calendar.BEGIN);
			out.write(':');
			out.write(Calendar.VCALENDAR);
			out.write(Strings.LINE_SEPARATOR);
			out.write(Version.VALUE_2_0);
			
			boolean calScale = false;
			for (Iterator<?> propIter = calendar.getProperties().iterator(); propIter.hasNext();) {
				Object pobject = propIter.next();
				if(pobject instanceof Property property) {
					if(Property.VERSION.equals(property.getName())) {
						//we force version 2.0
					} else if(Property.CALSCALE.equals(property.getName())) {
						out.write(property.toString());
						calScale = true;
					} else {
						out.write(property.toString());
					}
				}
			}
			
			if(!calScale) {
				out.write(CalScale.VALUE_GREGORIAN);
			}
	
			outputTTL(agent, out);
	
			Set<String> timezoneIds = new HashSet<>();
			outputCalendarComponents(calendar, out, agent, timezoneIds);
			if(agent == Agent.outlook) {
				outputTimeZoneForOutlook(timezoneIds, out);
			}
			
			out.write(Calendar.END);
			out.write(':');
			out.write(Calendar.VCALENDAR);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	/**
	 * Collect all the calendars, update the URL properties and the UUID.
	 * 
	 * @param identity
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void generateAggregatedCalendar(Identity identity, HttpServletRequest request, HttpServletResponse response) throws IOException {
		PersonalCalendarManager homeCalendarManager = CoreSpringFactory.getImpl(PersonalCalendarManager.class);
		if(identity == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else {
			List<CalendarFileInfos> iCalFiles = homeCalendarManager.getListOfCalendarsFiles(identity);
			DBFactory.getInstance().commitAndCloseSession();
			Agent agent = getAgent(request);
			
			try(Writer out = response.getWriter()) {
				out.write(Calendar.BEGIN);
				out.write(':');
				out.write(Calendar.VCALENDAR);
				out.write(Strings.LINE_SEPARATOR);
				out.write(Version.VALUE_2_0);
				out.write(CalScale.VALUE_GREGORIAN);
				
				outputTTL(agent, out);
	
				Set<String> timezoneIds = new HashSet<>();
				int numOfFiles = iCalFiles.size();
				for(int i=0; i<numOfFiles; i++) {
					outputCalendar(iCalFiles.get(i), out, agent, timezoneIds);
				}
				if(agent == Agent.outlook) {
					outputTimeZoneForOutlook(timezoneIds, out);
				}
				
				out.write(Calendar.END);
				out.write(':');
				out.write(Calendar.VCALENDAR);
			} catch(IOException e) {
				log.error("", e);
			}
		}
	}
	
	private Agent getAgent(HttpServletRequest request) {
		String userAgent = request.getHeader("User-Agent");
		if(userAgent == null) {
			return Agent.unkown;
		} else if(userAgent.indexOf("Microsoft ") >= 0 // to catch "Microsoft Outlook", "Microsoft Office", "Microsoft Exchange" which are all user agents used by outlook.com
				|| userAgent.equals("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)")) {// <- this is the user agent of Outlook live
			return Agent.outlook;
		} else if(userAgent.indexOf("Google") >= 0 && userAgent.indexOf("Calendar") >= 0) {
			return Agent.googleCalendar;
		} else if(userAgent.startsWith("Java/1.")) {
			return Agent.java;
		} else if(userAgent.indexOf("CalendarAgent/") >= 0) {
			return Agent.calendar;
		}
		return Agent.unkown;
	}
	
	/**
	 * Append TTL:<br>
	 * @see http://stackoverflow.com/questions/17152251/specifying-name-description-and-refresh-interval-in-ical-ics-format
	 * @see http://tools.ietf.org/html/draft-daboo-icalendar-extensions-06
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void outputTTL(Agent agent, Writer out)
	throws IOException {
		out.write("X-PUBLISHED-TTL:PT" + TTL_MINUTES + "M");
		out.write(Strings.LINE_SEPARATOR);
		if(agent == null || agent != Agent.java) {
			out.write("REFRESH-INTERVAL;VALUE=DURATION:PT" + TTL_MINUTES + "M");
			out.write(Strings.LINE_SEPARATOR);
		}
	}
	
	private void outputTimeZoneForOutlook(Set<String> timezoneIds,  Writer out) {
		for(String timezoneId:timezoneIds) {
			if(StringHelper.containsNonWhitespace(timezoneId)) {
				try {
					VTimeZone vTimeZone = getOutlookVTimeZone(timezoneId);
					if(vTimeZone != null) {
						out.write(vTimeZone.toString());
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		}
	}
	
	private void outputCalendar(CalendarFileInfos fileInfos, Writer out, Agent agent, Set<String> timezoneIds)
	throws IOException {
		try {
			CalendarManager calendarManager = CoreSpringFactory.getImpl(CalendarManager.class);
			Calendar calendar = calendarManager.readCalendar(fileInfos.getCalendarFile());
			updateUrlProperties(calendar);
			
			String prefix = fileInfos.getType() + "-" + fileInfos.getCalendarId() + "-";
			updateUUID(calendar, prefix);
			
			outputCalendarComponents(calendar, out, agent, timezoneIds);
		} catch (IOException | OLATRuntimeException e) {
			log.error("", e);
		}
	}
	
	private void outputCalendarComponents(Calendar calendar, Writer out, Agent agent, Set<String> timezoneIds)
	throws IOException {
		try {
			List<CalendarComponent> events = calendar.getComponents();
			for (CalendarComponent event:events) {
				outputCalendarComponent(event, out, agent, timezoneIds);
			}
		} catch (IOException | OLATRuntimeException e) {
			log.error("", e);
		}
	}
	
	private void outputCalendarComponent(CalendarComponent component, Writer out, Agent agent, Set<String> timezoneIds) throws IOException {
		if (component instanceof VEvent vEvent) {
			component = rewriteExDate(vEvent);
		}

		String event = component.toString();
		if (agent == Agent.outlook && component instanceof VEvent vEvent) {
			event = quoteTimeZone(event, vEvent, timezoneIds);
		}
		if(agent == Agent.googleCalendar) {
			event = event.replace("CLASS:PRIVATE" + Strings.LINE_SEPARATOR, "");
			event = event.replace("X-OLAT-MANAGED:all" + Strings.LINE_SEPARATOR, "");
			event = event.replace("DESCRIPTION:" + Strings.LINE_SEPARATOR, "");
			event = event.replace("LOCATION:" + Strings.LINE_SEPARATOR, "");
		}
		
		out.write(event);
	}
	
	/**
	 * 
	 * @param event The event to rewrite
	 */
	private VEvent rewriteExDate(VEvent event) {
		DtStart<Temporal> start = event.getDateTimeStart();
		@SuppressWarnings("unchecked")
		ExDate<Temporal> exDate = (ExDate<Temporal>)event.getProperty(Property.EXDATE).orElse(null);

		if(exDate != null && start != null) {
			ZonedDateTime startDate = CalendarUtils.convertDateProperty(start, getCalendarModule().getDefaultZoneId());
			ZoneId zone = startDate.getZone();
			if(zone == null) {
				zone = getCalendarModule().getDefaultZoneId();
			}
			List<Temporal> dateList = exDate.getDates();
			List<ZonedDateTime> list = new ArrayList<>();
			for(Temporal obj:dateList) {
				ZonedDateTime d = CalendarUtils.convertTemporal(obj, zone);
				if(d != null) {
					list.add(d);
				}
			}
			
			// EXDATE;TZID=Europe/Zurich:20250221T150000
			// EXDATE;TZID=America/Los_Angeles:20240902T190000
			ParameterList pList = new ParameterList(List.of(new TzId(zone.getId())));
			DateList<ZonedDateTime> dList = new DateList<>(list);
			ExDate<ZonedDateTime> newExDate = new ExDate<>(pList, dList);
			event = event.remove(exDate);
			event = event.add(newExDate);
		}
		return event;
	}
	
	private String quoteTimeZone(String event, VEvent vEvent, Set<String> timezoneIds) {
		if(vEvent == null) return event;
		
		DtStart<Temporal> start = vEvent.getDateTimeStart();
		if(start.getDate() == null) return event;
		
		ZonedDateTime zonedStart = ZonedDateTime.from(start.getDate());
		if(zonedStart.getZone() == null) return event;

		String timezoneId = zonedStart.getZone().getId();
		timezoneIds.add(timezoneId);
		
		TzId tzId = (TzId)start.getParameter(Parameter.TZID).orElse(null);
		String tzidReplacement = "TZID=\"" + timezoneId + "\"";	
		return event.replace(tzId.toString(), tzidReplacement);
	}
	
	private void updateUUID(Calendar calendar, String prefix) {
		for (Iterator<?> eventIter = calendar.getComponents().iterator(); eventIter.hasNext();) {
			Object comp = eventIter.next();
			if (comp instanceof VEvent event) {
				Uid uid = event.getUid().orElse(null);
				if(uid != null) {
					String newUid = prefix.concat(uid.getValue());
					uid.setValue(newUid);
				}
			}
		}
	}
	
	private void updateUrlProperties(Calendar calendar) {
		for (Iterator<CalendarComponent> eventIter = calendar.getComponents().iterator(); eventIter.hasNext();) {
			CalendarComponent comp = eventIter.next();
			if (comp instanceof VEvent event) {
				List<Property> ooLinkProperties = event.getProperties(CalendarManager.ICAL_X_OLAT_LINK);
				if(ooLinkProperties.isEmpty()) {
					continue;
				}
				
				Url currentUrl = event.getUrl();
				if(currentUrl != null) {
					continue;
				}
				
				for (Iterator<Property> iter = ooLinkProperties.iterator(); iter.hasNext();) {
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
							} catch (Exception e) {
								log.error("Invalid URL:{}", uri);
							}
						}
					}
				}
			}
		}
	}
	
	/**
     * Load the VTimeZone for Outlook. ical4j use a static map to reuse the TimeZone objects, we need to load
     * and save our specialized TimeZone in a separate map.
     */
    private VTimeZone getOutlookVTimeZone(final String id) {
    	return outlookVTimeZones.computeIfAbsent(id, timeZoneId -> {
        	try {
				URL resource = ResourceLoader.getResource("zoneinfo-outlook/" + id + ".ics");
				Calendar calendar = buildCalendar(resource);
				return calendar == null ? null : (VTimeZone)calendar.getComponent(Component.VTIMEZONE).orElse(null);
			} catch (Exception e) {
				log.error("", e);
				return null;
			}
    	});
    }
    
    private Calendar buildCalendar(URL resource) {
    	try(InputStream in = resource.openStream()) {
    		return CalendarUtils.buildCalendar(in);
    	} catch(Exception e) {
    		log.error("", e);
    		return null;
    	}
    }
    
    private enum Agent {
    		unkown,
    		outlook,
    		googleCalendar,
    		calendar,// macos, iOS
    		java
    }
}