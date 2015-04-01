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
package org.olat.course.nodes.gta;

/**
 * 
 * Initial date: 05.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentResponse {
	
	public static final AssignmentResponse ERROR = new AssignmentResponse(Status.error);
	public static final AssignmentResponse ALREADY_ASSIGNED = new AssignmentResponse(Status.alreadyAssigned);
	public static final AssignmentResponse NO_MORE_TASKS = new AssignmentResponse(Status.noMoreTasks);
	
	private final Task task;
	private final Status status;
	
	public AssignmentResponse(Status status) {
		this(null, status);
	}
	
	public AssignmentResponse(Task task, Status status) {
		this.task = task;
		this.status = status;
	}
	
	public Task getTask() {
		return task;
	}

	public Status getStatus() {
		return status;
	}

	public enum Status {
		ok,
		error,
		alreadyAssigned,
		noMoreTasks
	}
}