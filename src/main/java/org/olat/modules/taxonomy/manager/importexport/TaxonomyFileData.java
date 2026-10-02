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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.util.FileUtils;
import org.olat.modules.taxonomy.manager.importexport.FileMessage.Severity;

/**
 * The content of an import or export file: the taxonomy metadata, the level types and
 * the levels. The exporter fills it from the database, the reader from a
 * file.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyFileData {

	private String identifier;
	private String displayName;
	private String description;
	private String externalId;
	private String managedFlags;

	/** Information rows of the export (key to value), not imported */
	private final Map<String,String> exportInformation = new LinkedHashMap<>();
	/** Language keys of the language groups, in the order of the columns */
	private final List<String> languages = new ArrayList<>();
	private final List<FileLevelType> levelTypes = new ArrayList<>();
	private final List<FileLevel> levels = new ArrayList<>();
	/** Messages about the file itself (format, missing sheets, ...) */
	private final List<FileMessage> messages = new ArrayList<>();

	private int formatVersion = TaxonomyFileFormat.FORMAT_VERSION;
	private File workingDirectory;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public Map<String, String> getExportInformation() {
		return exportInformation;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public List<FileLevelType> getLevelTypes() {
		return levelTypes;
	}

	public FileLevelType getLevelType(String typeIdentifier) {
		if(typeIdentifier == null) return null;
		return levelTypes.stream()
				.filter(type -> typeIdentifier.equals(type.getIdentifier()))
				.findFirst().orElse(null);
	}

	public List<FileLevel> getLevels() {
		return levels;
	}

	public List<FileMessage> getMessages() {
		return messages;
	}

	public void addError(String i18nKey, String... args) {
		messages.add(new FileMessage(Severity.error, null, i18nKey, args));
	}

	public void addWarning(String i18nKey, String... args) {
		messages.add(new FileMessage(Severity.warning, null, i18nKey, args));
	}

	public boolean hasErrors() {
		return messages.stream().anyMatch(FileMessage::isError);
	}

	public int getFormatVersion() {
		return formatVersion;
	}

	public void setFormatVersion(int formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/**
	 * Delete the extracted media files.
	 */
	public void cleanUp() {
		if(workingDirectory != null && workingDirectory.exists()) {
			FileUtils.deleteDirsAndFiles(workingDirectory, true, true);
		}
		workingDirectory = null;
	}
}
