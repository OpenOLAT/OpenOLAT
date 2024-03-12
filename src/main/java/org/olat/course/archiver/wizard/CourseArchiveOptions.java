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

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.course.archiver.ExportFormat;
import org.olat.course.archiver.wizard.CourseArchiveContext.LogSettings;

/**
 * 
 * Initial date: 20 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseArchiveOptions {
	
	private LogSettings logSettings;
	private ArchiveType archiveType;
	private List<String> courseNodesIdents;
	private boolean resultsWithPDFs;
	
	private boolean customize;
	private boolean itemColumns;
	private boolean pointColumn;
	private boolean timeColumns;
	private boolean commentColumn;
	
	private boolean logFiles;
	private boolean courseResults;
	private boolean courseChat;
	
	private boolean logFilesAuthors;
	private boolean logFilesUsers;
	private boolean logFilesStatistics;
	private Date logFilesStartDate;
	private Date logFilesEndDate;
	
	private String title;
	private String filename;

	public CourseArchiveOptions() {
		//
	}
	
	public boolean isOnlyAdministrators() {
		return isLogFiles() && isLogFilesUsers();
	}
	
	public ArchiveType getArchiveType() {
		return archiveType;
	}

	public void setArchiveType(ArchiveType archiveType) {
		this.archiveType = archiveType;
	}
	
	
	public List<String> getCourseNodesIdents() {
		return courseNodesIdents;
	}

	public void setCourseNodesIdents(List<String> idents) {
		this.courseNodesIdents = idents;
	}

	public LogSettings getLogSettings() {
		return logSettings;
	}

	public void setLogSettings(LogSettings logSettings) {
		this.logSettings = logSettings;
	}

	public boolean isResultsWithPDFs() {
		if(customize) {
			return resultsWithPDFs;
		}
		return false;
	}

	public void setResultsWithPDFs(boolean resultsWithPDFs) {
		this.resultsWithPDFs = resultsWithPDFs;
	}

	public boolean isCustomize() {
		return customize;
	}

	public void setCustomize(boolean customize) {
		this.customize = customize;
	}
	
	public ExportFormat getQTI21ExportFormat() {
		if(customize) {
			return new ExportFormat(itemColumns, false, pointColumn, timeColumns, commentColumn);
		}
		return new ExportFormat(true, false, true, true, true);
	}

	public boolean isItemColumns() {
		return itemColumns;
	}

	public void setItemColumns(boolean itemColumns) {
		this.itemColumns = itemColumns;
	}

	public boolean isPointColumn() {
		return pointColumn;
	}

	public void setPointColumn(boolean pointColumn) {
		this.pointColumn = pointColumn;
	}

	public boolean isTimeColumns() {
		return timeColumns;
	}

	public void setTimeColumns(boolean timeColumns) {
		this.timeColumns = timeColumns;
	}

	public boolean isCommentColumn() {
		return commentColumn;
	}

	public void setCommentColumn(boolean commentColumn) {
		this.commentColumn = commentColumn;
	}

	public boolean isLogFiles() {
		return logFiles;
	}

	public void setLogFiles(boolean logFiles) {
		this.logFiles = logFiles;
	}

	public boolean isCourseResults() {
		return courseResults;
	}

	public void setCourseResults(boolean courseResults) {
		this.courseResults = courseResults;
	}

	public boolean isCourseChat() {
		return courseChat;
	}

	public void setCourseChat(boolean courseChat) {
		this.courseChat = courseChat;
	}

	public boolean isLogFilesAuthors() {
		return logFilesAuthors;
	}

	public void setLogFilesAuthors(boolean logFilesAuthors) {
		this.logFilesAuthors = logFilesAuthors;
	}

	public boolean isLogFilesUsers() {
		return logFilesUsers;
	}

	public void setLogFilesUsers(boolean logFilesUsers) {
		this.logFilesUsers = logFilesUsers;
	}

	public boolean isLogFilesStatistics() {
		return logFilesStatistics;
	}

	public void setLogFilesStatistics(boolean logFilesStatistics) {
		this.logFilesStatistics = logFilesStatistics;
	}

	public Date getLogFilesStartDate() {
		return logFilesStartDate;
	}

	public void setLogFilesStartDate(Date logFilesStartDate) {
		this.logFilesStartDate = logFilesStartDate;
	}

	public Date getLogFilesEndDate() {
		return logFilesEndDate;
	}

	public void setLogFilesEndDate(Date logFilesEndDate) {
		this.logFilesEndDate = logFilesEndDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
