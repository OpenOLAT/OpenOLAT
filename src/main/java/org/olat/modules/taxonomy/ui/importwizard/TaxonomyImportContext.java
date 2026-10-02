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
package org.olat.modules.taxonomy.ui.importwizard;

import java.util.List;

import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyImportMode;
import org.olat.modules.taxonomy.manager.importexport.FileLevel;
import org.olat.modules.taxonomy.manager.importexport.FileLevelType;
import org.olat.modules.taxonomy.manager.importexport.TaxonomyFileData;

/**
 * Shared state of the steps of the taxonomy import wizard.
 * 
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyImportContext {

	private final Taxonomy target;
	private TaxonomyFileData data;
	private String identifier;
	private String displayName;
	private TaxonomyImportMode mode = TaxonomyImportMode.addNewAndUpdateExisting;

	/**
	 * @param target The taxonomy to import into or null to create a new taxonomy
	 */
	public TaxonomyImportContext(Taxonomy target) {
		this.target = target;
	}

	public boolean isNewTaxonomy() {
		return target == null;
	}

	public Taxonomy getTarget() {
		return target;
	}

	public TaxonomyFileData getData() {
		return data;
	}

	public void setData(TaxonomyFileData data) {
		if(this.data != null && this.data != data) {
			this.data.cleanUp();
		}
		this.data = data;
	}

	public List<FileLevelType> getLevelTypes() {
		return data == null ? List.of() : data.getLevelTypes();
	}

	public List<FileLevel> getLevels() {
		return data == null ? List.of() : data.getLevels();
	}

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

	public TaxonomyImportMode getMode() {
		return mode;
	}

	public void setMode(TaxonomyImportMode mode) {
		this.mode = mode;
	}

	public void cleanUp() {
		if(data != null) {
			data.cleanUp();
		}
	}
}
