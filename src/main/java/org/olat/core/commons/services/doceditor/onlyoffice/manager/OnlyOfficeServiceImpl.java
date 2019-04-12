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
package org.olat.core.commons.services.doceditor.onlyoffice.manager;

import java.io.File;

import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.onlyoffice.OnlyOfficeService;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.commons.services.doceditor.wopi.WopiService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OnlyOfficeServiceImpl implements OnlyOfficeService {

	@Autowired
	private WopiService wopiService;

	@Override
	public boolean fileExists(String fileId) {
		return wopiService.fileExists(fileId);
	}

	@Override
	public File getFile(String fileId) {
		return wopiService.getFile(fileId);
	}

	@Override
	public VFSMetadata getMetadata(String fileId) {
		return wopiService.getMetadata(fileId);
	}

	@Override
	public Access createAccess(VFSMetadata vfsMetadata, Identity identity, DocEditorSecurityCallback secCallback) {
		return wopiService.createAccess(vfsMetadata, identity, secCallback);
	}

	@Override
	public Access getAccess(String accessToken) {
		return wopiService.getAccess(accessToken);
	}

	@Override
	public void deleteAccess(Access access) {
		if (access == null) return;
		
		wopiService.deleteAccess(access.getToken());
	}
	
}
