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
package org.olat.core.commons.services.doceditor.model;

import java.util.Date;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 29 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientAccess implements Access {
	
	private Long key;
	private Date creationDate;
	private Date lastModified;

	private String editorType;
	private Date expiresAt;
	private Mode mode;

	private Date editStartDate;
	private boolean versionControlled;
	private boolean download;
	private boolean fireSavedEvent;
	
	private VFSMetadata metadata;
	private Identity identity;
	
	public TransientAccess(VFSMetadata metadata, Identity identity, String editorType, Mode mode, boolean versionControlled,
			boolean download, boolean fireSavedEvent, Date expiresAt) {
		creationDate = new Date();
		this.key = - CodeHelper.getForeverUniqueID();
		this.editorType = editorType;
		this.mode = mode;
		this.versionControlled = versionControlled;
		this.download = download;
		this.fireSavedEvent = fireSavedEvent;
		this.expiresAt = expiresAt;
		this.metadata = metadata;
		this.identity = identity;
	}
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public String getEditorType() {
		return editorType;
	}

	@Override
	public Date getExpiresAt() {
		return expiresAt;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public Date getEditStartDate() {
		return editStartDate;
	}

	@Override
	public boolean isVersionControlled() {
		return versionControlled;
	}

	@Override
	public boolean isDownload() {
		return download;
	}

	@Override
	public boolean isFireSavedEvent() {
		return fireSavedEvent;
	}

	@Override
	public VFSMetadata getMetadata() {
		return metadata;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
}
