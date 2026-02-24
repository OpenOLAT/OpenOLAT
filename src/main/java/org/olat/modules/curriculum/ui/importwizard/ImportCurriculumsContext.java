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

import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 6 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImportCurriculumsContext {

	private List<ImportedUserRow> importedUsersRows;
	private List<ImportedRow> importedElementsRows;
	private List<ImportedRow> importedCurriculumsRows;
	private List<ImportedMembershipRow> importedMembershipsRows;

	private final ImportCurriculumsFileReader reader;
	private final ImportCurriculumsValidator validator;
	private final ImportCurriculumsObjectsLoader loader;
	
	public ImportCurriculumsContext(Identity identity, Roles roles, Translator translator) {
		reader = new ImportCurriculumsFileReader(roles);
		loader = new ImportCurriculumsObjectsLoader(translator);
		validator = new ImportCurriculumsValidator(identity, roles, translator);
	}
	
	public List<UserPropertyHandler> getUserPropertyHandlers() {
		return validator.getUserPropertyHandlers();
	}
	
	public ImportCurriculumsObjectsLoader getLoader() {
		return loader;
	}

	public ImportCurriculumsValidator getValidator() {
		return validator;
	}
	
	public ImportCurriculumsFileReader getReader() {
		return reader;
	}
	
	public List<ImportedRow> getImportedCurriculumsRows() {
		return importedCurriculumsRows;
	}

	public void setImportedCurriculumsRows(List<ImportedRow> rows) {
		this.importedCurriculumsRows = rows;
	}

	public List<ImportedRow> getImportedElementsRows() {
		return importedElementsRows;
	}

	public void setImportedElementsRows(List<ImportedRow> rows) {
		this.importedElementsRows = rows;
	}
	
	public boolean hasImportedUsersPasswords() {
		if(importedUsersRows == null || importedUsersRows.isEmpty()) return false;
		return importedUsersRows.stream()
				.anyMatch(usr -> StringHelper.containsNonWhitespace(usr.getPassword()));
	}

	public List<ImportedUserRow> getImportedUsersRows() {
		return importedUsersRows;
	}

	public void setImportedUsersRows(List<ImportedUserRow> importedUsersRows) {
		this.importedUsersRows = importedUsersRows;
	}

	public List<ImportedMembershipRow> getImportedMembershipsRows() {
		return importedMembershipsRows;
	}

	public void setImportedMembershipsRows(List<ImportedMembershipRow> importedMembershipsRows) {
		this.importedMembershipsRows = importedMembershipsRows;
	}
	
	public void reset() {
		importedUsersRows = null;
		importedElementsRows = null;
		importedCurriculumsRows = null;
		importedMembershipsRows = null;
	}
}
