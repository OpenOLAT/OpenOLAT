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
import java.util.List;

import org.olat.modules.opencast.OpencastEvent;
import org.olat.modules.opencast.OpencastService;
import org.olat.modules.opencast.manager.client.Api;
import org.olat.modules.opencast.manager.client.Event;
import org.olat.modules.opencast.manager.client.GetEventsParams;
import org.olat.modules.opencast.manager.client.OpencastRestClient;
import org.olat.modules.opencast.model.OpencastEventImpl;
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

	@Override
	public boolean checkApiConnection() {
		Api api = opencastRestClient.getApi();
		return api != null && api.getVersion() != null;
	}

	@Override
	public List<OpencastEvent> getEvents(String identifier) {
		GetEventsParams params = new GetEventsParams();
		params.getFilter().setTextFilter(identifier);
		Event[] events = opencastRestClient.getEvents(params);
		List<OpencastEvent> opencastEvents = new ArrayList<>(events.length);
		for (Event event : events) {
			OpencastEventImpl opencastEvent = new OpencastEventImpl();
			opencastEvent.setIdentifier(event.getIdentifier());
			opencastEvent.setTitle(event.getTitle());
			opencastEvent.setStart(event.getStart());
			// End has to be calculated with the duration, but the duration of the event is always 0.
			// Only the duration of the metadata would be the right value. We skip that for now.
			opencastEvents.add(opencastEvent);
		}
		return opencastEvents;
	}

	@Override
	public boolean deleteEvents(String identifier) {
		return opencastRestClient.deleteEvent(identifier);
	}
}
