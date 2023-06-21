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
package org.olat.modules.todo.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTaskStatusStats;

/**
 * 
 * Initial date: 21 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskStatusStatsImpl implements ToDoTaskStatusStats {
	
	private final Map<ToDoStatus, Long> statusToCount = new HashMap<>(4);

	@Override
	public Long getToDoTaskCount(ToDoStatus status) {
		return statusToCount.getOrDefault(status, Long.valueOf(0));
	}

	@Override
	public Long getToDoTaskCount(Collection<ToDoStatus> status) {
		if (status == null || status.isEmpty()) {
			return Long.valueOf(0);
		}
		
		return status.stream()
				.map(s -> statusToCount.getOrDefault(s, Long.valueOf(0)))
				.mapToLong(Long::longValue)
				.sum();
	}
	
	public void put(ToDoStatus status, Long count) {
		statusToCount.put(status, count);
	}

}
