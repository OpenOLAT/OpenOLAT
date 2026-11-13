/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.manager;

import java.util.Date;
import java.util.Objects;
import java.util.Set;

import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.olat.user.UserImpl;
import org.olat.user.UserPropertiesConfig;

/**
 * Hibernate pre-update listener which maintains the last modified date
 * of the user. The date is only updated if at least one property backed
 * by a user property handler really changed. Technical fields (preferences,
 * portrait, logo, initials colour, e-mail change key, e-mail disabled) are ignored.
 * <p>
 * The new date is written in the state array (which is used to build
 * the SQL update) and in the entity. This only works because UserImpl
 * doesn't use dynamic updates.
 *
 * Initial date: 13 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserLastModifiedListener implements PreUpdateEventListener {

	private static final String LAST_MODIFIED = "lastModified";
	/**
	 * Properties with a handler but which are technical (e-mail change process)
	 */
	private static final Set<String> IGNORED_PROPERTIES = Set.of("emchangeKey", "emailDisabled");

	private final UserPropertiesConfig userPropertiesConfig;

	public UserLastModifiedListener(UserPropertiesConfig userPropertiesConfig) {
		this.userPropertiesConfig = userPropertiesConfig;
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {
		if(event.getEntity() instanceof UserImpl user) {
			String[] propertyNames = event.getPersister().getPropertyNames();
			int lastModifiedIndex = indexOf(propertyNames, LAST_MODIFIED);
			if(lastModifiedIndex < 0) {
				return false;
			}

			Object[] states = event.getState();
			Object[] oldStates = event.getOldState();
			if(oldStates == null || hasPropertyChanges(propertyNames, oldStates, states)) {
				setLastModified(user, states, lastModifiedIndex, new Date());
			} else if(oldStates[lastModifiedIndex] instanceof Date oldLastModified
					&& (!(states[lastModifiedIndex] instanceof Date lastModified) || lastModified.before(oldLastModified))) {
				// A merged, outdated copy of the user cannot move the date back
				setLastModified(user, states, lastModifiedIndex, oldLastModified);
			}
		}
		return false;
	}

	private boolean hasPropertyChanges(String[] propertyNames, Object[] oldStates, Object[] states) {
		for(int i=0; i<propertyNames.length; i++) {
			if(!equalsState(oldStates[i], states[i])
					&& !IGNORED_PROPERTIES.contains(propertyNames[i])
					&& UserChangesListener.observeProperty(propertyNames[i], userPropertiesConfig)) {
				return true;
			}
		}
		return false;
	}

	private void setLastModified(UserImpl user, Object[] states, int index, Date date) {
		states[index] = date;
		user.setLastModified(date);
	}

	/**
	 * The property handlers save empty strings as null, the setters
	 * of UserImpl not. Both are considered as equal.
	 */
	private static boolean equalsState(Object oldState, Object state) {
		return Objects.equals(normalize(oldState), normalize(state));
	}

	private static Object normalize(Object state) {
		if(state instanceof String string && string.isEmpty()) {
			return null;
		}
		return state;
	}

	private static int indexOf(String[] propertyNames, String name) {
		for(int i=0; i<propertyNames.length; i++) {
			if(name.equals(propertyNames[i])) {
				return i;
			}
		}
		return -1;
	}
}
