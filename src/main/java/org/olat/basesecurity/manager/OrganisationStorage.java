/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.basesecurity.manager;

import java.io.File;

import jakarta.annotation.PostConstruct;

import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: Feb 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class OrganisationStorage {

	private File bcrootDirectory;
	private File organisationsDirectory;
	
	@Autowired
	private FolderModule folderModule;

	@PostConstruct
	public void initFolders() {
		bcrootDirectory = new File(folderModule.getCanonicalRoot());
		organisationsDirectory = new File(bcrootDirectory, "organisation");
		if (!organisationsDirectory.exists()) {
			organisationsDirectory.mkdirs();
		}
	}

	public void delete(OrganisationRef organisation) {
		File organisationRoot = new File(organisationsDirectory, organisation.getKey().toString());
		if (organisationRoot.exists()) {
			String relativePath = File.separator + bcrootDirectory.toPath().relativize(organisationRoot.toPath()).toString();
			VFSManager.olatRootContainer(relativePath).deleteSilently();
		}
	}
	
	public VFSContainer getLegalContainer(OrganisationRef organisation) {
		return getOrCreateContainer(organisation, "legal");
	}
	
	private VFSContainer getOrCreateContainer(OrganisationRef organisation, String path) {
		File storage = new File(organisationsDirectory, organisation.getKey().toString());
		if (!storage.exists()) {
			storage.mkdirs();
		}
		storage = new File(storage, path);
		if (!storage.exists()) {
			storage.mkdirs();
		}
		
		String relativePath = File.separator + bcrootDirectory.toPath().relativize(storage.toPath()).toString();
		return VFSManager.olatRootContainer(relativePath);
	}
	
}
