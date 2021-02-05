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
package org.olat.core.commons.services.doceditor.onlyoffice;

import java.io.File;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.lock.LockResult;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface OnlyOfficeService {

	boolean fileExists(String fileId);

	File getFile(String fileId);

	VFSLeaf getVfsLeaf(String fileId);
	
	ApiConfig getApiConfig(VFSMetadata vfsMetadata, Identity identity, Mode mode, boolean isDownloadEnabled, boolean versionControlled, String downloadUrl);

	String toJson(ApiConfig apiConfig);

	boolean editorOpened(VFSLeaf vfsLeaf, Identity identity, String documentKey);

	boolean editorClosed(VFSLeaf vfsLeaf, Identity identity, boolean stillEditing);
	
	void editorFinishedContentUnchanged(VFSLeaf vfsLeaf);

	boolean editorFinishedContentChanged(VFSLeaf vfsLeaf, Identity identity, String documentKey, String editedDocumentUrl,
			boolean versionControlled);

	boolean isEditLicenseAvailable();
	
	Long getEditLicensesInUse();
	
	boolean isLockNeeded(Mode mode);

	boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity);
	
	boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity);

	LockResult lock(VFSLeaf vfsLeaf, Identity identity);

	void unlock(VFSLeaf vfsLeaf);
	
	boolean isSupportedFormat(String suffix, Mode mode);

	Identity getIdentity(String identityId);

}
