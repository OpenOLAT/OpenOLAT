/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
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
package org.olat.modules.todo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.modules.todo.manager.ToDoMailing;

/**
 *
 * Collects the to-do assignments of one operation, grouped by recipient, and sends
 * one digest mail per recipient on {@link #sendMails()}.
 *
 * Initial date: 22 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ToDoAssignedMailBatch implements ToDoAssignedMailer {
	
	public record Entry(Identity doer, ToDoTask toDoTask, ToDoProvider provider) {}
	
	private final Map<Long, List<Entry>> recipientKeyToEntries = new LinkedHashMap<>();
	private final Set<String> deduplication = new HashSet<>();
	
	@Override
	public void onAssigned(Identity doer, IdentityRef recipient, ToDoTask toDoTask, ToDoProvider provider) {
		if (!deduplication.add(recipient.getKey() + "-" + toDoTask.getKey())) {
			return;
		}
		recipientKeyToEntries.computeIfAbsent(recipient.getKey(), key -> new ArrayList<>())
				.add(new Entry(doer, toDoTask, provider));
	}
	
	public boolean isEmpty() {
		return recipientKeyToEntries.isEmpty();
	}
	
	public Map<Long, List<Entry>> getAssignments() {
		return recipientKeyToEntries;
	}
	
	public void sendMails() {
		CoreSpringFactory.getImpl(ToDoMailing.class).sendAssignedEmails(this);
	}
	
}
