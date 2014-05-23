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
package org.olat.repository.handlers;

import java.io.File;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.webFeed.FeedResourceSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.podcast.PodcastUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.repository.ui.author.AuthoringEditEntrySettingsController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
import org.olat.resource.references.ReferenceManager;

/**
 * Responsible class for handling any actions involving podcast resources.
 * 
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
// Loads of parameters are unused
public class PodcastHandler implements RepositoryHandler {
	
	@Override
	public boolean isCreate() {
		return true;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.podcast";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OLATResourceable ores = FeedManager.getInstance().createPodcastResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean isPostCreateWizardAvailable() {
		return false;
	}
	
	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return PodcastFileResource.evaluate(file, filename);
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Locale locale, File file, String filename) {
		
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(new PodcastFileResource());
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File blogRoot = new File(fResourceFileroot, FeedManager.getInstance().getFeedKind(resource));
		FileResource.copyResource(file, filename, blogRoot);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class)
				.create(initialAuthor, null, "", displayname, description, resource, RepositoryEntry.ACC_OWNERS);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public void addExtendedEditionControllers(UserRequest ureq, WindowControl wControl,
			AuthoringEditEntrySettingsController pane, RepositoryEntry entry) {
		
		QuotaManager qm = QuotaManager.getInstance();
		if (qm.hasQuotaEditRights(ureq.getIdentity())) {
			OlatRootFolderImpl feedRoot = FileResourceManager.getInstance().getFileResourceRootImpl(entry.getOlatResource());
			Controller quotaCtrl = qm.getQuotaEditorInstance(ureq, wControl, feedRoot.getRelPath(), false);
			pane.appendEditor(pane.getTranslator().translate("tab.quota.edit"), quotaCtrl);
		}
	}
	
	@Override
	public RepositoryEntry copy(RepositoryEntry source, RepositoryEntry target) {
		OLATResource sourceResource = source.getOlatResource();
		OLATResource targetResource = target.getOlatResource();
		FeedManager.getInstance().copy(sourceResource, targetResource);
		return target;
	}

	@Override
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return FeedManager.getInstance().acquireLock(ores, identity);
	}

	@Override
	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry repoEntry) {
		// Apperantly, this method is used for backing up any user related content
		// (comments etc.) on deletion. Up to now, this doesn't exist in podcasts.
		return null;
	}

	@Override
	public boolean cleanupOnDelete(OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		// For now, notifications are not implemented since a podcast feed is meant
		// to be subscriped to anyway.
		// NotificationsManager.getInstance().deletePublishersOf(res);
		FeedManager.getInstance().delete(res);
		return true;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		FeedManager manager = FeedManager.getInstance();
		return manager.getFeedArchiveMediaResource(res);
	}

	@Override
	public Controller createDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return FileResourceManager.getInstance().getDetailsForm(ureq, wControl, res);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control) {
		// Return the launch controller. Owners and admins will be able to edit the
		// podcast 'inline'.
		return createLaunchController(re, ureq, control);
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		boolean isAdmin = ureq.getUserSession().getRoles().isOLATAdmin();
		boolean isOwner = RepositoryManager.getInstance().isOwnerOfRepositoryEntry(ureq.getIdentity(), re);	
		FeedSecurityCallback callback = new FeedResourceSecurityCallback(isAdmin, isOwner);
		FeedMainController podcastCtr = PodcastUIFactory.getInstance(ureq.getLocale()).createMainController(re.getOlatResource(), ureq, wControl, callback);
		LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(ureq, wControl, podcastCtr);
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, layoutCtr);
		return wrapper;
	}

	@Override
	public String getSupportedType() {
		return PodcastFileResource.TYPE_NAME;
	}

	@Override
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references", new String[] { referencesSummary }));
			return false;
		}
		return true;
	}

	@Override
	public void releaseLock(LockResult lockResult) {
		FeedManager.getInstance().releaseLock(lockResult);
	}

	@Override
	public boolean supportsDownload(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsEdit(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public boolean supportsLaunch(RepositoryEntry repoEntry) {
		return true;
	}

	@Override
	public StepsMainRunController createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		throw new AssertException("Trying to get wizard where no creation wizard is provided for this type.");
	}

	@Override
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl control, RepositoryEntry repositoryEntry) {
		// This was copied from WikiHandler. No specific close wizard is
		// implemented.
		throw new AssertException("not implemented");
	}

	@Override
	public boolean isLocked(OLATResourceable ores) {
		return FeedManager.getInstance().isLocked(ores);
	}
}