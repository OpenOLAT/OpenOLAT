/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.ui;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.CourseLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.CourseModule;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.DialogSecurityCallback;
import org.olat.course.nodes.dialog.model.DialogElementImpl;
import org.olat.course.nodes.dialog.security.SecurityCallbackFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.ForumController;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DialogElementController extends BasicController implements Activateable2 {

	
	private final Link downloadLink;
	private final Link editMetadataLink;
	private final Link openFileLink;
	private final VelocityContainer mainVC;
	
	private final ForumController forumCtr;
	private CloseableModalController cmc;
	private DialogFileEditMetadataController dialogFileEditMetadataCtrl;
	
	private DialogElement element;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private DialogElementsManager dialogElmsMgr;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public DialogElementController(UserRequest ureq, WindowControl wControl, DialogElement element,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, DialogSecurityCallback secCallback) {
		super(ureq, wControl);
		this.element = element;
		Forum forum = element.getForum();

		boolean isGuestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		if (!isGuestOnly) {
			SubscriptionContext subsContext = CourseModule.createSubscriptionContext(
					userCourseEnv.getCourseEnvironment(), courseNode, forum.getKey().toString());
			secCallback = SecurityCallbackFactory.create(secCallback, subsContext);
		}
		
		forumCtr = new ForumController(ureq, wControl, forum, secCallback, !isGuestOnly);
		listenTo(forumCtr);
		
		mainVC = createVelocityContainer("discussion");
		boolean isCurrentUserCreator = element.getAuthor().equals(getIdentity());
		
		downloadLink = LinkFactory.createLink("download", "download", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		downloadLink.setCustomDisplayText(translate("dialog.download.file"));
		downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadLink.setTarget("_blank");

		boolean canEditDialog = isCurrentUserCreator || secCallback.mayEditMessageAsModerator();
		mainVC.contextPut("isOwner", canEditDialog);
		editMetadataLink = LinkFactory.createLink("editMetadata", "editMetadata", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		editMetadataLink.setCustomDisplayText(translate("dialog.metadata.edit"));
		editMetadataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");

		String extension = FileUtils.getFileSuffix(element.getFilename());
		boolean canPreview = hasEditor(ureq, extension);
		mainVC.contextPut("canPreview", canPreview);
		openFileLink = LinkFactory.createLink("openFile", "openFile", getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
		openFileLink.setCustomDisplayText(translate("dialog.open.file"));
		openFileLink.setIconLeftCSS("o_icon o_icon-fw o_icon_link_extern");
		openFileLink.setNewWindow(true, true);

		VFSLeaf file = dialogElmsMgr.getDialogLeaf(element);
		if (file != null) {
			boolean thumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(file);
			if (thumbnailAvailable) {
				VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(file, 650, 1000, false);
				if (thumbnail != null) {
					mainVC.contextPut("isThumbnailAvailable", Boolean.TRUE);
					VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
					String thumbnailUrl = registerCacheableMapper(null, null, thumbnailMapper);
					mainVC.contextPut("thumbnailUrl", thumbnailUrl);
				}
			}
		}

		String mediaUrl = createDocumentUrl(ureq);
		mainVC.contextPut("mediaUrl", mediaUrl);

		loadForumCount();

		String authoredBy = StringHelper.escapeHtml(element.getAuthoredBy());
		Date lastActivity;
		List<Date> lastActivities = forumManager.getMessagesByForum(forum).stream().map(MessageLight::getLastModified).toList();
		if (lastActivities.isEmpty()) {
			lastActivity = element.getCreationDate();
		} else if (Collections.max(lastActivities).before(element.getLastModified())) {
			lastActivity = element.getLastModified();
		} else {
			lastActivity = Collections.max(lastActivities);
		}
		mainVC.contextPut("authoredBy", authoredBy != null ? authoredBy : "");
		mainVC.contextPut("lastActivity", Formatter.getInstance(getLocale()).formatDateAndTime(lastActivity));

		mainVC.contextPut("filename", StringHelper.escapeHtml(element.getFilename()));
		if(element.getSize() != null && element.getSize().longValue() > 0) {
			mainVC.contextPut("size", Formatter.formatBytes(element.getSize().longValue()));
		}
		String author = userManager.getUserDisplayName(element.getAuthor());
		mainVC.contextPut("author", StringHelper.escapeHtml(author));
		
		mainVC.put("forum", forumCtr.getInitialComponent());
		putInitialPanel(mainVC);
		
		addToHistory(ureq, OresHelper.createOLATResourceableInstance("Element", element.getKey()), null);
	}
	
	public DialogElement getElement() {
		return element;
	}

	private void loadForumCount() {
		int messageCount = forumManager.countMessagesByForumID(element.getForum().getKey());
		int threadCount = forumManager.countThreadsByForumID(element.getForum().getKey());
		mainVC.contextPut("messageCount", messageCount);
		mainVC.contextPut("threadCount", threadCount);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("open".equals(event.getCommand())) {
			doOpenFile(ureq);
		} else if(downloadLink == source) {
			doDownload(ureq);
		} else if (editMetadataLink == source) {
			doEditMetadata(ureq);
		} else if (openFileLink == source) {
			doOpenFile(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogFileEditMetadataCtrl) {
			if (event == Event.DONE_EVENT) {
				doUpdateFileDialog();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
		}  else if (event == Event.CHANGED_EVENT) {
			loadForumCount();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Message".equals(name)) {
			forumCtr.activate(ureq, entries, state);
		}
	}

	private void doUpdateFileDialog() {
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		String updatedFileName = dialogFileEditMetadataCtrl.getFileName();
		// add file extension
		updatedFileName = updatedFileName + "." + FileUtils.getFileSuffix(element.getFilename());
		String updatedAuthoredBy = dialogFileEditMetadataCtrl.getAuthoredBy();
		VFSItem existingVFSItem = dialogContainer.resolve(element.getFilename());
		existingVFSItem.rename(updatedFileName);
		element = dialogElmsMgr.updateDialogElement((DialogElementImpl) element, updatedFileName, updatedAuthoredBy);
		mainVC.contextPut("filename", StringHelper.escapeHtml(existingVFSItem.getName()));
		mainVC.contextPut("authoredBy", StringHelper.escapeHtml(updatedAuthoredBy));
	}

	private void doDownload(UserRequest ureq) {
		VFSLeaf file = dialogElmsMgr.getDialogLeaf(element);
		if(file != null) {
			VFSMediaResource mediaResource = new VFSMediaResource(file);
			mediaResource.setDownloadable(true);
			ureq.getDispatchResult().setResultingMediaResource(mediaResource);
			ThreadLocalUserActivityLogger.log(CourseLoggingAction.DIALOG_ELEMENT_FILE_DOWNLOADED, getClass(),
					LoggingResourceable.wrapBCFile(element.getFilename()));
		} else {
			ureq.getDispatchResult().setResultingMediaResource(new NotFoundMediaResource());
			logError("No file to discuss: " + element, null);
		}
	}

	private void doEditMetadata(UserRequest ureq) {
		removeAsListenerAndDispose(dialogFileEditMetadataCtrl);
		dialogFileEditMetadataCtrl = new DialogFileEditMetadataController(ureq, getWindowControl(), element);
		listenTo(dialogFileEditMetadataCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), dialogFileEditMetadataCtrl.getInitialComponent(),
				true, translate("dialog.metadata.edit"));
		listenTo(cmc);
		cmc.activate();
	}

	private boolean hasEditor(UserRequest ureq, String extension) {
		return docEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), extension, DocEditor.Mode.VIEW, true, true);
	}

	/**
	 * @param ureq
	 * @return String value, containing url directing to dialog file
	 */
	private String createDocumentUrl(UserRequest ureq) {
		VFSContainer dialogContainer = dialogElmsMgr.getDialogContainer(element);
		VFSItem vfsItem = dialogContainer.resolve(element.getFilename());
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withMode(DocEditor.Mode.VIEW)
				.withDownloadEnabled(true)
				.build((VFSLeaf) vfsItem);
		return docEditorService.prepareDocumentUrl(ureq.getUserSession(), configs);
	}

	private void doOpenFile(UserRequest ureq) {
		String url = createDocumentUrl(ureq);
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
	}
}