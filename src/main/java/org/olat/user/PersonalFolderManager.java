/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.user;

import java.io.File;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.BriefcaseWebDAVProvider;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSManager;
import org.olat.user.manager.ManifestBuilder;

/**
 * Manager for the personal-folder of a user.
 */
public class PersonalFolderManager extends BriefcaseWebDAVProvider implements UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(PersonalFolderManager.class);

	@Override
	public String getExporterID() {
		return "personal.folders";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale lcoale) {
		File folder = new File(archiveDirectory, "PersonalFolders");
		String rootPath = FolderConfig.getCanonicalRoot() + FolderConfig.getUserHome(identity);
		export("PersonalFolders", new File(rootPath, "private"), manifest, new File(folder, "private"));
		export("PersonalFolders", new File(rootPath, "public"), manifest, new File(folder, "public"));
	}
	
	public void export(String path, File directory, ManifestBuilder manifest, File archive) {
		File[] files = directory.listFiles();
		if(files != null && files.length > 0) {
			String currentPath = path + "/" + directory.getName();
			for(File file:files) {
				if(file.isHidden()) {
					continue;
				}
				if(file.isDirectory()) {
					File nextArchiveDir = new File(archive, file.getName());
					nextArchiveDir.mkdirs();
					export(currentPath, file, manifest, nextArchiveDir);
				} else if(file.isFile()) {
					FileUtils.copyFileToDir(file, archive, false, "Copy personal folder");
					manifest.appendFile(currentPath + "/" + file.getName());
				}
			}
		}
	}

	/**
	 * Delete personal-folder homes/<username> (private & public) of an user.
	 */
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		VFSManager.olatRootContainer(getRootPathFor(identity), null).deleteSilently();// will delete meta and version informations
		log.info(Tracing.M_AUDIT, "Personal-folder deleted for identity={}", identity.getKey());
	}
}
