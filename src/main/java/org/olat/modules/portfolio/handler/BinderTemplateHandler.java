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
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.BinderController;
import org.olat.modules.portfolio.ui.BinderPickerController;
import org.olat.modules.portfolio.ui.BinderRuntimeController;
import org.olat.modules.portfolio.ui.PortfolioAssessmentDetailsController;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.EditionSupport;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.RepositoryEntryRuntimeController.RuntimeControllerCreator;
import org.olat.resource.OLATResource;

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
	
	@Override
	public boolean isCreate() {
		return CoreSpringFactory.getImpl(PortfolioV2Module.class).isEnabled();
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.portfoliov2";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Object createObject, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
		OLATResource resource = portfolioService.createBinderTemplateResource();
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		portfolioService.createAndPersistBinderTemplate(initialAuthor, re, locale);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return new ResourceEvaluation(false);
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Locale locale, File file, String filename) {
		return null;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
		return null;
	}

	@Override
	public boolean supportsDownload() {
		return false;
	}

	@Override
	public EditionSupport supportsEdit(OLATResourceable resource) {
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
		return false;
	}

	@Override
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		return false;
	}

	/**
	 * Transform the map in a XML file and zip it (Repository export want a zip)
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable)
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		return null;
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control, TooledStackedPanel toolbar) {
		return null;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		return new BinderRuntimeController(ureq, wControl, re, reSecurity,
			new RuntimeControllerCreator() {
				@Override
				public Controller create(UserRequest uureq, WindowControl wwControl, TooledStackedPanel toolbarPanel,
						RepositoryEntry entry, RepositoryEntrySecurity security, AssessmentMode assessmentMode) {
					PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
					if(reSecurity.isGroupParticipant() || reSecurity.isCourseParticipant()) {
						//pick up the template
						
						return new BinderPickerController(uureq, wwControl, entry);
					} else {
						Binder binder = portfolioService.getBinderByResource(entry.getOlatResource());
						CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
							.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
						BinderConfiguration bConfig = BinderConfiguration.createTemplateConfig();
						BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForTemplate(reSecurity);
						return new BinderController(uureq, wwControl, toolbarPanel, secCallback, binder, bConfig);
					}
				}
			});
	}

	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbar, Identity assessedIdentity) {
		return new PortfolioAssessmentDetailsController(ureq, wControl, toolbar, re, assessedIdentity);
	}

	@Override
	public String getSupportedType() {
		return BinderTemplateResource.TYPE_NAME;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, identity, "subkey");
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

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}
}
