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
package org.olat.group.ui.edit;

import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupRepositoryEntryEvent extends MultiUserEvent {

	private static final long serialVersionUID = 4794247345720510726L;
	
	public static final String REPOSITORY_ENTRY_ADDED = "repository.entry.added.event";
	
	private final Long groupKey;
	private final Long entryKey;
	
	private BusinessGroupRepositoryEntryEvent(String command, Long groupKey, Long entryKey) {
		super(command);
		this.groupKey = groupKey;
		this.entryKey = entryKey;
	}

	public Long getGroupKey() {
		return groupKey;
	}

	public Long getEntryKey() {
		return entryKey;
	}

	public static void fireEvents(String command, BusinessGroup group, RepositoryEntry entry) {
		BusinessGroupRepositoryEntryEvent event = new BusinessGroupRepositoryEntryEvent(command, group.getKey(),entry.getKey());
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		// 1) notify listeners of group event
		eventBus.fireEventToListenersOf(event, group);
		// 2) notify listeners of the repository entry events
		eventBus.fireEventToListenersOf(event, entry);
	}
}
