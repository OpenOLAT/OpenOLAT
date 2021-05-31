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
package org.olat.course.nodes.livestream.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamEvent;
import org.olat.course.nodes.livestream.LiveStreamModule;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.model.LiveStreamEventImpl;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LiveStreamServiceImpl implements LiveStreamService, DisposableBean {
	
	private static final Logger log = Tracing.createLoggerFor(LiveStreamServiceImpl.class);
	
	private ScheduledExecutorService scheduler;
	
	@Autowired
	private LiveStreamModule liveStreamModule;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private UrlTemplateDAO urlTemplateDao;
	@Autowired
	private LaunchDAO launchDao;
	@Autowired
	private HttpClientService httpClientService;

	@Override
	public ScheduledExecutorService getScheduler() {
		if (scheduler == null) {
			ThreadFactory threadFactory = new CustomizableThreadFactory("oo-livestream-");
			scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		}
		return scheduler;
	}

	@Override
	public void destroy() throws Exception {
		if(scheduler != null) {
			scheduler.shutdownNow();
		}
	}

	@Override
	public List<? extends LiveStreamEvent> getRunningEvents(CourseCalendars calendars, int bufferBeforeMin,
			int bufferAfterMin) {
		Date now = new Date();
		
		Calendar cFrom = Calendar.getInstance();
		cFrom.setTime(now);
		cFrom.add(Calendar.MINUTE, -bufferAfterMin);
		Date from = cFrom.getTime();
		
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(now);
		cTo.add(Calendar.MINUTE, bufferBeforeMin);
		Date to = cTo.getTime();
		
		return getLiveStreamEvents(calendars, from, to, true);
	}

	@Override
	public List<? extends LiveStreamEvent> getRunningAndPastEvents(CourseCalendars calendars, int bufferBeforeMin) {
		Date now = new Date();
		
		Date from = new GregorianCalendar(2000, 1, 1).getTime();
		
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(now);
		cTo.add(Calendar.MINUTE, bufferBeforeMin);
		Date to = cTo.getTime();
		
		return getLiveStreamEvents(calendars, from, to, false);
	}
	
	@Override
	public List<? extends LiveStreamEvent> getUpcomingEvents(CourseCalendars calendars, int bufferBeforeMin) {
		Date now = new Date();
		Calendar cFrom = Calendar.getInstance();
		cFrom.setTime(now);
		cFrom.add(Calendar.MINUTE, bufferBeforeMin);
		Date from = cFrom.getTime();
		Calendar cTo = Calendar.getInstance();
		cTo.setTime(now);
		cTo.add(Calendar.YEAR, 10);
		Date to = cTo.getTime();
		
		return getLiveStreamEvents(calendars, from, to, false).stream()
				.filter(notStartedFilter(from))
				.collect(Collectors.toList());
	}

	private Predicate<LiveStreamEvent> notStartedFilter(Date from) {
		return (LiveStreamEvent e) -> !e.getBegin().before(from);
	}

	private List<? extends LiveStreamEvent> getLiveStreamEvents(CourseCalendars calendars, Date from, Date to, boolean syncUrl) {
		List<LiveStreamEvent> liveStreamEvents = new ArrayList<>();
		for (KalendarRenderWrapper cal : calendars.getCalendars()) {
			if(cal != null) {
				boolean privateEventsVisible = cal.isPrivateEventsVisible();
				List<KalendarEvent> events = calendarManager.getEvents(cal.getKalendar(), from, to, privateEventsVisible);
				for(KalendarEvent event:events) {
					if(!privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_PRIVATE) {
						continue;
					}
					
					if (isLiveStream(event)) {
						boolean timeOnly = !privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_X_FREEBUSY;
						LiveStreamEventImpl liveStreamEvent = toLiveStreamEvent(event, timeOnly, syncUrl);
						liveStreamEvents.add(liveStreamEvent);
					}
				}
			}
		}
		
		return liveStreamEvents;
	}
	
	private boolean isLiveStream(KalendarEvent event) {
		return event.getLiveStreamUrl() != null;
	}

	private LiveStreamEventImpl toLiveStreamEvent(KalendarEvent event, boolean timeOnly, boolean syncUrl) {
		LiveStreamEventImpl liveStreamEvent = new LiveStreamEventImpl();
		liveStreamEvent.setId(event.getID());
		liveStreamEvent.setAllDayEvent(event.isAllDayEvent());
		liveStreamEvent.setBegin(event.getBegin());
		Date end = CalendarUtils.endOf(event);
		liveStreamEvent.setEnd(end);
		liveStreamEvent.setLiveStreamUrl(event.getLiveStreamUrl());
		if (syncUrl && event.getLiveStreamUrlTemplateKey() != null) {
			Long key = Long.valueOf(event.getLiveStreamUrlTemplateKey());
			UrlTemplate urlTemplate = urlTemplateDao.loadByKey(key);
			String concatUrls = concatUrls(urlTemplate);
			if (StringHelper.containsNonWhitespace(concatUrls)) {
				liveStreamEvent.setLiveStreamUrl(concatUrls);
			}
		}
		if (!timeOnly) {
			liveStreamEvent.setSubject(event.getSubject());
			liveStreamEvent.setDescription(event.getDescription());
			liveStreamEvent.setLocation(event.getLocation());
		}
		return liveStreamEvent;
	}

	@Override
	public void createLaunch(RepositoryEntry courseEntry, String subIdent, Identity identity) {
		launchDao.create(courseEntry, subIdent, identity, new Date());
	}

	@Override
	public Long getLaunchers(RepositoryEntryRef courseEntry, String subIdent, Date from, Date to) {
		return launchDao.getLaunchers(courseEntry, subIdent, from, to);
	}

	@Override
	public UrlTemplate createUrlTemplate(String name) {
		return urlTemplateDao.create(name);
	}

	@Override
	public UrlTemplate updateUrlTemplate(UrlTemplate urlTemplate) {
		return urlTemplateDao.update(urlTemplate);
	}

	@Override
	public List<UrlTemplate> getAllUrlTemplates() {
		return urlTemplateDao.loadAll();
	}

	@Override
	public UrlTemplate getUrlTemplate(Long key) {
		return urlTemplateDao.loadByKey(key);
	}

	@Override
	public void deleteUrlTemplate(UrlTemplate urlTemplate) {
		urlTemplateDao.delete(urlTemplate);
	}

	@Override
	public String concatUrls(UrlTemplate urlTemplate) {
		if (urlTemplate == null) return null;
		
		String urls = List.of(urlTemplate.getUrl1(), urlTemplate.getUrl2()).stream()
				.filter(StringHelper::containsNonWhitespace)
				.collect(Collectors.joining(liveStreamModule.getUrlSeparator()));
		return StringHelper.containsNonWhitespace(urls)? urls: null;
	}
	
	@Override
	public String[] splitUrl(String url) {
		if (!StringHelper.containsNonWhitespace(url)) return new String[0];
		
		String cleanedUrl = url.replace(" ", "");
		
		String urlSeparator = liveStreamModule.getUrlSeparator();
		return cleanedUrl.split(urlSeparator);
	}

	@Override
	public String[] getStreamingUrls(String[] urls) {
		if (urls == null || urls.length < 1) return urls;
		
		List<String> streamingUrls = new ArrayList<>(urls.length);
		for (String url : urls) {
			if (isStreaming(url)) {
				streamingUrls.add(url);
			}
		}
		
		String[] streaming = new String[streamingUrls.size()];
		streamingUrls.toArray(streaming);
		return streaming;
	}

	private boolean isStreaming(String url) {
		HttpGet request = new HttpGet(url);
		try(CloseableHttpClient httpclient = httpClientService.createHttpClient();
			CloseableHttpResponse response = httpclient.execute(request);) {
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				// Wrong WOWZA urls returns a html message instead of a video.
				Header contentType = response.getFirstHeader("Content-Type");
				if (contentType != null && !contentType.getValue().contains("html")) {
					return true;
				}
			}
		} catch(Exception e) {
			log.debug("LiveStream not available.", e);
		}
		
		return false;
	}
}
