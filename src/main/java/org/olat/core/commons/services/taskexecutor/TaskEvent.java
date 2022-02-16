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
package org.olat.core.commons.services.taskexecutor;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 15 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskEvent extends MultiUserEvent {

	private static final long serialVersionUID = 5353305241426349804L;

	public static final String TASK_DONE = "ex-task-done";
	public static final String TASK_STARTED = "ex-task-started";
	public static final String TASK_DELETED = "ex-task-deleted";
	
	private Long taskKey;
	
	public TaskEvent(String name, Long taskKey) {
		super(name);
		this.taskKey = taskKey;
	}
	
	public long getTaskKey() {
		return taskKey;
	}

	@Override
	public int hashCode() {
		return taskKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TaskEvent) {
			TaskEvent te = (TaskEvent)obj;
			return taskKey != null && taskKey.equals(te.taskKey)
					&& getCommand().equals(te.getCommand());
		}
		return false;
	}
}
