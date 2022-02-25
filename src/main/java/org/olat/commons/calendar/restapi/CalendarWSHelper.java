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
package org.olat.commons.calendar.restapi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.restapi.support.MediaTypeVariants;

/**
 * 
 * Initial date: 08.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarWSHelper {
	
	protected static void transfer(EventVO event, KalendarEvent kalEvent) {
		kalEvent.setDescription(event.getDescription());
		kalEvent.setLocation(event.getLocation());
		kalEvent.setColor(event.getColor());
		kalEvent.setManagedFlags(CalendarManagedFlag.toEnum(event.getManagedFlags()));
		kalEvent.setAllDayEvent(event.isAllDayEvent());
		kalEvent.setLiveStreamUrl(event.getLiveStreamUrl());
        kalEvent.setExternalId(event.getExternalId());
        kalEvent.setExternalSource(event.getExternalSource());
        if(event.getClassification() != null) {
        	int classification = event.getClassification().intValue();
        	if(classification == KalendarEvent.CLASS_PRIVATE
        			|| classification == KalendarEvent.CLASS_PUBLIC
        			|| classification == KalendarEvent.CLASS_X_FREEBUSY) {
        		kalEvent.setClassification(classification);
        	}
        }
        EventLinkVO[] links = event.getLinks();
        if(links != null && links.length > 0) {
        	List<KalendarEventLink> kalendarLinks = new ArrayList<>(links.length);
        	for(EventLinkVO link:links) {
        		KalendarEventLink kalendarLink = new KalendarEventLink(link.getProvider(), link.getId(),
        				link.getDisplayName(), link.getUri(), link.getIconCssClass());
        		kalendarLinks.add(kalendarLink);
        	}
        	kalEvent.setKalendarEventLinks(kalendarLinks);
        }
	}
	
	static boolean hasReadAccess(KalendarRenderWrapper wrapper) {
		return wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY
				|| wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE;
	}
	
	protected static boolean hasWriteAccess(KalendarRenderWrapper wrapper) {
		return wrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_WRITE;
	}
	
	protected static Response processEvents(List<EventVO> events, Boolean onlyFuture, int firstResult, int maxReturns,
			HttpServletRequest httpRequest, Request request) {
		
		if(onlyFuture != null && onlyFuture.booleanValue()) {
			Locale locale = I18nManager.getInstance().getCurrentThreadLocale();
			Calendar cal = CalendarUtils.getStartOfDayCalendar(locale);
			Date today = cal.getTime();
			
			for(Iterator<EventVO> eventIt=events.iterator(); eventIt.hasNext(); ) {
				EventVO event = eventIt.next();
				Date end = event.getEnd();
				if(end.before(today)) {
					eventIt.remove();
				}
			}
		}
		
		Collections.sort(events, new EventComparator());
		
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = events.size();
			if(maxReturns > 0 && firstResult >= 0) {
				if(firstResult >= events.size()) {
					events.clear();
				} else {	
					int lastResult = Math.min(events.size(), firstResult + maxReturns);
					events = events.subList(firstResult, lastResult); 
				}
			}
			
			EventVO[] voes = new EventVO[events.size()];
			voes = events.toArray(voes);
			EventVOes vos = new EventVOes();
			vos.setEvents(voes);
			vos.setTotalCount(totalCount);
			return Response.ok(vos).build();
		} else {
			EventVO[] voes = new EventVO[events.size()];
			voes = events.toArray(voes);
			return Response.ok(voes).build();
		}
	}
	
	private static class EventComparator implements Comparator<EventVO> {
		@Override
		public int compare(EventVO e1, EventVO e2) {
			if(e1 == null) {
				if(e2 == null) return 0;
				return -1;
			}
			if(e2 == null) return 1;
			
			Date d1 = e1.getBegin();
			Date d2 = e2.getBegin();
			if(d1 == null) {
				if(d2 == null) return 0;
				return -1;
			}
			if(d2 == null) return 1;
			return d1.compareTo(d2);
		}
	}

}
