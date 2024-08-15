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
package org.olat.core.commons.services.commentAndRating.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.creator.UserAvatarDisplayControllerCreator;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller can display a comment and all it's replies
 * <ul>
 * <li>UserCommentDisplayController.DELETED_EVENT</li>
 * <li>UserCommentDisplayController.COMMENT_COUNT_CHANGED</li>
 * <li>UserCommentDisplayController.COMMENT_DATAMODEL_DIRTY</li>
 * </ul>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentDisplayController extends BasicController {

	private final CommentAndRatingSecurityCallback securityCallback;

	// GUI container
	private final VelocityContainer userCommentDisplayVC;

	// Data model
	private final List<UserComment> allComments;
	private List<Controller> replyControllers;
	private final UserComment userComment;
	private DialogBoxController deleteDialogCtrl;

	// Events
	public static final Event DELETED_EVENT = new Event("comment_deleted");
	public static final Event COMMENT_COUNT_CHANGED = new Event("comment_count_changed");
	public static final Event COMMENT_DATAMODEL_DIRTY = new Event("comment_datamode_dirty");

	// Reply workflow
	private Link replyLink;
	private UserCommentFormController replyCommentFormCtrl;
	private ToolsController toolsCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private Controller docEditorCtrl;

	private final String resSubPath;
	private final OLATResourceable ores;
	private final PublishingInformations publishingInformations;

	@Autowired
	private UserManager userManager;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	UserCommentDisplayController(UserRequest ureq, WindowControl wControl,
								 UserComment userComment, List<UserComment> allComments,
								 OLATResourceable ores, String resSubPath, CommentAndRatingSecurityCallback securityCallback,
								 PublishingInformations publishingInformations) {
		super(ureq, wControl);
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.userComment = userComment;
		this.allComments = allComments;
		this.securityCallback = securityCallback;
		this.publishingInformations = publishingInformations;

		// Init view
		userCommentDisplayVC = createVelocityContainer("userCommentDisplay");
		initVelocityContainer(ureq);

		// Creator information
		initCreatorInformation();

		// Portrait
		initPortraits(ureq);

		// Reply/Tools link
		initLinks();

		// Add all replies
		replyControllers = new ArrayList<>();
		buildReplyComments(ureq);
		userCommentDisplayVC.contextPut("replyControllers", replyControllers);

		// Initialize reply comment form
		initializeReplyCommentForm(ureq);

		putInitialPanel(this.userCommentDisplayVC);
	}

	/**
	 * Initializes the VelocityContainer with necessary context values
	 */
	private void initVelocityContainer(UserRequest ureq) {
		userCommentDisplayVC.contextPut("formatter", Formatter.getInstance(getLocale()));
		userCommentDisplayVC.contextPut("securityCallback", securityCallback);
		userCommentDisplayVC.contextPut("comment", userComment);

		List<VFSLeaf> commentLeafs = commentAndRatingService.getCommentLeafs(userComment);
		userCommentDisplayVC.contextPut("commentLeafs", commentLeafs);

		if (commentLeafs != null && !commentLeafs.isEmpty()) {
			for (VFSLeaf commentLeaf : commentLeafs) {
				processCommentLeaf(ureq, commentLeaf);
			}
		}
	}

	/**
	 * Processes a single comment leaf to determine if it has an image thumbnail,
	 * and sets up the preview and download links.
	 *
	 * @param ureq
	 * @param commentLeaf The VFSLeaf representing a comment attachment.
	 */
	private void processCommentLeaf(UserRequest ureq, VFSLeaf commentLeaf) {
		String fileSuffix = FileUtils.getFileSuffix(commentLeaf.getRelPath()).toUpperCase();
		userCommentDisplayVC.contextPut("fileSuffix_" + commentLeaf.getMetaInfo().getKey(), fileSuffix);

		String fileNameWithoutSuffix = FileUtils.getFileNameWithoutSuffix(commentLeaf.getName());
		userCommentDisplayVC.contextPut("fileName_" + commentLeaf.getMetaInfo().getKey(), fileNameWithoutSuffix);

		if (isImageFile(fileSuffix)) {
			addThumbnailIfAvailable(ureq, commentLeaf);
		}

		addLinksForLeaf(commentLeaf);
	}

	/**
	 * Checks if the file suffix represents an image file.
	 *
	 * @param fileSuffix The file suffix.
	 * @return true if the file is an image, false otherwise.
	 */
	private boolean isImageFile(String fileSuffix) {
		return fileSuffix.equalsIgnoreCase("jpg")
				|| fileSuffix.equalsIgnoreCase("jpeg")
				|| fileSuffix.equalsIgnoreCase("png")
				|| fileSuffix.equalsIgnoreCase("gif");
	}

	/**
	 * Adds a thumbnail to the context if available for the given comment leaf.
	 *
	 * @param ureq
	 * @param commentLeaf The VFSLeaf representing a comment attachment.
	 */
	private void addThumbnailIfAvailable(UserRequest ureq, VFSLeaf commentLeaf) {
		boolean isThumbnailAvailable = vfsRepositoryService.isThumbnailAvailable(commentLeaf);
		if (isThumbnailAvailable) {
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(commentLeaf, 150, 100, false);
			VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
			String thumbnailUrl = registerCacheableMapper(ureq, null, thumbnailMapper);
			userCommentDisplayVC.contextPut("thumbnail_" + commentLeaf.getMetaInfo().getKey(), thumbnailUrl);
		}
	}

	/**
	 * Adds preview and download links for the given comment leaf.
	 *
	 * @param commentLeaf The VFSLeaf representing a comment attachment.
	 */
	private void addLinksForLeaf(VFSLeaf commentLeaf) {
		String previewLinkId = "prev-" + commentLeaf.getMetaInfo().getKey();
		Link previewLink = LinkFactory.createLink(previewLinkId,  "preview", getTranslator(), userCommentDisplayVC, this, Link.BUTTON + Link.NONTRANSLATED);
		previewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_preview");
		previewLink.setGhost(true);
		previewLink.setCustomDisplayText("");
		previewLink.setUserObject(commentLeaf);

		String downloadLinkId = "download-" + commentLeaf.getMetaInfo().getKey();
		Link downloadLink = LinkFactory.createLink(downloadLinkId, "download", getTranslator(), userCommentDisplayVC, this, Link.BUTTON + Link.NONTRANSLATED);
		downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadLink.setGhost(true);
		downloadLink.setCustomDisplayText("");
		downloadLink.setUserObject(commentLeaf);
	}

	private void initCreatorInformation() {
		TextComponent creator = TextFactory.createTextComponentFromI18nKey("creator", null, null, null, true, userCommentDisplayVC);
		String name = userManager.getUserDisplayName(userComment.getCreator());
		creator.setText(name);
	}

	private void initPortraits(UserRequest ureq) {
		if (CoreSpringFactory.containsBean(UserAvatarDisplayControllerCreator.class.getName())) {
			UserAvatarDisplayControllerCreator avatarControllerCreator = (UserAvatarDisplayControllerCreator) CoreSpringFactory.getBean(UserAvatarDisplayControllerCreator.class);
			Controller avatarCtrl = avatarControllerCreator.createController(ureq, getWindowControl(), userComment.getCreator(), false, true);
			listenTo(avatarCtrl);
			userCommentDisplayVC.put("avatarCtrl", avatarCtrl.getInitialComponent());

			Controller avatarCtrlOwn = avatarControllerCreator.createController(ureq, getWindowControl(), getIdentity(), false, true);
			listenTo(avatarCtrlOwn);
			userCommentDisplayVC.put("avatarCtrlOwn", avatarCtrlOwn.getInitialComponent());
		}
	}

	private void initLinks() {
		if (securityCallback.canReplyToComment(userComment)) {
			replyLink = LinkFactory.createCustomLink("replyLink", "reply", "comments.coment.reply", Link.BUTTON, userCommentDisplayVC, this);
			replyLink.setGhost(true);
			replyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reply");
			replyLink.setElementCssClass("o_reply");
		}
		if (securityCallback.canDeleteComment(userComment)) {
			Link toolsLink = LinkFactory.createCustomLink("tools", "tools", "", Link.NONTRANSLATED, userCommentDisplayVC, this);
			toolsLink.setElementCssClass("o_tools");
			toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsLink.setGhost(true);
		}
	}

	private void initializeReplyCommentForm(UserRequest ureq) {
		replyCommentFormCtrl = new UserCommentFormController(ureq, getWindowControl(), userComment, null,
				ores, resSubPath, publishingInformations);
		listenTo(replyCommentFormCtrl);
		userCommentDisplayVC.contextPut("replyCommentFormCtrl", replyCommentFormCtrl.getInitialFormItem().getName());
	}


	/**
	 * Used in velocity container to render replies
	 * 
	 * @return String with the name used for this comment as velocity component
	 *         name
	 */
	public String getViewCompName() {
		return "comment_" + this.userComment.getKey();
	}
	
	/**
	 * Internal helper to build the view controller for the replies
	 * @param ureq
	 */
	private void buildReplyComments(UserRequest ureq) {
		// First remove all old replies
		clearOldReplies();

		// Build replies again
		for (UserComment reply : allComments) {
			if (reply.getParent() == null) continue;
			if (reply.getParent().getKey().equals(userComment.getKey())) {
				createReplyCtrl(ureq, reply);
			}
			createReplyParentLink(reply);
		}
	}

	private void clearOldReplies() {
		for (Controller replyController : replyControllers) {
			removeAsListenerAndDispose(replyController);
		}
		replyControllers.clear();
	}

	private void createReplyCtrl(UserRequest ureq, UserComment reply) {
		UserCommentDisplayController replyCtrl = new UserCommentDisplayController(ureq, getWindowControl(), reply, allComments,
				ores, resSubPath, securityCallback, publishingInformations);
		replyControllers.add(replyCtrl);
		listenTo(replyCtrl);
		userCommentDisplayVC.put(replyCtrl.getViewCompName(), replyCtrl.getInitialComponent());
	}

	private void createReplyParentLink(UserComment reply) {
		if (reply.getParent() != null) {
			String name = userManager.getUserDisplayName(reply.getParent().getCreator());
			Link replyParentUserLink = LinkFactory.createLink("replyTo_" + reply.getParent().getKey(), "replyTo_" + reply.getParent().getKey(),
					"replyParentUserLink", "@" + name, getTranslator(), userCommentDisplayVC, this, Link.LINK + Link.NONTRANSLATED);
			replyParentUserLink.setUserObject(reply.getParent().getCreator());
			replyParentUserLink.setAjaxEnabled(false);

			userCommentDisplayVC.contextPut("parentUserName_" + reply.getParent().getKey(), "@" + name);
			userCommentDisplayVC.contextPut("parentUserIdentity_" + reply.getParent().getKey(), reply.getParent().getCreator());
		}
	}

	@Override
	protected void doDispose() {
		// Child controllers disposed by basic controller
		replyControllers = null;
        super.doDispose();
	}

	private void showUserInfo(UserRequest ureq) {
		ControllerCreator ctrlCreator = (lureq, lwControl) ->
				new UserInfoMainController(lureq, lwControl, userComment.getParent().getCreator(),
						true, false
				);
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrl = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
		//open in new browser window
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrl);
		pbw.open(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == replyLink) {
			doReply(ureq);	
		} else if (source instanceof Link link) {
			handleLinkEvent(ureq, link);
		} else if (event.getCommand().equals("showuserinfo")) {
			showUserInfo(ureq);
		} else if (source == userCommentDisplayVC) {
			handleUserCommentDisplayEvent(ureq, event);
		}
	}

	/**
	 * Handles events triggered by links
	 *
	 * @param ureq
	 * @param link The source link component
	 */
	private void handleLinkEvent(UserRequest ureq, Link link) {
		switch (link.getCommand()) {
			case "tools":
				doOpenTools(ureq, link.getDispatchID());
				break;
			case "preview":
				VFSLeaf previewAttachment = (VFSLeaf) link.getUserObject();
				doPreviewAttachment(ureq, previewAttachment);
				break;
			case "download":
				VFSLeaf downloadAttachment = (VFSLeaf) link.getUserObject();
				doDownloadAttachment(ureq, downloadAttachment);
				break;
			default:
				break;
		}
	}

	/**
	 * Handles events triggered by user comments displayed in the VelocityContainer.
	 *
	 * @param ureq
	 * @param event The event triggered.
	 */
	private void handleUserCommentDisplayEvent(UserRequest ureq, Event event) {
		String fileMetaInfoKey = event.getCommand();
		List<VFSLeaf> commentLeafs = commentAndRatingService.getCommentLeafs(userComment);
		VFSLeaf attachmentFile = findAttachmentByMetaInfoKey(commentLeafs, fileMetaInfoKey);
		if (attachmentFile != null) {
			doPreviewAttachment(ureq, attachmentFile);
		}
	}

	/**
	 * Finds an attachment by its meta info key.
	 *
	 * @param commentLeafs  The list of VFSLeaf objects representing comment attachments.
	 * @param metaInfoKey The meta info key to search for.
	 * @return The VFSLeaf object if found, otherwise null.
	 */
	private VFSLeaf findAttachmentByMetaInfoKey(List<VFSLeaf> commentLeafs, String metaInfoKey) {
		return commentLeafs.stream()
				.filter(cl -> String.valueOf(cl.getMetaInfo().getKey()).equals(metaInfoKey))
				.findFirst()
				.orElse(null);
	}

	private void doPreviewAttachment(UserRequest ureq, VFSLeaf attachmentFile) {
		if (folderModule.isForceDownload()) {
			doDownloadAttachment(ureq, attachmentFile);
		} else {
			DocEditorConfigs configs = DocEditorConfigs.builder()
					.withMode(DocEditor.Mode.VIEW)
					.withDownloadEnabled(true)
					.build(attachmentFile);

			docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW).getController();
			listenTo(docEditorCtrl);
		}
	}

	private void doDownloadAttachment(UserRequest ureq, VFSLeaf attachmentFile) {
		VFSMediaResource mediaResource = new VFSMediaResource(attachmentFile);
		mediaResource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(mediaResource);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteDialogCtrl) {
			handleDeleteDialogEvent(ureq, event);
		} else if (source == replyCommentFormCtrl) {
			handleReplyCommentFormEvent(ureq, event);
		} else if (source instanceof UserCommentDisplayController replyCtr) {
			handleReplyCommentChangeCtrlEvent(ureq, replyCtr, event);
		} else if (source == docEditorCtrl) {
			cleanUp();
		}
	}

	private void handleDeleteDialogEvent(UserRequest ureq, Event event) {
		boolean hasReplies = ((Boolean) deleteDialogCtrl.getUserObject()).booleanValue();
		if (DialogBoxUIFactory.isClosedEvent(event)) {
			// Nothing to do
		} else {
			int buttonPos = DialogBoxUIFactory.getButtonPos(event);
			if (buttonPos == 0) {
				// Do delete
				commentAndRatingService.deleteComment(userComment, false);
				allComments.remove(userComment);
				fireEvent(ureq, DELETED_EVENT);
				// Inform top level view that it needs to rebuild due to comments that are now unlinked
				if (hasReplies) {
					fireEvent(ureq, COMMENT_DATAMODEL_DIRTY);
				}
			} else if (buttonPos == 1 && hasReplies) {
				// Delete current comment and all replies. Notify parent, probably needs full redraw
				commentAndRatingService.deleteComment(userComment, true);
				allComments.remove(userComment);
				fireEvent(ureq, DELETED_EVENT);
			} else if (buttonPos == 1 && !hasReplies) {
				// Nothing to do, cancel button
			}
		}
		// Cleanup delete dialog
		removeAsListenerAndDispose(deleteDialogCtrl);
		deleteDialogCtrl = null;
	}

	private void handleReplyCommentFormEvent(UserRequest ureq, Event event) {
		if (event == Event.CHANGED_EVENT) {
			// Update view
			UserComment newReply = replyCommentFormCtrl.getComment();
			allComments.add(newReply);
			// Create child controller
			UserCommentDisplayController replyCtrl = new UserCommentDisplayController(ureq, getWindowControl(), newReply, allComments, ores, resSubPath, securityCallback, publishingInformations);
			replyControllers.add(replyCtrl);
			listenTo(replyCtrl);
			userCommentDisplayVC.put(replyCtrl.getViewCompName(), replyCtrl.getInitialComponent());
			// notify parent
			fireEvent(ureq, COMMENT_COUNT_CHANGED);
		} else if (event == Event.FAILED_EVENT) {
			// Reply failed - reload everything
			fireEvent(ureq, COMMENT_DATAMODEL_DIRTY);
		}
		cleanUp();
	}

	private void handleReplyCommentChangeCtrlEvent(UserRequest ureq, UserCommentDisplayController replyCtr, Event event) {
		if (event == DELETED_EVENT) {
			// Remove comment from view and re-render.
			replyControllers.remove(replyCtr);
			userCommentDisplayVC.remove(replyCtr.getInitialComponent());
			removeAsListenerAndDispose(replyCtr);
			// Notify parent about this - probably needs complete reload of data model
			fireEvent(ureq, COMMENT_COUNT_CHANGED);
		} else if (event == COMMENT_COUNT_CHANGED || event == COMMENT_DATAMODEL_DIRTY) {
			// Forward to parent, nothing to do here
			fireEvent(ureq, event);
		}
	}
	
	private void cleanUp() {
		userCommentDisplayVC.remove("replyCommentFormCtrl");
		removeAsListenerAndDispose(replyCommentFormCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		replyCommentFormCtrl = null;
		toolsCalloutCtrl = null;
		docEditorCtrl = null;
		toolsCtrl = null;
	}

	private void doOpenTools(UserRequest ureq, String dispatchID) {
		toolsCtrl = new ToolsController(ureq, getWindowControl());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), dispatchID, "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		// Init delete workflow
		List<String> buttonLabels = new ArrayList<>();
		boolean hasReplies = false;
		for (UserComment comment : allComments) {
			if (comment.getParent() != null && comment.getParent().getKey().equals(userComment.getKey())) {
				hasReplies = true;
				break;
			}
		}
		if (hasReplies) {
			buttonLabels.add(translate("comments.button.delete.without.replies"));
			buttonLabels.add(translate("comments.button.delete.with.replies"));
		} else {
			buttonLabels.add(translate("delete"));
		}
		buttonLabels.add(translate("cancel"));								
		String deleteText;
		if (hasReplies) {
			deleteText = translate("comments.dialog.delete.with.replies");				
		} else {
			deleteText = translate("comments.dialog.delete");
		}
		deleteDialogCtrl = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("comments.dialog.delete.title"), deleteText, buttonLabels);
		listenTo(deleteDialogCtrl);
		deleteDialogCtrl.activate();
		// Add replies info as user object to retrieve it later when evaluating the events
		deleteDialogCtrl.setUserObject(Boolean.valueOf(hasReplies));
	}
	
	private void doReply(UserRequest ureq) {
		// Init reply workflow
		replyCommentFormCtrl = new UserCommentFormController(ureq, getWindowControl(), userComment, null,
				ores, resSubPath, publishingInformations);
		listenTo(replyCommentFormCtrl);
		userCommentDisplayVC.put("replyCommentFormCtrl", replyCommentFormCtrl.getInitialComponent());
	}

	private class ToolsController extends BasicController {

		private Link deleteLink;

		protected ToolsController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);

			VelocityContainer mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>();
			mainVC.contextPut("links", links);

			// Delete link
			if(securityCallback.canDeleteComment(userComment)) {
				deleteLink = LinkFactory.createCustomLink("deleteLink", "delete", "delete", Link.LINK, mainVC, this);
				deleteLink.setElementCssClass("o_delete");
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");
				links.add(deleteLink.getComponentName());
			}

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (source == deleteLink) {
				close();
				doConfirmDelete(ureq);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}
