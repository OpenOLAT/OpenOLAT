/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy.manager.importexport;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.taxonomy.manager.importexport.FileMessage.Severity;

/**
 * Common part of an imported row: row number, status and validation messages.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public abstract class FileRow {

	public enum Status {
		/** The row creates a new object */
		created,
		/** The row changes an existing object */
		changed,
		/** The row matches an existing object without change */
		unchanged,
		/** The row has errors and cannot be imported */
		error
	}

	private int rowNum;
	private Status status;
	private final List<FileMessage> messages = new ArrayList<>(2);
	private final List<FileChange> changes = new ArrayList<>(2);

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return true if the row is imported: the row has no error
	 */
	public boolean isImportable() {
		return !hasErrors();
	}

	public List<FileMessage> getMessages() {
		return messages;
	}

	/**
	 * @return The values of the existing object which the import changes
	 */
	public List<FileChange> getChanges() {
		return changes;
	}
	
	public void addChange(String column, String language, String before, String after, boolean contentOnly) {
		changes.add(new FileChange(column, language, before, after, contentOnly));
	}

	public void addError(String column, String i18nKey, String... args) {
		messages.add(new FileMessage(Severity.error, column, i18nKey, args));
	}

	public void addWarning(String column, String i18nKey, String... args) {
		messages.add(new FileMessage(Severity.warning, column, i18nKey, args));
	}

	public boolean hasErrors() {
		return messages.stream().anyMatch(FileMessage::isError);
	}

	public boolean hasWarnings() {
		return messages.stream().anyMatch(message -> !message.isError());
	}
}
