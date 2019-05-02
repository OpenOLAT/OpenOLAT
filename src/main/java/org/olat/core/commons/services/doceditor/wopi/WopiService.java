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
package org.olat.core.commons.services.doceditor.wopi;

import java.util.Collection;
import java.util.Date;

import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 6 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface WopiService {
	
	public String getRegularDiscoveryPath();

	/**
	 * Download the discovery file from the WOPI client.
	 *
	 * @param discoveryUrl
	 * @return
	 */
	public Discovery getDiscovery(String discoveryUrl);

	boolean hasAction(Discovery discovery, String actionName, String suffix);

	Action getAction(Discovery discovery, String actionName, String suffix);
	
	Collection<Action> getActions(Discovery discovery);

	/**
	 * Get an access for a file and user. A new access is created if
	 *  - no access for the file and user exists
	 *  - the old access is expired
	 *  - if the user rights of the file changed
	 *  If a new access is created, the old one is deleted.
	 *
	 * @param vfsMetadata
	 * @param identity
	 * @param secCallback
	 * @param expiresAt (optional) date when the access expires
	 * @return
	 */
	Access getOrCreateAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback, Date expiresAt);

	Access getAccess(String accessToken);

	VFSLeaf getVfsLeaf(Access access);
	
	void deleteAccess(String accessToken);

}
