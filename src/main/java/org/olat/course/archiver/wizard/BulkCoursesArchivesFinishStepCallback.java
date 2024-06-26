/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.archiver.wizard;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.DateUtils;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.archiver.CourseArchiveListController;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkCoursesArchivesFinishStepCallback implements StepRunnerCallback {
	
	private final BulkCoursesArchivesContext archiveContext;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private ExportManager exportManager;
	
	public BulkCoursesArchivesFinishStepCallback(BulkCoursesArchivesContext archiveContext) {
		CoreSpringFactory.autowireObject(this);
		this.archiveContext = archiveContext;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		List<RepositoryEntry> courseEntries = archiveContext.getEntries();
		CourseArchiveOptions options = archiveContext.getArchiveOptions();
		Date expirationDate = CalendarUtils.endOfDay(DateUtils.addDays(ureq.getRequestTimestamp(), courseModule.getCourseArchiveRetention()));

		for(RepositoryEntry courseEntry:courseEntries) {
			ArchiveType type = archiveContext.getArchiveTypesForEntries().get(courseEntry);
			OLATResource resource = courseEntry.getOlatResource();
			String title = CourseArchiveExportTask.getArchiveName(courseEntry, type, ureq.getLocale());
			String filename = CourseArchiveExportTask.getFilename(title);
			String description = CourseArchiveExportTask.getDescription(options, courseEntry, ureq.getLocale());
			CourseArchiveExportTask task = new CourseArchiveExportTask(archiveContext.getArchiveOptions(),
					OresHelper.clone(resource), ureq.getLocale());
			exportManager.startExport(task, title, description, filename,
					type, expirationDate, options.isOnlyAdministrators(), 
					courseEntry, CourseArchiveListController.COURSE_ARCHIVE_SUB_IDENT, ureq.getIdentity());
		}
		return StepsMainRunController.DONE_MODIFIED;
	}
}
