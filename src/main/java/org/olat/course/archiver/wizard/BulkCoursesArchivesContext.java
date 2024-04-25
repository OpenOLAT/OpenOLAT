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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.id.Roles;
import org.olat.course.archiver.wizard.CourseArchiveContext.LogSettings;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkCoursesArchivesContext {
	
	private final List<RepositoryEntry> entries;
	private CourseArchiveOptions archiveOptions;
	private final Map<RepositoryEntry,ArchiveType> archiveTypesMap = new HashMap<>();
	
	public BulkCoursesArchivesContext(CourseArchiveOptions archiveOptions, List<RepositoryEntry> entries) {
		this.entries = entries;
		this.archiveOptions = archiveOptions;
	}
	
	public static final BulkCoursesArchivesContext defaultValues(List<RepositoryEntry> entries, Roles roles) {
		CourseArchiveOptions options = new CourseArchiveOptions();
		options.setArchiveType(ArchiveType.COMPLETE);
		options.setLogSettings(LogSettings.ANONYMOUS);
		
		options.setCustomize(false);
		options.setItemColumns(true);
		options.setPointColumn(true);
		options.setTimeColumns(true);
		options.setCommentColumn(true);
		options.setResultsWithPDFs(false);
		
		options.setLogFiles(true);
		options.setCourseResults(true);
		options.setCourseChat(true);
		
		boolean isAdministrator = roles.isAdministrator() || roles.isLearnResourceManager();
		options.setLogFilesAuthors(true);
		options.setLogFilesUsers(isAdministrator);
		options.setLogFilesStatistics(!isAdministrator);
	
		return new BulkCoursesArchivesContext(options, entries);
	}
	
	public List<RepositoryEntry> getEntries() {
		return entries;
	}
	
	public CourseArchiveOptions getArchiveOptions() {
		return archiveOptions;
	}
	
	public Map<RepositoryEntry,ArchiveType> getArchiveTypesForEntries() {
		return archiveTypesMap;
	}
	
	public void addArchiveTypeFor(ArchiveType type, RepositoryEntry entry) {
		archiveTypesMap.put(entry, type);
	}
}
