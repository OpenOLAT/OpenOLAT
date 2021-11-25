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
package org.olat.repository.ui.author;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl.SelectionMode;

/**
 * 
 * Initial date: 4 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuthorListConfiguration {
	
	private final String tableId;
	private String i18nKeyTitle;
	private boolean importRessources = true;
	private boolean createRessources = true;
	private boolean helpCenter = true;
	private boolean tools = true;
	private boolean infos = false;
	private SelectionMode selectRepositoryEntries = SelectionMode.disabled;
	private List<String> allowedRessourceTypes;
	
	private boolean defaultAccessControl = true;
	private boolean defaultTaxonomyPath = true;
	private boolean defaultGuest = true;
	private boolean defaultExternalRef = false;
	private boolean defaultBookmark = true;
	private boolean defaultIconType = true;
	
	private AuthorListConfiguration(String tableId) {
		this.tableId = tableId;
	}
	
	public static final AuthorListConfiguration allEnabled() {
		AuthorListConfiguration config = new AuthorListConfiguration("authors-list-v2");
		config.setI18nKeyTitle("author.title");
		return config;
	}
	
	public static final AuthorListConfiguration selectRessource(String tableId, String ressourceType) {
		AuthorListConfiguration config = new AuthorListConfiguration(tableId);
		config.setImportRessources(true);
		config.setCreateRessources(true);
		config.setHelpCenter(false);
		config.setAllowedResourceTypes(List.of(ressourceType));
		config.setTools(false);
		config.setSelectRepositoryEntry(SelectionMode.single);
		config.setInfos(true);
		config.setDefaultAccessControl(false);
		config.setDefaultGuest(false);
		config.setDefaultTaxonomyPath(false);
		config.setDefaultExternalRef(true);
		config.setDefaultIconType(false);
		config.setDefaultBookmark(false);
		return config;
	}
	
	public String getTableId() {
		return tableId;
	}
	
	public String getI18nKeyTitle() {
		return i18nKeyTitle;
	}

	public void setI18nKeyTitle(String i18nKeyTitle) {
		this.i18nKeyTitle = i18nKeyTitle;
	}
	
	public boolean isImportRessources() {
		return importRessources;
	}
	
	public void setImportRessources(boolean importRessources) {
		this.importRessources = importRessources;
	}
	
	public boolean isCreateRessources() {
		return createRessources;
	}
	
	public void setCreateRessources(boolean createRessources) {
		this.createRessources = createRessources;
	}
	
	public boolean isHelpCenter() {
		return helpCenter;
	}

	public void setHelpCenter(boolean helpCenter) {
		this.helpCenter = helpCenter;
	}

	public boolean isTools() {
		return tools;
	}

	public void setTools(boolean tools) {
		this.tools = tools;
	}

	public boolean isInfos() {
		return infos;
	}

	public void setInfos(boolean infos) {
		this.infos = infos;
	}

	public boolean isSelectRepositoryEntries() {
		return selectRepositoryEntries != null && selectRepositoryEntries != SelectionMode.disabled;
	}
	
	public SelectionMode getSelectRepositoryEntries() {
		return selectRepositoryEntries;
	}

	public void setSelectRepositoryEntry(SelectionMode selectRepositoryEntries) {
		this.selectRepositoryEntries = selectRepositoryEntries;
	}
	
	public boolean isOnlyAllowedResourceType(String resourceType) {
		return allowedRessourceTypes != null && allowedRessourceTypes.size() == 1
				&& allowedRessourceTypes.contains(resourceType);
	}

	public List<String> getAllowedResourceTypes() {
		return allowedRessourceTypes;
	}
	
	public void setAllowedResourceTypes(List<String> allowedRessourceTypes) {
		this.allowedRessourceTypes = allowedRessourceTypes;
	}
	
	public boolean isResourceTypeAllowed(String type) {
		return allowedRessourceTypes == null || allowedRessourceTypes.isEmpty()
				|| allowedRessourceTypes.contains(type);
	}

	public boolean isDefaultAccessControl() {
		return defaultAccessControl;
	}

	public void setDefaultAccessControl(boolean defaultAccessControl) {
		this.defaultAccessControl = defaultAccessControl;
	}

	public boolean isDefaultTaxonomyPath() {
		return defaultTaxonomyPath;
	}

	public void setDefaultTaxonomyPath(boolean defaultTaxonomyPath) {
		this.defaultTaxonomyPath = defaultTaxonomyPath;
	}

	public boolean isDefaultGuest() {
		return defaultGuest;
	}

	public void setDefaultGuest(boolean defaultGuest) {
		this.defaultGuest = defaultGuest;
	}

	public boolean isDefaultExternalRef() {
		return defaultExternalRef;
	}

	public void setDefaultExternalRef(boolean defaultExternalRef) {
		this.defaultExternalRef = defaultExternalRef;
	}

	public boolean isDefaultBookmark() {
		return defaultBookmark;
	}

	public void setDefaultBookmark(boolean defaultBookmark) {
		this.defaultBookmark = defaultBookmark;
	}

	public boolean isDefaultIconType() {
		return defaultIconType;
	}

	public void setDefaultIconType(boolean defaultIconType) {
		this.defaultIconType = defaultIconType;
	}
}
