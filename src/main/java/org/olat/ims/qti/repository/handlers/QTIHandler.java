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

package org.olat.ims.qti.repository.handlers;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.ims.qti.editor.QTIEditorPackageImpl;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.modules.qpool.model.QItemList;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.FileHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;

/**
 * Initial Date:  Apr 6, 2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */
public abstract class QTIHandler extends FileHandler {

	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}

	protected RepositoryEntry createResource(String type, FileResource ores, Identity initialAuthor,
			String displayname, String description, Object object, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		
		File fRepositoryQTI = new File(FileResourceManager.getInstance().getFileResourceRoot(re.getOlatResource()), "qti.zip");
		QTIEditorPackageImpl qtiPackage = new QTIEditorPackageImpl(displayname, type, locale);
		if(object instanceof QItemList) {
			QItemList itemToImport = (QItemList)object;
			QTIQPoolServiceProvider provider = (QTIQPoolServiceProvider)CoreSpringFactory.getBean("qtiPoolServiceProvider");
			provider.exportToEditorPackage(qtiPackage, itemToImport.getItems(), true);
		}
		qtiPackage.savePackageTo(fRepositoryQTI);
		return re;
	}

	protected RepositoryEntry importResource(Identity initialAuthor, String displayname, String description,
			Organisation organisation, FileResource ores, File file, String filename) {
		
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(ores);
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File zipDir = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
		FileResource.copyResource(file, filename, zipDir);
		ZipUtil.zipAll(zipDir, new File(fResourceFileroot, "qti.zip"), false);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		//zip it
		return re;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		//getFileResource search the first zip
		File sourceFile = FileResourceManager.getInstance().getFileResource(sourceResource);
		File targetRootDir = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();

		File targetDir = new File(targetRootDir, FileResourceManager.ZIPDIR);
		FileResource.copyResource(sourceFile, sourceFile.getName(), targetDir, new ChangeLogFilter());
		ZipUtil.zipAll(targetDir, new File(targetRootDir, "qti.zip"), false);
		return target;
	}
	
	private static final class ChangeLogFilter implements PathMatcher {
		@Override
		public boolean matches(Path path) {
			String name = path.toString();
			if(name.startsWith("/changelog")) {
				return false;
			}
			return true;
		}
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}
	
	@Override
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors) {
		ReferenceManager refM = CoreSpringFactory.getImpl(ReferenceManager.class);
		String referencesSummary = refM.getReferencesToSummary(entry.getOlatResource(), locale);
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			errors.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary, entry.getDisplayname() }));
			return false;
		}
		if (CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(entry.getOlatResource(), null)) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			errors.setError(translator.translate("details.delete.error.editor",
					new String[] { entry.getDisplayname() }));
			return false;
		}
		return true;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
    //nothing to do
		return null;
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		//nothing to do since nothing locked
	}
	
	@Override
	public boolean isLocked(OLATResourceable ores) {
		return false;
	}
}