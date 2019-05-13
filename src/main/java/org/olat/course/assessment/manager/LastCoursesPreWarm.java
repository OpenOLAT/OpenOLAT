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
package org.olat.course.assessment.manager;

import java.util.List;

import org.olat.core.configuration.PreWarm;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.course.CourseFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LastCoursesPreWarm implements PreWarm {
	
	private static final Logger log = Tracing.createLoggerFor(LastCoursesPreWarm.class);
	
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;

	@Override
	public void run() {
		long start = System.nanoTime();
		List<RepositoryEntry> entries = repositoryEntryDao
				.getLastUsedRepositoryEntries("CourseModule", 0, 100);
		for(RepositoryEntry entry:entries) {
			CourseFactory.loadCourse(entry);
		}
		log.info(entries.size() + " Courses preloaded in (ms): " + CodeHelper.nanoToMilliTime(start));
	}
}
