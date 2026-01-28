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
package org.olat.modules.curriculum.ui.widgets;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Jan 23, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationRow implements OLATResourceable {
	
	private Long key;
	private String externalRef;
	private String displayName;
	private String status;
	private String executionPeriod;
	private String translatedTechnicalType;
	private Long resourceableId;
	private String resourceableTypeName;
	private String thumbnailRelPath;
	private FormLink titleLink;
	private FormLink structureLink;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getExternalRef() {
		return externalRef;
	}
	
	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getExecutionPeriod() {
		return executionPeriod;
	}

	public void setExecutionPeriod(String executionPeriod) {
		this.executionPeriod = executionPeriod;
	}

	public String getTranslatedTechnicalType() {
		return translatedTechnicalType;
	}

	public void setTranslatedTechnicalType(String translatedTechnicalType) {
		this.translatedTechnicalType = translatedTechnicalType;
	}

	@Override
	public Long getResourceableId() {
		return resourceableId;
	}
	
	public void setResourceableId(Long resourceableId) {
		this.resourceableId = resourceableId;
	}
	
	@Override
	public String getResourceableTypeName() {
		return resourceableTypeName;
	}
	
	public void setResourceableTypeName(String resourceableTypeName) {
		this.resourceableTypeName = resourceableTypeName;
	}
	
	public boolean isThumbnailAvailable() {
		return StringHelper.containsNonWhitespace(thumbnailRelPath);
	}
	
	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public void setThumbnailRelPath(String thumbnailRelPath) {
		this.thumbnailRelPath = thumbnailRelPath;
	}
	
	public String getTitleName() {
		return titleLink != null? titleLink.getComponent().getComponentName(): null;
	}
	
	public FormLink getTitleLink() {
		return titleLink;
	}

	public void setTitleLink(FormLink titleLink) {
		this.titleLink = titleLink;
	}

	public String getStructureName() {
		return structureLink != null? structureLink.getComponent().getComponentName(): null;
	}
	
	public FormLink getStructureLink() {
		return structureLink;
	}
	
	public void setStructureLink(FormLink structureLink) {
		this.structureLink = structureLink;
	}

}
