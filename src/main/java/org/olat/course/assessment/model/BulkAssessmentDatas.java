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
package org.olat.course.assessment.model;

import java.io.Serializable;
import java.util.List;

import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 20.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentDatas implements Serializable {

	private static final long serialVersionUID = 8109609348537626355L;
	
	private List<BulkAssessmentRow> rows;
	private String returnFiles;
	private String dataBackupFile;
	private Boolean visibility;
	private Boolean acceptSubmission;
	private AssessmentEntryStatus status;
	private BulkAssessmentColumnSettings columnsSettings;

	public BulkAssessmentColumnSettings getColumnsSettings() {
		return columnsSettings;
	}

	public void setColumnsSettings(BulkAssessmentColumnSettings columnsSettings) {
		this.columnsSettings = columnsSettings;
	}

	public int getRowsSize() {
		return rows == null ? 0 : rows.size();
	}
	
	public List<BulkAssessmentRow> getRows() {
		return rows;
	}

	public void setRows(List<BulkAssessmentRow> rows) {
		this.rows = rows;
	}

	/**
	 * The path to a ZIP file containing the returned files.
	 * 
	 * @return The path the ZIP file
	 */
	public String getReturnFiles() {
		return returnFiles;
	}

	public void setReturnFiles(String returnFiles) {
		this.returnFiles = returnFiles;
	}

	/**
	 * The data, user, passed/failed are saved as a CSV file
	 * at the path specified here.
	 * 
	 * @return The path to the data as a file.
	 */
	public String getDataBackupFile() {
		return dataBackupFile;
	}

	public void setDataBackupFile(String dataBackupFile) {
		this.dataBackupFile = dataBackupFile;
	}

	public Boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(Boolean visibility) {
		this.visibility = visibility;
	}

	public AssessmentEntryStatus getStatus() {
		return status;
	}

	public void setStatus(AssessmentEntryStatus status) {
		this.status = status;
	}

	public Boolean getAcceptSubmission() {
		return acceptSubmission;
	}

	public void setAcceptSubmission(Boolean acceptSubmission) {
		this.acceptSubmission = acceptSubmission;
	}
}
