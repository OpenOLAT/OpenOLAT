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
package org.olat.modules.opencast.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.modules.opencast.AuthDelegate;
import org.olat.modules.opencast.AuthDelegate.Type;
import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastModule;
import org.olat.modules.opencast.OpencastSeries;
import org.olat.modules.opencast.OpencastService;
import org.olat.modules.opencast.manager.client.Api;
import org.olat.modules.opencast.manager.client.Event;
import org.olat.modules.opencast.manager.client.GetEventsParams;
import org.olat.modules.opencast.manager.client.GetEventsParams.Filter;
import org.olat.modules.opencast.manager.client.GetSeriesParams;
import org.olat.modules.opencast.manager.client.OpencastRestClient;
import org.olat.modules.opencast.manager.client.Series;
import org.olat.modules.opencast.model.OpencastEventImpl;
import org.olat.modules.opencast.model.OpencastLtiContext;
import org.olat.modules.opencast.model.OpencastSeriesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OpencastServiceImpl implements OpencastService {
	
	@Autowired
	private OpencastRestClient opencastRestClient;
	@Autowired
	private OpencastModule opencastModule;
	@Autowired
	private LTIManager ltiManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private BaseSecurityManager securityManager;

	@Override
	public boolean checkApiConnection() {
		Api api = opencastRestClient.getApi();
		return api != null && api.getVersion() != null;
	}

	@Override
	public OpencastEvent getEvent(String identifier) {
		Event event = opencastRestClient.getEvent(identifier);
		return event != null? toOpencastEvent(event): null;
	}

	@Override
	public List<OpencastEvent> getEvents(AuthDelegate authDelegate) {
		GetEventsParams params = GetEventsParams.builder()
				.setAuthDelegate(authDelegate)
				.build();
		return getEvents(params);
	}

	@Override
	public List<OpencastEvent> getEvents(String metadata, boolean publishedOnly) {
		GetEventsParams params = GetEventsParams.builder()
				.addFilter(Filter.textFilter, metadata)
				.build();
		List<OpencastEvent> events = getEvents(params);
		if (publishedOnly) {
			events = events.stream()
					.filter(event -> opencastRestClient.isEpisodeExisting(event.getIdentifier()))
					.collect(Collectors.toList());
		}
		return events;
	}

	private List<OpencastEvent> getEvents(GetEventsParams params) {
		Event[] events = opencastRestClient.getEvents(params);
		return toOpencastEvents(events);
	}

	private List<OpencastEvent> toOpencastEvents(Event[] events) {
		List<OpencastEvent> opencastEvents = new ArrayList<>(events.length);
		for (Event event : events) {
			OpencastEvent opencastEvent = toOpencastEvent(event);
			opencastEvents.add(opencastEvent);
		}
		return opencastEvents;
	}

	private OpencastEvent toOpencastEvent(Event event) {
		OpencastEventImpl opencastEvent = new OpencastEventImpl();
		opencastEvent.setIdentifier(event.getIdentifier());
		opencastEvent.setTitle(event.getTitle());
		opencastEvent.setDescription(event.getDescription());
		opencastEvent.setCreator(event.getCreator());
		opencastEvent.setPresenters(Arrays.stream(event.getPresenter()).collect(Collectors.toList()));
		opencastEvent.setStart(event.getStart());
		opencastEvent.setSeries(event.getSeries());
		// End has to be calculated with the duration, but the duration of the event is always 0.
		// Only the duration of the metadata would be the right value. We skip that for now.
		return opencastEvent;
	}

	@Override
	public boolean deleteEvents(String identifier) {
		return opencastRestClient.deleteEvent(identifier);
	}

	@Override
	public OpencastSeries getSeries(String identifier) {
		Series series = opencastRestClient.getSeries(identifier);
		return series != null? toOpencastSeries(series): null;
	}

	@Override
	public List<OpencastSeries> getSeries(AuthDelegate authDelegate) {
		GetSeriesParams params = GetSeriesParams.builder()
				.setAuthDelegate(authDelegate)
				.build();
		return getSeries(params);
	}
	
	private List<OpencastSeries> getSeries(GetSeriesParams params) {
		Series[] series = opencastRestClient.getSeries(params);
		return toOpencastSeries(series);
	}
	
	private List<OpencastSeries> toOpencastSeries(Series[] series) {
		List<OpencastSeries> opencastSeriesLIst = new ArrayList<>(series.length);
		for (Series event : series) {
			OpencastSeries opencastSeries = toOpencastSeries(event);
			opencastSeriesLIst.add(opencastSeries);
		}
		return opencastSeriesLIst;
	}

	private OpencastSeries toOpencastSeries(Series series) {
		OpencastSeriesImpl opencastSeries = new OpencastSeriesImpl();
		opencastSeries.setIdentifier(series.getIdentifier());
		opencastSeries.setTitle(series.getTitle());
		opencastSeries.setDescription(series.getDescription());
		opencastSeries.setContributors(Arrays.stream(series.getContributors()).collect(Collectors.toList()));
		opencastSeries.setSubjects(Arrays.stream(series.getSubjects()).collect(Collectors.toList()));
		return opencastSeries;
	}

	@Override
	public AuthDelegate getAuthDelegate(Identity identity) {
		Type type = opencastModule.getAuthDelegateType();
		String value = null;
		switch (type) {
		case User: value = getUserId(identity); break;
		case Roles: value = opencastModule.getAuthDelegateRoles(); break;
		default: //
		}
		return AuthDelegate.of(type, value);
	}

	@Override
	public String getUserId(Identity identity) {
		return securityManager.findAuthenticationName(identity);
	}

	@Override
	public String getLtiEventMapperUrl(UserSession usess, String identifier, String roles) {
		String tool = "play/" + identifier;
		return getLtiMapperUrl(usess, tool, roles);
	}

	@Override
	public String getLtiSeriesMapperUrl(UserSession usess, OpencastSeries opencastSeries, String roles) {
		String tool = "ltitools/series/index.html?series=" + opencastSeries.getIdentifier();
		return getLtiMapperUrl(usess, tool, roles);
	}

	private String getLtiMapperUrl(UserSession usess, String tool, String roles) {
		LTIContext context = new OpencastLtiContext(tool, roles);
		Map<String,String> unsignedProps = ltiManager.forgeLTIProperties(usess.getIdentity(), usess.getLocale(), context, false, false, true);
		Mapper contentMapper = new PostDataMapper(unsignedProps, opencastModule.getLtiUrl(),
				opencastModule.getLtiSignUrl(), opencastModule.getLtiKey(), opencastModule.getLtiSecret(), false);
		return mapperService.register(usess, contentMapper).getUrl();
	}
}
