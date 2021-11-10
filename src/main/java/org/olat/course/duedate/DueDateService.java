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
package org.olat.course.duedate;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.group.BusinessGroupRef;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DueDateService {
	
	public static final String TYPE_COURSE_START = "courseStart"; // relative to course start defined by a life-cycle
	public static final String TYPE_COURSE_LAUNCH = "courseLaunch"; // relative to the course launch by a user
	public static final String TYPE_ENROLLMENT ="enrollment"; //relative to the enrollment date
	
	public List<String> getCourseRelativeToDateTypes(RepositoryEntry courseEntry);
	
	public Date getDueDate(DueDateConfig config, RepositoryEntry courseEntry, IdentityRef identity);

	public Date getRelativeDate(RelativeDueDateConfig config, RepositoryEntry courseEntry, IdentityRef identity);
	
	public Date getRelativeDate(RelativeDueDateConfig config, RepositoryEntry courseEntry, BusinessGroupRef businessGroup);
	
	public Date addNumOfDays(Date date, int numOfDays);

}
