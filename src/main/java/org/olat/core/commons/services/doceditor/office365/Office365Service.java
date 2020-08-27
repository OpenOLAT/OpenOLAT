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
package org.olat.core.commons.services.doceditor.office365;

import java.io.InputStream;
import java.util.Collection;
import java.util.Locale;

import org.olat.core.commons.services.doceditor.Access;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 26.04.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface Office365Service {
	
	public static final OLATResourceable REFRESH_EVENT_ORES = OresHelper
			.createOLATResourceableType(Office365RefreshDiscoveryEvent.class.getSimpleName() + ":RefreshDiscovery");

	boolean updateContent(Access access, InputStream fileInputStream);
	
	boolean verifyProofKey(String requestUrl, String accessToken, String timeStamp, String proofKey, String oldProofKey);
	
	Collection<String> getContentSecurityPolicyUrls();

	String getEditorActionUrl(VFSMetadata vfsMetadata, Mode mode, Locale locale);
	
	boolean isSupportingFormat(String suffix, Mode mode);
	
	boolean isLockNeeded(Mode mode);

	boolean isLockedForMe(VFSLeaf vfsLeaf, Identity identity);
	
	boolean isLockedForMe(VFSLeaf vfsLeaf, VFSMetadata metadata, Identity identity);

	String getLockToken(VFSLeaf vfsLeaf);

	boolean lock(VFSLeaf vfsLeaf, Identity identity, String lockToken);

	boolean canUnlock(VFSLeaf vfsLeaf, String lockToken);

	void unlock(VFSLeaf vfsLeaf, String lockToken);

	void refreshLock(VFSLeaf vfsLeaf, String lockToken);

}
