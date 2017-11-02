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
package org.olat.modules.taxonomy.webdav;

import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("taxonomyDocumentsLibraryWebDAVProvider")
public class TaxonomyDocumentsLibraryWebDAVProvider implements WebDAVProvider {

	@Value("${taxonomy.webdav.mountpoint:doc-pool}")
	private String MOUNTPOINT;

	@Override
	public String getMountPoint() {
		return MOUNTPOINT;
	}
	
	@Override
	public boolean hasAccess(IdentityEnvironment identityEnv) {
		return identityEnv != null;
	}

	@Override
	public VFSContainer getContainer(IdentityEnvironment identityEnv) {
		return new TaxonomyDocumentsLibraryWebDAVMergeSource(identityEnv);
	}
}