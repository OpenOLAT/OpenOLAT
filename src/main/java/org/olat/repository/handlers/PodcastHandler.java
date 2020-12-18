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
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.notifications.SubscriptionContext;
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
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.FileResource;
import org.olat.fileresource.types.PodcastFileResource;
import org.olat.fileresource.types.ResourceEvaluation;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedChangedEvent;
import org.olat.modules.webFeed.FeedResourceSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.manager.ValidatedURL;
import org.olat.modules.webFeed.manager.ValidatedURL.State;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.FeedRuntimeController;
import org.olat.modules.webFeed.ui.podcast.PodcastUIFactory;
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
 * Responsible class for handling any actions involving podcast resources.
 * 
 * <P>
 * Initial Date: Feb 25, 2009 <br>
 * 
 * @author Gregor Wassmann
 */
public class PodcastHandler implements RepositoryHandler {
	
	@Override
	public boolean supportCreate(Identity identity, Roles roles) {
		return true;
	}
	
	@Override
	public String getCreateLabelI18nKey() {
		return "new.podcast";
	}
	
	@Override
	public RepositoryEntry createResource(Identity initialAuthor, String displayname, String description,
			Object createObject, Organisation organisation, Locale locale) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OLATResourceable ores = FeedManager.getInstance().createPodcastResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public boolean supportImport() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(File file, String filename) {
		return PodcastFileResource.evaluate(file, filename);
	}

	@Override
	public boolean supportImportUrl() {
		return true;
	}

	@Override
	public ResourceEvaluation acceptImport(String url) {
		ResourceEvaluation eval = new ResourceEvaluation();
		ValidatedURL vUrl = FeedManager.getInstance().validateFeedUrl(url, PodcastFileResource.TYPE_NAME);
		if(vUrl.getState() == State.VALID) {
			eval.setValid(true);
			eval.setDisplayname(vUrl.getTitle());
		}
		return eval;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname, String description,
			boolean withReferences, Organisation organisation, Locale locale, File file, String filename) {
		
		OLATResource resource = OLATResourceManager.getInstance().createAndPersistOLATResourceInstance(new PodcastFileResource());
		File fResourceFileroot = FileResourceManager.getInstance().getFileResourceRootImpl(resource).getBasefile();
		File blogRoot = new File(fResourceFileroot, FeedManager.getInstance().getFeedKind(resource));
		FileResource.copyResource(file, filename, blogRoot);
		FeedManager.getInstance().importFeedFromXML(resource, true);
		RepositoryEntry re = CoreSpringFactory.getImpl(RepositoryService.class).create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		return re;
	}
	
	@Override
	public RepositoryEntry importResource(Identity initialAuthor, String initialAuthorAlt, String displayname,
			String description, Organisation organisation, Locale locale, String url) {
		
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		OLATResourceable ores = FeedManager.getInstance().createPodcastResource();
		OLATResource resource = OLATResourceManager.getInstance().findOrPersistResourceable(ores);
		RepositoryEntry re = repositoryService.create(initialAuthor, null, "", displayname, description,
				resource, RepositoryEntryStatusEnum.preparation, organisation);
		DBFactory.getInstance().commit();
		
		Feed feed = FeedManager.getInstance().loadFeed(ores);
		feed = FeedManager.getInstance().updateFeedMode(Boolean.TRUE, feed);
		FeedManager.getInstance().updateExternalFeedUrl(feed, url);
		DBFactory.getInstance().commit();
		
		return re;
	}
	
	@Override
	public RepositoryEntry copy(Identity author, RepositoryEntry source, RepositoryEntry target) {
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
	public boolean cleanupOnDelete(RepositoryEntry entry, OLATResourceable res) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		FeedManager.getInstance().deleteFeed(res);
		return true;
	}
	
	@Override
	public VFSContainer getMediaContainer(RepositoryEntry repoEntry) {
		return FileResourceManager.getInstance()
				.getFileResourceMedia(repoEntry.getOlatResource());
	}

	@Override
	public MediaResource getAsMediaResource(OLATResourceable res) {
		FeedManager manager = FeedManager.getInstance();
		return manager.getFeedArchiveMediaResource(res);
	}

	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl control, TooledStackedPanel toolbar) {
		return null;
	}

	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, RepositoryEntrySecurity reSecurity, UserRequest ureq, WindowControl wControl) {
		boolean isAdministrator = reSecurity.isEntryAdmin();	
		final FeedSecurityCallback callback = new FeedResourceSecurityCallback(isAdministrator);
		SubscriptionContext subsContext = new SubscriptionContext(re.getOlatResource(), re.getSoftkey());
		callback.setSubscriptionContext(subsContext);
		return new FeedRuntimeController(ureq, wControl, re, reSecurity,
				(uureq, wwControl, toolbarPanel, entry, security, assessmentMode) -> {
					CoreSpringFactory.getImpl(UserCourseInformationsManager.class)
						.updateUserCourseInformations(entry.getOlatResource(), uureq.getIdentity());
					return new FeedMainController(entry.getOlatResource(), uureq, wwControl, null, null,
						PodcastUIFactory.getInstance(uureq.getLocale()), callback, null);
				});
	}
	
	@Override
	public Controller createAssessmentDetailsController(RepositoryEntry re, UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbar, Identity assessedIdentity) {
		return null;
	}

	@Override
	public String getSupportedType() {
		return PodcastFileResource.TYPE_NAME;
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
	public void releaseLock(LockResult lockResult) {
		FeedManager.getInstance().releaseLock(lockResult);
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
	public boolean isLocked(OLATResourceable ores) {
		return FeedManager.getInstance().isLocked(ores);
	}

	@Override
	public void onDescriptionChanged(RepositoryEntry entry) {
		Feed feed = FeedManager.getInstance().updateFeedWithRepositoryEntry(entry);
		DBFactory.getInstance().commitAndCloseSession();
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(new FeedChangedEvent(feed.getKey()), feed);
	}
	
}