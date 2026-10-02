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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * One row of the levels sheet.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class FileLevel extends FileRow {

	private String path;
	private String identifier;
	private String externalId;
	private String type;
	private String sortOrder;
	private String managedFlags;
	private FileMedia teaserImage;
	private FileMedia backgroundImage;
	/** Language key to title and description, in the order of the file */
	private final Map<String,FileTranslation> translations = new LinkedHashMap<>();

	private TaxonomyLevel existingLevel;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<String> getPathSegments() {
		return TaxonomyFileFormat.splitPath(path);
	}

	/**
	 * @return The normalized path of the parent level, "/" for a root level
	 */
	public String getParentPath() {
		List<String> segments = getPathSegments();
		if(segments.size() <= 1) {
			return "/";
		}
		return TaxonomyFileFormat.toPath(segments.subList(0, segments.size() - 1));
	}

	/**
	 * @return The path rebuilt from its segments, to compare paths independently
	 * 		of missing leading or trailing slashes.
	 */
	public String getNormalizedPath() {
		return TaxonomyFileFormat.toPath(getPathSegments());
	}

	/**
	 * @return The path with the unescaped identifiers, as used by the media
	 * 		folders of the format version 1.
	 */
	public String getNormalizedLegacyPath() {
		return "/" + String.join("/", getPathSegments()) + "/";
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getManagedFlags() {
		return managedFlags;
	}

	public void setManagedFlags(String managedFlags) {
		this.managedFlags = managedFlags;
	}

	public FileMedia getTeaserImage() {
		return teaserImage;
	}

	public void setTeaserImage(FileMedia teaserImage) {
		this.teaserImage = teaserImage;
	}

	public FileMedia getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(FileMedia backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public Map<String, FileTranslation> getTranslations() {
		return translations;
	}

	public FileTranslation getTranslation(String languageKey) {
		return translations.get(languageKey);
	}

	public void putTranslation(String languageKey, String title, String description) {
		translations.put(languageKey, new FileTranslation(title, description));
	}

	public TaxonomyLevel getExistingLevel() {
		return existingLevel;
	}

	public void setExistingLevel(TaxonomyLevel existingLevel) {
		this.existingLevel = existingLevel;
	}
}
