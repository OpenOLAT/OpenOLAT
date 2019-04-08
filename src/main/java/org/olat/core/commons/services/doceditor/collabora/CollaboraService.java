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
package org.olat.core.commons.services.doceditor.collabora;

import java.io.File;
import java.io.InputStream;

import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.Discovery;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.lock.LockResult;

/**
 * 
 * Initial date: 6 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CollaboraService {
	
	public static final OLATResourceable REFRESH_EVENT_ORES = OresHelper
			.createOLATResourceableType(CollaboraRefreshDiscoveryEvent.class.getSimpleName() + ":RefreshDiscovery");

	boolean fileExists(String fileId);

	File getFile(String fileId);

	VFSMetadata getMetadata(String fileId);
	
	Access createAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback);

	Access getAccess(String accessToken);
	
	void deleteAccess(Access access);
	
	boolean canUpdateContent(Access access, String fileId);

	boolean updateContent(Access access, InputStream fileInputStream);
	
	Discovery getDiscovery();
	
	String getEditorBaseUrl(File file);
	
	boolean accepts(String suffix, Mode mode);
	
	boolean isLockNeeded(Mode mode);

	boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity);

	LockResult lock(VFSLeaf vfsLeaf, Identity identity);

	void unlock(VFSLeaf vfsLeaf, LockResult lock);

}
