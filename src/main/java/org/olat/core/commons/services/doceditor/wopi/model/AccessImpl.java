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
package org.olat.core.commons.services.doceditor.wopi.model;

import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 18 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessImpl implements Access {
	
	private String fileId;
	private String token;
	private Identity identity;
	private boolean canEdit;
	private boolean versionControlled;
	private boolean canClose;
	private VFSMetadata vfsMetadata;

	@Override
	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Override
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public boolean canEdit() {
		return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
	}

	@Override
	public boolean isVersionControlled() {
		return versionControlled;
	}

	public void setVersionControlled(boolean versionControlled) {
		this.versionControlled = versionControlled;
	}

	@Override
	public boolean canClose() {
		return canClose;
	}

	public void setCanClose(boolean canClose) {
		this.canClose = canClose;
	}

	@Override
	public VFSMetadata getVfsMetadata() {
		return vfsMetadata;
	}

	public void setVfsMetadata(VFSMetadata vfsMetadata) {
		this.vfsMetadata = vfsMetadata;
	}

}
