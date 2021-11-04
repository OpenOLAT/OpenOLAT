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
*/
package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;
import java.util.Properties;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.glossary.GlossaryItemManager;
import org.olat.core.commons.modules.glossary.GlossaryMainController;
import org.olat.core.commons.modules.glossary.GlossaryRuntimeController;
import org.olat.core.commons.modules.glossary.GlossarySecurityCallback;
import org.olat.core.commons.modules.glossary.GlossarySecurityCallbackImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;


/**
 * Description:<br>
 * 
 * <P>
 * Initial Date: Dec 04 2006 <br>
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class GlossaryHandler implements RepositoryHandler {

	
	public static final String PROCESS_CREATENEW = "cn";
	public static final String PROCESS_UPLOAD = "pu";

	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return true;
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		GlossaryResource glossaryResource = CoreSpringFactory.getImpl(GlossaryManager.class).createGlossary();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(glossaryResource);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource,
				RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.glossary";
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return GlossaryResource.evaluate(file, filename);
	}

	@Override
	public boolean supportImportUrl() {
		return false;
	}
	
	@Override
	public ResourceEvaluation acceptImport(String url) {
		return ResourceEvaluation.notValid();
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String filename) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		GlossaryManager glossaryManager = CoreSpringFactory.getImpl(GlossaryManager.class);
		GlossaryResource glossaryResource = glossaryManager.createGlossary();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(glossaryResource);
		//copy resources
		File glossyPath = glossaryManager.getGlossaryRootFolder(glossaryResource).getBasefile();
		FileResource.copyResource(file, filename, glossyPath);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource,
				RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		//
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		File sourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(sourceResource).getBasefile();
		File targetFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(targetResource).getBasefile();
		FileUtils.copyDirContentsToDir(sourceFileroot, targetFileroot, false, "copy");
		return target;
	}

	@Override
	public String getSupportedType() {
		return GlossaryResource.TYPE_NAME;
	}

	@Override
	public boolean supportsDownload() {
		return true;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource, Identity identity, Roles roles) {
		return EditionSupport.embedded;
	}
	
	@Override
	public boolean supportsAssessmentDetails() {
		return false;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	/**
	 * @param ureq
	 * @param wControl
	 * @param res
	 * @param initialViewIdentifier
	 * @return Controller
	 */
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		
		return new GlossaryRuntimeController(ureq, wControl, re, reSecurity,
				(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
					GlossaryManager glossaryManager = CoreSpringFactory.getImpl(GlossaryManager.class);
					VFSContainer glossaryFolder = glossaryManager.getGlossaryRootFolder(entry.getOlatResource());

					Properties glossProps = CoreSpringFactory.getImpl(GlossaryItemManager.class).getGlossaryConfig(glossaryFolder);
					boolean editableByUser = "true".equals(glossProps.getProperty(GlossaryItemManager.EDIT_USERS));
					boolean owner = security.isOwner();
					
					GlossarySecurityCallback secCallback;
					if (uureq.getUserSession().getRoles().isGuestOnly()) {
						secCallback = new GlossarySecurityCallbackImpl();				
					} else {
						secCallback = new GlossarySecurityCallbackImpl(false, owner, editableByUser, uureq.getIdentity().getKey());
					}

					CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
						.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
					return new GlossaryMainController(wwControl, uureq, glossaryFolder, entry.getOlatResource(), secCallback, false);
			});
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}
	
	@Override
	public FormBasicController createAuthorSmallDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, Form mainForm) {
		return null;
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		return CoreSpringFactory.getImpl(GlossaryManager.class).getAsMediaResource(res);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar) {
		VFSContainer glossaryFolder = CoreSpringFactory.getImpl(GlossaryManager.class).getGlossaryRootFolder(re.getOlatResource());

		Properties glossProps = CoreSpringFactory.getImpl(GlossaryItemManager.class).getGlossaryConfig(glossaryFolder);
		boolean editableByUser = "true".equals(glossProps.getProperty(GlossaryItemManager.EDIT_USERS));
		GlossarySecurityCallback secCallback;
		if (ureq.getUserSession().getRoles().isGuestOnly()) {
			secCallback = new GlossarySecurityCallbackImpl();				
		} else {
			secCallback = new GlossarySecurityCallbackImpl(true, true, editableByUser, ureq.getIdentity().getKey());
		}
		GlossaryMainController gctr = new GlossaryMainController(wControl, ureq, glossaryFolder, re.getOlatResource(), secCallback, false);
		// use on column layout
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, gctr);
		layoutCtr.addDisposableChildController(gctr); // dispose content on layout dispose
		return layoutCtr;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		// FIXME fg
		// do not need to notify all current users of this resource, since the only
		// way to access this resource
		// FIXME:fj:c to be perfect, still need to notify
		// repositorydetailscontroller and searchresultcontroller....
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		CoreSpringFactory.getImpl(GlossaryManager.class).deleteGlossary(res);
		return true;
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
