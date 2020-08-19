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
package org.olat.modules.opencast;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 11 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OpencastEventProvider implements ListProvider {
	
	private static final Function<Object, String> EVENT_TITLE_STINGIFIER = new EventTitleStingifier();
	private static final int LIMIT = 15;

	private final String moreKey;
	private final AuthDelegate authDelegate;
	private List<OpencastEvent> events;
	
	private final OpencastService opencastService;

	public OpencastEventProvider(Identity identity, String moreKey) {
		this.moreKey = moreKey;
		opencastService = CoreSpringFactory.getImpl(OpencastService.class);
		authDelegate = opencastService.getAuthDelegate(identity);
	}

	@Override
	public int getMaxEntries() {
		return LIMIT;
	}

	@Override
	public void getResult(String searchValue, ListReceiver receiver) {
		// Opencast does not support wildcards in filters. So we load all events
		// and filter them here.
		if (events == null) {
			events = opencastService.getEvents(authDelegate);
		}
		
		List<OpencastEvent> matchingEvents = events.stream()
				.filter(new WildcardFilter(searchValue, EVENT_TITLE_STINGIFIER))
			.sorted((e1, e2) -> e1.getTitle().compareToIgnoreCase(e2.getTitle()))
			.collect(Collectors.toList());
		List<OpencastEvent> limitedEvents = matchingEvents.stream()
			.limit(LIMIT)
			.collect(Collectors.toList());
		limitedEvents.forEach(e -> receiver.addEntry(e.getIdentifier(), e.getTitle()));
		
		if (matchingEvents.size() > limitedEvents.size()) {
			receiver.addEntry(moreKey, moreKey);
		}
	}
	
	private static final class EventTitleStingifier implements Function<Object, String> {

		@Override
		public String apply(Object object) {
			if (object instanceof OpencastEvent) {
				return ((OpencastEvent)object).getTitle();
			}
			return null;
		}
		
	}

}
