/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.user.UsersPortraitsComponent;

/**
 * 
 * Initial date: 18 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTADocumentRow {
	
	private final String id;
	private final VFSLeaf document;
	private final VFSMetadata metadata;
	private String authorName;
	private final boolean anonym;
	
	private String thumbnailUrl;
	private boolean thumbnailAvailable;
	private boolean openInNewWindow;
	
	private FormLink selectLink;
	private FormLink selectClassicLink;
	private UsersPortraitsComponent usersPortraitCmp;
	
	public GTADocumentRow(String id, VFSLeaf document, VFSMetadata metadata, boolean anonym) {
		this.document = document;
		this.metadata = metadata;
		this.anonym = anonym;
		this.id = id;
	}

	public VFSLeaf getDocument() {
		return document;
	}
	
	public VFSMetadata getDocumentMetadata() {
		return metadata;
	}
	
	public String getDisplayName() {
		return document.getName();
	}
	
	public boolean isAnonym() {
		return anonym;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean isOpenInNewWindow() {
		return openInNewWindow;
	}
	
	public void setOpenInNewWindow(boolean openInNewWindow) {
		this.openInNewWindow = openInNewWindow;
	}
	
	public boolean isThumbnailAvailable() {
		return thumbnailAvailable;
	}
	
	public void setThumbnailAvailable(boolean available) {
		this.thumbnailAvailable = available;
	}
	
	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	
	public void setThumbnailUrl(String url) {
		this.thumbnailUrl = url;
	}
	
	public UsersPortraitsComponent getUserPortraits() {
		return usersPortraitCmp;
	}
	
	public String getSelectLinkName() {
		return selectLink != null ? selectLink.getComponent().getComponentName() : null;
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

	public void setUserPortraits(UsersPortraitsComponent usersPortraitCmp) {
		this.usersPortraitCmp = usersPortraitCmp;
	}

	public String getUserPortraitsName() {
		return usersPortraitCmp.getComponentName();
	}
}
