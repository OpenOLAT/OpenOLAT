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
	private String modified;
	private boolean thumbnailAvailable;
	private String thumbnailUrl;
	private FormLink selectLink;
	private FormLink toolsLink;
	
	public ProjFileRow(ProjFile file) {
		this.key = file.getKey();
		this.displayName = ProjectUIFactory.getDisplayName(file);
		this.filename = file.getVfsMetadata().getFilename();
		this.creationDate = file.getVfsMetadata().getCreationDate();
		this.lastModifiedDate = file.getArtefact().getContentModifiedDate();
		this.lastModifiedBy = file.getArtefact().getContentModifiedBy();
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

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
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

	public String getSelectLinkName() {
		return selectLink != null? selectLink.getComponent().getComponentName(): null;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
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
