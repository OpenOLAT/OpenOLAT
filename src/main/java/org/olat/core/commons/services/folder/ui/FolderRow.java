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
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;

/**
 * 
 * Initial date: 21 Feb 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FolderRow {
	
	private final VFSItem vfsItem;
	private VFSMetadata metadata;
	private Long key;
	private String iconCssClass;
	private String title;
	private String createdBy;
	private Date lastModifiedDate;
	private String lastModifiedBy;
	private String modified;
	private boolean deleted;
	private Date deletedDate;
	private String deletedBy;
	private String fileSuffix;
	private String translatedType;
	private FolderStatus status;
	private String translatedStatus;
	private String elementsLabel;
	private String newLabel;
	private String labels;
	private Long size;
	private String translatedSize;
	private String filePath;
	private Long versions;
	private License license;
	private String translatedLicense;
	private boolean thumbnailAvailable;
	private String thumbnailUrl;
	private String thumbnailCss;
	private boolean openable;
	private boolean openInNewWindow;
	private FormItem titleItem;
	private FormItem selectionItem;
	private FormItem filePathItem;
	private FormItem toolsLink;
	
	public FolderRow(VFSItem vfsItem) {
		this.vfsItem = vfsItem;
	}

	public VFSItem getVfsItem() {
		return vfsItem;
	}
	
	public VFSMetadata getMetadata() {
		return metadata;
	}
	
	public void setMetadata(VFSMetadata metadata) {
		this.metadata = metadata;
	}

	public boolean isDirectory() {
		if (metadata != null) {
			return metadata.isDirectory();
		}
		if (vfsItem != null) {
			return vfsItem instanceof VFSContainer;
		}
		return false;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getFilename() {
		if (metadata != null) {
			return metadata.getFilename();
		}
		if (vfsItem != null) {
			vfsItem.getName();
		}
		return null;
	}
	
	public String getDescription() {
		return metadata != null? metadata.getComment(): null;
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
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

	public FolderStatus getStatus() {
		return status;
	}

	public void setStatus(FolderStatus status) {
		this.status = status;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public String getElementsLabel() {
		return elementsLabel;
	}

	public void setElementsLabel(String elementsLabel) {
		this.elementsLabel = elementsLabel;
	}

	public String getNewLabel() {
		return newLabel;
	}

	public void setNewLabel(String newLabel) {
		this.newLabel = newLabel;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
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

	public String getTranslatedLicense() {
		return translatedLicense;
	}

	public void setTranslatedLicense(String translatedLicense) {
		this.translatedLicense = translatedLicense;
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

	public String getThumbnailCss() {
		return thumbnailCss;
	}

	public void setThumbnailCss(String thumbnailCss) {
		this.thumbnailCss = thumbnailCss;
	}

	public boolean isOpenable() {
		return openable;
	}

	public void setOpenable(boolean openable) {
		this.openable = openable;
	}

	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
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

	public FormItem getFilePathItem() {
		return filePathItem;
	}

	public void setFilePathItem(FormItem filePathItem) {
		this.filePathItem = filePathItem;
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
