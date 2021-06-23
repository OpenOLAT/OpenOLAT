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
package org.olat.modules.portfolio.handler;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.model.export.BinderXML;
import org.olat.modules.portfolio.ui.BinderController;
import org.olat.modules.portfolio.ui.BinderPickerController;
import org.olat.modules.portfolio.ui.BinderRuntimeController;
import org.olat.modules.portfolio.ui.PortfolioAssessmentDetailsController;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.references.ReferenceManager;

/**
 * 
 * Description:<br>
 * Handler wihich allow the portfolio map in repository to be opened and launched.
 * 
 * <P>
 * Initial Date:  12 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
// Loads of parameters are unused
public class BinderTemplateHandler implements RepositoryHandler {
	
	private static final Logger log = Tracing.createLoggerFor(BinderTemplateHandler.class);

	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return CoreSpringFactory.getImpl(PortfolioV2Module.class).isEnabled();
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.portfoliov2";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Object createObject, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		OLATResource resource = portfolioService.createBinderTemplateResource();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource,
				RepositoryEntryStatusEnum.preparation, organisation);
		portfolioService.createAndPersistBinderTemplate(initialAuthor, re, locale);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return BinderTemplateResource.evaluate(file, filename);
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
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		try {
			//create resource
			OLATResource resource = portfolioService.createBinderTemplateResource();
			LocalFolderImpl fResourceRootContainer = FileResourceManager.getInstance().getFileResourceRootImpl(resource);
			File fResourceFileroot = fResourceRootContainer.getBasefile();
			File zipRoot = new File(fResourceFileroot, FileResourceManager.ZIPDIR);
			FileResource.copyResource(file, filename, zipRoot);

			//create repository entry
			RepositoryEntry re = repositoryService.create(initialAuthor, initialAuthorAlt, "", displayname, description, resource,
					RepositoryEntryStatusEnum.preparation, organisation);
			
			//import binder
			File binderFile = new File(zipRoot, BinderTemplateResource.BINDER_XML);
			BinderXML transientBinder = BinderXStream.fromPath(binderFile.toPath());
			
			File posterImage = null;
			if(StringHelper.containsNonWhitespace(transientBinder.getImagePath())) {
				posterImage = new File(zipRoot, transientBinder.getImagePath());
			}
			portfolioService.importBinder(transientBinder, re, posterImage);

			RepositoryEntryImportExport rei = new RepositoryEntryImportExport(re, zipRoot);
			if(rei.anyExportedPropertiesAvailable()) {
				re = rei.importContent(re, fResourceRootContainer.createChildContainer("media"), initialAuthor);
			}
			//delete the imported files
			FileUtils.deleteDirsAndFiles(zipRoot, true, true);
			return re;
		} catch (IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		//
		return null;
	}

	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		Binder templateSource = portfolioService.getBinderByResource(source.getOlatResource());
		portfolioService.copyBinder(templateSource, target);
		return target;
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
		return true;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	@Override
	public boolean readyToDelete(RepositoryEntry entry, Identity identity, Roles roles, Locale locale, ErrorList errors) {
		String referencesSummary = CoreSpringFactory.getImpl(ReferenceManager.class)
				.getReferencesToSummary(entry.getOlatResource(), locale);
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			errors.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary, entry.getDisplayname() }));
			return false;
		}
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		int usage = portfolioService.getTemplateUsage(entry);
		if(usage > 0) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, locale);
			String reason = translator.translate("details.referenceinfo.binder.template",
					new String[] { String.valueOf(usage) });
			errors.setError(translator.translate("details.delete.error.references",
					new String[] { reason, entry.getDisplayname() }));
			return false;
		}
		
		return true;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		Binder template = portfolioService.getBinderByResource(entry.getOlatResource());
		return portfolioService.deleteBinderTemplate(template, entry);
	}

	/**
	 * Transform the map in a XML file and zip it (Repository export want a zip)
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		RepositoryEntry templateEntry = RepositoryManager.getInstance().lookupRepositoryEntry(res, true);
		Binder template = CoreSpringFactory.getImpl(PortfolioService.class)
				.getBinderByResource(templateEntry.getOlatResource());
		return new BinderTemplateMediaResource(template, templateEntry);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control, TooledStackedPanel toolbar) {
		return null;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new BinderRuntimeController(ureq, wControl, re, reSecurity, (uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
				PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
				if(reSecurity.isParticipant()) {
					//pick up the template
					
					return new BinderPickerController(uureq, wwControl, entry);
				} else {
					Binder binder = portfolioService.getBinderByResource(entry.getOlatResource());
					CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
						.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
					BinderConfiguration bConfig = BinderConfiguration.createTemplateConfig(reSecurity.isEntryAdmin());
					BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForTemplate(reSecurity);
					return new BinderController(uureq, wwControl, toolbarPanel, secCallback, binder, bConfig);
				}
			});
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar, Identity assessedIdentity) {
		return new PortfolioAssessmentDetailsController(ureq, wControl, re, assessedIdentity);
	}

	@Override
	public String getSupportedType() {
		return BinderTemplateResource.TYPE_NAME;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, identity, "subkey", null);
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		if(lockResult!=null) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(ores, "subkey");
	}

}
