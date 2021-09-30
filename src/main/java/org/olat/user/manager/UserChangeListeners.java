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
package org.olat.user.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.logging.log4j.Logger;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 30 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserChangeListeners implements PostUpdateEventListener {

	private static final long serialVersionUID = 1666311938725742075L;
	
	private static final Logger log = Tracing.createLoggerFor(UserChangeListeners.class);
	
	private final ConcurrentMap<Long,List<ChangedEvent>> events = new ConcurrentHashMap<>();

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		return false;
	}
	
	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		int[] props = event.getDirtyProperties();
		if(props != null && props.length > 0) {
			Object entity = event.getEntity();
			if(entity instanceof User) {
				processUser(event, (User)entity);
			}
		}
	}
	
	private void processUser(PostUpdateEvent event, User user) {
		try {
			Long key = user.getKey();
			Object[] oldStates = event.getOldState();
			Object[] states = event.getState();

			List<UserPropertyHandler> handlers = UserManager.getInstance().getUserPropertiesConfig().getAllUserPropertyHandlers();
			Map<String,UserPropertyHandler> handlerMap = new HashMap<>();
			for(UserPropertyHandler handler:handlers) {
				handlerMap.put(handler.getName(), handler);
			}
			EntityPersister persister = event.getPersister();
			String[] propertyNames = persister.getPropertyNames();
			
			List<ChangedEvent> evs = new ArrayList<>();
			int[] props = event.getDirtyProperties();
			for(int prop:props) {
				Object oldState = oldStates[prop];
				Object state = states[prop];
				String propertyName = propertyNames[prop];
				evs.add(new ChangedEvent(propertyName, (String)oldState, (String)state));
			}
			events.put(key, evs);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public List<ChangedEvent> flushEvents(User user) {
		List<ChangedEvent> evs = events.remove(user.getKey());
		return evs == null ? List.of() : evs;
	}
	
	public static class ChangedEvent {
		
		private final String propertyName;
		private final String oldState;
		private final String state;
		
		public ChangedEvent(String propertyName, String oldState, String state) {
			this.propertyName = propertyName;
			this.oldState = oldState;
			this.state = state;
		}

		public String getPropertyName() {
			return propertyName;
		}

		public String getOldState() {
			return oldState;
		}

		public String getState() {
			return state;
		}
	}
}
