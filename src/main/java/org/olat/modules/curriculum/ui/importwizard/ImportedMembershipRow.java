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
package org.olat.modules.curriculum.ui.importwizard;

import java.time.LocalDateTime;

/**
 * 
 * Initial date: 20 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportedMembershipRow extends AbstractImportRow {

	private final String curriculumIdentifier;
	private final String implementationIdentifier;
	private final String identifier;
	private final String role;
	private final String username;
	private final LocalDateTime registrationDate;
	private final LocalDateTime lastModified;
	
	private ImportedRow curriculumRow;
	private ImportedRow implementationRow;
	private ImportedRow elementRow;
	
	private ImportedUserRow userRow;
	
	public ImportedMembershipRow(int rowNum, String curriculumIdentifier, String implementationIdentifier,
			String identifier, String role, String username,
			LocalDateTime registrationDate, LocalDateTime lastModified) {
		super(rowNum);
		this.curriculumIdentifier = curriculumIdentifier;
		this.implementationIdentifier = implementationIdentifier;
		this.identifier = identifier;
		this.role = role;
		this.username = username;
		this.registrationDate = registrationDate;
		this.lastModified = lastModified;
	}

	public String getCurriculumIdentifier() {
		return curriculumIdentifier;
	}

	public ImportedRow getCurriculumRow() {
		return curriculumRow;
	}

	public void setCurriculumRow(ImportedRow curriculumRow) {
		this.curriculumRow = curriculumRow;
	}

	public String getImplementationIdentifier() {
		return implementationIdentifier;
	}

	public ImportedRow getImplementationRow() {
		return implementationRow;
	}

	public void setImplementationRow(ImportedRow implementationRow) {
		this.implementationRow = implementationRow;
	}

	public String getIdentifier() {
		return identifier;
	}

	public ImportedRow getElementRow() {
		return elementRow;
	}

	public void setElementRow(ImportedRow elementRow) {
		this.elementRow = elementRow;
	}

	public String getRole() {
		return role;
	}

	public String getUsername() {
		return username;
	}

	public ImportedUserRow getUserRow() {
		return userRow;
	}

	public void setUserRow(ImportedUserRow userRow) {
		this.userRow = userRow;
	}

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}
}
