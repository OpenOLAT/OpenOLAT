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
package org.olat.course.archiver.restapi;

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Initial date: 27 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "courseArchiveVO")
public class CourseArchiveOptionsVO {
	
	private Boolean resultsWithPDFs;

	private Boolean logFiles;
	private Boolean courseResults;
	private Boolean courseChat;
	
	private Boolean logFilesAuthors;
	private Boolean logFilesUsers;
	private Boolean logFilesStatistics;
	private Date logFilesStartDate;
	private Date logFilesEndDate;
	
	private String title;
	private String filename;
	
	public CourseArchiveOptionsVO() {
		//
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
	
	public Boolean getResultsWithPDFs() {
		return resultsWithPDFs;
	}
	
	public void setResultsWithPDFs(Boolean resultsWithPDFs) {
		this.resultsWithPDFs = resultsWithPDFs;
	}
	
	public Boolean getLogFiles() {
		return logFiles;
	}
	
	public void setLogFiles(Boolean logFiles) {
		this.logFiles = logFiles;
	}
	
	public Boolean getCourseResults() {
		return courseResults;
	}
	
	public void setCourseResults(Boolean courseResults) {
		this.courseResults = courseResults;
	}
	
	public Boolean getCourseChat() {
		return courseChat;
	}
	
	public void setCourseChat(Boolean courseChat) {
		this.courseChat = courseChat;
	}

	public Boolean getLogFilesAuthors() {
		return logFilesAuthors;
	}

	public void setLogFilesAuthors(Boolean logFilesAuthors) {
		this.logFilesAuthors = logFilesAuthors;
	}

	public Boolean getLogFilesUsers() {
		return logFilesUsers;
	}

	public void setLogFilesUsers(Boolean logFilesUsers) {
		this.logFilesUsers = logFilesUsers;
	}

	public Boolean getLogFilesStatistics() {
		return logFilesStatistics;
	}

	public void setLogFilesStatistics(Boolean logFilesStatistics) {
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
}
