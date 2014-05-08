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
* <p>
* Initial code contributed and copyrighted by<br>
* frentix GmbH, http://www.frentix.com
* <p>
*/
package org.olat.course.repository;

import java.io.File;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 déc. 2011 <br>
 *
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class ImportGlossaryReferencesController {
	
	private static final OLog log = Tracing.createLoggerFor(ImportGlossaryReferencesController.class);



	/**
	 * Import a referenced repository entry.
	 * 
	 * @param importExport
	 * @param node
	 * @param importMode Type of import.
	 * @param keepSoftkey If true, no new softkey will be generated.
	 * @param owner
	 * @return
	 */
	public static RepositoryEntry doImport(RepositoryEntryImportExport importExport, ICourse course, Identity owner) {
		GlossaryManager gm = GlossaryManager.getInstance();
		GlossaryResource resource = gm.createGlossary();
		if (resource == null) {
			log.error("Error adding glossary directry during repository reference import: " + importExport.getDisplayName());
			return null;
		}

		// unzip contents
		VFSContainer glossaryContainer = gm.getGlossaryRootFolder(resource);
		File fExportedFile = importExport.importGetExportedFile();
		if (fExportedFile.exists()) {
			ZipUtil.unzip(new LocalFileImpl(fExportedFile), glossaryContainer);
		} else {
			log.warn("The actual contents of the glossary were not found in the export.");
		}

		// create repository entry
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(resource);
		
		RepositoryEntry importedRepositoryEntry = repositoryService.create(owner,
				importExport.getResourceName(), importExport.getDisplayName(), importExport.getDescription(), ores, 0);

			// set the new glossary reference
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setGlossarySoftKey(importedRepositoryEntry.getSoftkey());
		ReferenceManager.getInstance().addReference(course, importedRepositoryEntry.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);			
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);


		return importedRepositoryEntry;
	}
}
