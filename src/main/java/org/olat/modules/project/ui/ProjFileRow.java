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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjFileRef;

/**
 * 
 * Initial date: 12 Dez 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjFileRow implements ProjFileRef {
	
	private final Long key;
	private final String displayName;
	private final String filename;
	private final Date creationDate;
	private final Date lastModifiedDate;
	private final Identity lastModifiedBy;
	private String lastModifiedByName;
	private final Date deletedDate;
	private final Identity deletedBy;
	private String deletedByName;
	private Set<Long> memberKeys;
	private String modified;
	private Set<Long> tagKeys;
	private String formattedTags;
	private boolean thumbnailAvailable;
	private String thumbnailUrl;
	private boolean openInNewWindow;
	private FormLink selectLink;
	private FormLink selectClassicLink;
	private FormLink toolsLink;
	
	public ProjFileRow(ProjFile file) {
		this.key = file.getKey();
		this.displayName = ProjectUIFactory.getDisplayName(file);
		this.filename = file.getVfsMetadata().getFilename();
		this.creationDate = file.getVfsMetadata().getCreationDate();
		this.lastModifiedDate = file.getArtefact().getContentModifiedDate();
		this.lastModifiedBy = file.getArtefact().getContentModifiedBy();
		this.deletedDate = file.getArtefact().getDeletedDate();
		this.deletedBy = file.getArtefact().getDeletedBy();
	}

	@Override
	public Long getKey() {
		return key;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getFilename() {
		return filename;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public Identity getLastModifiedBy() {
		return lastModifiedBy;
	}

	public String getLastModifiedByName() {
		return lastModifiedByName;
	}

	public void setLastModifiedByName(String lastModifiedByName) {
		this.lastModifiedByName = lastModifiedByName;
	}

	public String getDeletedByName() {
		return deletedByName;
	}

	public void setDeletedByName(String deletedByName) {
		this.deletedByName = deletedByName;
	}

	public Date getDeletedDate() {
		return deletedDate;
	}

	public Identity getDeletedBy() {
		return deletedBy;
	}

	public Set<Long> getMemberKeys() {
		return memberKeys;
	}

	public void setMemberKeys(Set<Long> memberKeys) {
		this.memberKeys = memberKeys;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public Set<Long> getTagKeys() {
		return tagKeys;
	}

	public void setTagKeys(Set<Long> tagKeys) {
		this.tagKeys = tagKeys;
	}

	public String getFormattedTags() {
		return formattedTags;
	}

	public void setFormattedTags(String formattedTags) {
		this.formattedTags = formattedTags;
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

	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}

	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
	}

	public String getSelectLinkName() {
		return selectLink != null? selectLink.getComponent().getComponentName(): null;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}

	public FormLink getSelectClassicLink() {
		return selectClassicLink;
	}

	public void setSelectClassicLink(FormLink selectClassicLink) {
		this.selectClassicLink = selectClassicLink;
	}
	
	public String getToolsLinkName() {
		return toolsLink != null? toolsLink.getComponent().getComponentName(): null;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
}
