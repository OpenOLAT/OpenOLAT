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
package org.olat.core.commons.services.folder.ui;

import java.util.Date;

import org.olat.core.commons.services.license.License;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderRow {
	
	private final VFSItem vfsItem;
	private String iconCssClass;
	private String title;
	private String createdBy;
	private Date lastModifiedDate;
	private String lastModifiedBy;
	private String modified;
	private String fileSuffix;
	private String translatedType;
	private String status;
	private Long size;
	private String translatedSize;
	private String filePath;
	private Long versions;
	private License license;
	private String labels;
	private boolean thumbnailAvailable;
	private String thumbnailUrl;
	private FormItem titleItem;
	private FormItem selectionItem;
	private FormItem toolsLink;
	
	public FolderRow(VFSItem vfsItem) {
		this.vfsItem = vfsItem;
	}

	public VFSItem getVfsItem() {
		return vfsItem;
	}
	
	public String getIconCssClass() {
		return iconCssClass;
	}

	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public String getTranslatedType() {
		return translatedType;
	}
	
	public void setTranslatedType(String translatedType) {
		this.translatedType = translatedType;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getTranslatedSize() {
		return translatedSize;
	}

	public void setTranslatedSize(String translatedSize) {
		this.translatedSize = translatedSize;
	}

	public String getFilePath() {
		return filePath;
	}
	
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	public Long getVersions() {
		return versions;
	}
	
	public void setVersions(Long versions) {
		this.versions = versions;
	}
	
	public License getLicense() {
		return license;
	}
	
	public void setLicense(License license) {
		this.license = license;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public boolean isThumbnailAvailable() {
		return thumbnailAvailable;
	}

	public void setThumbnailAvailable(boolean thumbnailAvailable) {
		this.thumbnailAvailable = thumbnailAvailable;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public FormItem getTitleItem() {
		return titleItem;
	}

	public void setTitleItem(FormItem titleItem) {
		this.titleItem = titleItem;
	}

	public FormItem getSelectionItem() {
		return selectionItem;
	}

	public void setSelectionItem(FormItem selectionItem) {
		this.selectionItem = selectionItem;
	}
	
	public String getSelectionItemName() {
		return selectionItem != null? selectionItem.getComponent().getComponentName(): null;
	}

	public FormItem getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormItem toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public String getToolsLinkName() {
		return toolsLink != null? toolsLink.getComponent().getComponentName(): null;
	}

}
