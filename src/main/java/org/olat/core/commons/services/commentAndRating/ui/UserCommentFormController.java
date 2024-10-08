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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.quota.QuotaConstants;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.folder.ui.FileBrowserController;
import org.olat.core.commons.services.folder.ui.FileBrowserSelectionMode;
import org.olat.core.commons.services.folder.ui.FolderQuota;
import org.olat.core.commons.services.folder.ui.event.FileBrowserSelectionEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This controller offers a form to edit a comment. When the user presses save,
 * the controller saves the comment to the manager.
 * <p>
 * When initialized without a comment, the form renders as empty message.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>Event.CANCELLED_EVENT when the user canceled the operation</li>
 * <li>Event.CHANGED_EVENT when the user pressed save (new comment or update).
 * Use getComment() to get the updated or new comment object.</li>
 * <li>Event.FAILED_EVENT when the comment could not be saved (probably due to
 * the fact that the comment or the parent has been deleted)</li>
 * </ul>
 * <p>
 * Initial Date: 24.11.2009 <br>
 *
 * @author gnaegi
 */
public class UserCommentFormController extends FormBasicController {

	private static final int MAX_COMMENT_LENGTH = 4000;

	private final String resSubPath;
	private boolean subscribeOnce = true;

	private RichTextElement commentElem;
	private FormSubmit submitButton;
	private FormCancel cancelButton;
	private FormLink preButtonArea;
	private FormLink attachFileButton;

	private final UserComment parentComment;
	private final OLATResourceable ores;
	private final PublishingInformations publishingInformations;
	private final List<VFSLeaf> uploadedAttachmentLeafs = new ArrayList<>();
	private final Map<VFSLeaf, String> uploadedFiles = new HashMap<>();
	private UserComment toBeUpdatedComment;

	private Controller docEditorCtrl;
	private CloseableModalController cmc;
	private FileBrowserController addFromBrowserCtrl;

	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private DocEditorService docEditorService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	/**
	 * Constructor for a user comment form controller. Use the
	 * UserCommentsAndRatingsController to work with user comments.
	 *
	 * @param ureq
	 * @param wControl
	 * @param parentComment
	 * @param toBeUpdatedComment
	 * @param ores
	 * @param resSubPath
	 * @param publishingInformations
	 */
	UserCommentFormController(UserRequest ureq, WindowControl wControl,
							  UserComment parentComment, UserComment toBeUpdatedComment,
							  OLATResourceable ores, String resSubPath,
							  PublishingInformations publishingInformations) {
		super(ureq, wControl, "userCommentForm");
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.parentComment = parentComment;
		this.toBeUpdatedComment = toBeUpdatedComment;
		this.publishingInformations = publishingInformations;
		initForm(ureq);
	}

	private static boolean containsEmptyPTag(String str) {
		String startTag = "<p>";
		String endTag = "</p>";

		// Find the first occurrence of the start tag
		int startIndex = str.indexOf(startTag);
		while (startIndex != -1) {
			// Find the corresponding end tag
			int endIndex = str.indexOf(endTag, startIndex + startTag.length());
			if (endIndex != -1) {
				// Get the content between the tags
				String content = str.substring(startIndex + startTag.length(), endIndex);
				// Check if the content is empty or contains only whitespace
				if (!StringHelper.containsNonWhitespace(content)) {
					return true;
				}
				// Move past this end tag to search for the next start tag
				startIndex = str.indexOf(startTag, endIndex + endTag.length());
			} else {
				// No matching end tag found, stop searching
				break;
			}
		}
		return false;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (parentComment == null) {
			// fake text area, which is clickable and tabable (for a11y reasons)
			preButtonArea = uifactory.addFormLink("comments.form.placeholder", formLayout, Link.BUTTON);
			preButtonArea.setIconLeftCSS("o_icon o_icon-fw o_icon_pencil");
			preButtonArea.setElementCssClass("o_fake_comment_text_area");
		}

		commentElem = uifactory.addRichTextElementForStringData(
				"commentElem", null, "", 12, -1,
				false, null, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		commentElem.setPlaceholderKey("comments.form.placeholder", null);
		commentElem.setMaxLength(MAX_COMMENT_LENGTH);
		commentElem.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);
		commentElem.setVisible(parentComment != null);

		FormLayoutContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add(buttonContainer);

		submitButton = uifactory.addFormSubmitButton("submit", "comments.button.submit", buttonContainer);
		submitButton.setVisible(parentComment != null);
		cancelButton = uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		cancelButton.setVisible(parentComment != null);
		attachFileButton = uifactory.addFormLink("comments.button.attachment", buttonContainer, Link.BUTTON);
		attachFileButton.setIconLeftCSS("o_icon o_icon-fw o_icon_paperclip");
		attachFileButton.setGhost(true);
		attachFileButton.setVisible(parentComment != null);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		String commentText = commentElem.getValue();

		if (!StringHelper.containsNonWhitespace(commentText) || containsEmptyPTag(commentText)) {
			commentElem.setErrorKey("comments.form.input.invalid");
			allOk = false;
		} else if (commentText.length() <= MAX_COMMENT_LENGTH) {
			commentElem.clearError();
		} else {
			commentElem.setErrorKey("input.toolong", Integer.toString(MAX_COMMENT_LENGTH));
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == preButtonArea || (source == commentElem && event.equals(Event.CANCELLED_EVENT))) {
			toggleCommentFormElem();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == attachFileButton) {
			doAddFromBrowser(ureq);
		} else if (source instanceof FormLink formLink) {
			handleFormLinkEvent(ureq, formLink);
		}
	}

	/**
	 * Handles events triggered by form links.
	 *
	 * @param ureq      The user request.
	 * @param formLink  The form link that triggered the event.
	 */
	private void handleFormLinkEvent(UserRequest ureq, FormLink formLink) {
		String command = formLink.getCmd();
		VFSLeaf attachmentFile = (VFSLeaf) formLink.getUserObject();

		switch (command) {
			case "preview":
				doPreviewAttachment(ureq, attachmentFile);
				break;
			case "delete":
				doDeleteAttachment(attachmentFile);
				break;
			default:
				break;
		}
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

	/**
	 * Deletes the given attachment file
	 *
	 * @param attachmentFile file to delete
	 */
	private void doDeleteAttachment(VFSLeaf attachmentFile) {
		uploadedFiles.remove(attachmentFile);
		attachmentFile.deleteSilently();
	}

	private void doDownloadAttachment(UserRequest ureq, VFSLeaf attachmentFile) {
		VFSMediaResource mediaResource = new VFSMediaResource(attachmentFile);
		mediaResource.setDownloadable(true);
		ureq.getDispatchResult().setResultingMediaResource(mediaResource);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (addFromBrowserCtrl == source) {
			handleAddFromBrowserEvent(ureq, event);
		} else if (source == docEditorCtrl || source == cmc) {
			cleanUp();
		}
	}

	/**
	 * Handles the event triggered by adding files from the browser
	 *
	 * @param ureq
	 * @param event The event that was triggered
	 */
	private void handleAddFromBrowserEvent(UserRequest ureq, Event event) {
		if (event instanceof FileBrowserSelectionEvent selectionEvent) {
			List<VFSLeaf> selectedFiles = selectionEvent.getVfsItems().stream()
					.filter(VFSLeaf.class::isInstance)
					.map(VFSLeaf.class::cast)
					.toList();

			uploadedAttachmentLeafs.addAll(selectedFiles);
			doCopyTemp(ureq);
		}
		cmc.deactivate();
		cleanUp();
	}


	/**
	 * Clean up temp dir, which only serves for uploading.
	 * Clean up e.g. in case of cancel or disposal
	 */
	private void cleanUpTempDir() {
		if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
			VFSContainer tempCont = VFSManager.olatRootContainer("/comments/temp/");
			VFSManager.deleteContainersAndLeaves(tempCont, true, true, false);
			flc.contextRemove("uploadedFiles");
			uploadedFiles.clear();
			uploadedAttachmentLeafs.clear();
		}
	}

	private void doAddFromBrowser(UserRequest ureq) {
		if (guardModalController(addFromBrowserCtrl)) return;

		Quota commentDefQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_COMMENTS);
		FolderQuota folderQuota = new FolderQuota(ureq, commentDefQuota, 0L);

		removeAsListenerAndDispose(addFromBrowserCtrl);
		addFromBrowserCtrl = new FileBrowserController(ureq, getWindowControl(), FileBrowserSelectionMode.sourceMulti,
				folderQuota, translate("add"));
		listenTo(addFromBrowserCtrl);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				addFromBrowserCtrl.getInitialComponent(), true, translate("browser.add"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCopyTemp(UserRequest ureq) {
		List<VFSLeaf> leafs = new ArrayList<>(uploadedAttachmentLeafs);
		for (VFSLeaf uploadedFileLeaf : leafs) {
			if (!FileUtils.validateFilename(uploadedFileLeaf.getName())) {
				uploadedAttachmentLeafs.remove(uploadedFileLeaf);
				attachFileButton.setErrorKey("file.invalid.error", uploadedFileLeaf.getName());
				attachFileButton.showError(true);
			} else {
				handleValidFile(ureq, uploadedFileLeaf);
			}
		}

		if (!uploadedFiles.isEmpty()) {
			flc.contextPut("uploadedFiles", uploadedFiles);
		}
	}

	/**
	 * Handles valid file uploads by creating a thumbnail, setting up preview and delete links, and forging metadata
	 *
	 * @param ureq
	 * @param uploadedFileLeaf The file with a valid name that was uploaded
	 */
	private void handleValidFile(UserRequest ureq, VFSLeaf uploadedFileLeaf) {
		VFSContainer tempCont = VFSManager.olatRootContainer("/comments/temp/");
		uploadedFileLeaf = uploadNewFile(uploadedFileLeaf, uploadedFileLeaf.getName(), tempCont, getIdentity());

		if (uploadedFileLeaf != null) {
			String thumbnailUrl = createThumbnail(ureq, uploadedFileLeaf);
			// put thumbnailUrl (or null) as value for the uploadedFile
			uploadedFiles.put(uploadedFileLeaf, thumbnailUrl);
			createFileLinks(uploadedFileLeaf);
			forgeFileMetadata(ureq, uploadedFileLeaf);
		}
	}


	/**
	 * Creates a thumbnail for the uploaded file if available
	 *
	 * @param ureq
	 * @param uploadedFileLeaf The file for which to create a thumbnail
	 * @return The URL of the thumbnail, or null if no thumbnail is available
	 */
	private String createThumbnail(UserRequest ureq, VFSLeaf uploadedFileLeaf) {
		if (vfsRepositoryService.isThumbnailAvailable(uploadedFileLeaf)) {
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(uploadedFileLeaf, 150, 100, false);
			VFSMediaMapper thumbnailMapper = new VFSMediaMapper(thumbnail);
			return registerCacheableMapper(ureq, null, thumbnailMapper);
		}
		return null;
	}

	/**
	 * Creates preview and delete links for the uploaded file
	 *
	 * @param uploadedFileLeaf The uploaded file
	 */
	private void createFileLinks(VFSLeaf uploadedFileLeaf) {
		String previewLinkId = "prev-" + uploadedFileLeaf.getMetaInfo().getKey();
		FormLink previewLink = uifactory.addFormLink(previewLinkId, "preview", uploadedFileLeaf.getName(), null, flc, Link.NONTRANSLATED);
		previewLink.setUserObject(uploadedFileLeaf);

		String deleteLinkId = "delete-" + uploadedFileLeaf.getMetaInfo().getKey();
		FormLink deleteLink = uifactory.addFormLink(deleteLinkId, "delete", "delete", null, flc, Link.BUTTON);
		deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_trash");
		deleteLink.setElementCssClass("o_sel_comment_attachment_delete");
		deleteLink.setGhost(true);
		deleteLink.setUserObject(uploadedFileLeaf);
	}

	/**
	 * Forges metadata for the uploaded file and adds it to the context
	 *
	 * @param ureq
	 * @param uploadedFileLeaf The uploaded file
	 */
	private void forgeFileMetadata(UserRequest ureq, VFSLeaf uploadedFileLeaf) {
		String formattedFileSize = Formatter.formatBytes(uploadedFileLeaf.getSize());
		String fileSuffix = FileUtils.getFileSuffix(uploadedFileLeaf.getRelPath()).toUpperCase();
		String fileDate = Formatter.getInstance(ureq.getLocale()).formatDateAndTime(uploadedFileLeaf.getMetaInfo().getFileLastModified());
		String fileCreator = UserManager.getInstance().getUserDisplayName(uploadedFileLeaf.getMetaInfo().getFileInitializedBy());
		String metaData = translate("attachment.metadata", fileSuffix, fileDate, fileCreator, formattedFileSize);
		flc.contextPut("metaData_" + uploadedFileLeaf.getMetaInfo().getKey(), metaData);
	}

	private VFSLeaf uploadNewFile(VFSLeaf uploadedLeaf, String filename, VFSContainer uploadVFSContainer, Identity publishedBy) {
		// save file and finish
		VFSLeaf newFile = uploadVFSContainer.createChildLeaf(filename);

		if (newFile != null) {
			try {
				VFSManager.copyContent(uploadedLeaf, newFile, true, publishedBy);
			} catch (Exception e) {
				return null;
			}
		}

		return newFile;
	}

	private void doFinalizeAttachmentUpload(VFSContainer commentContainer) {
		for (VFSLeaf vfsLeaf : uploadedFiles.keySet()) {
			VFSManager.copyContent(vfsLeaf.getParentContainer(), commentContainer);
		}
	}

	private void toggleCommentFormElem() {
		if (parentComment == null) {
			preButtonArea.setVisible(!preButtonArea.isVisible());
			commentElem.setVisible(!commentElem.isVisible());
			submitButton.setVisible(!submitButton.isVisible());
			cancelButton.setVisible(!cancelButton.isVisible());
			attachFileButton.setVisible(!attachFileButton.isVisible());
			commentElem.setFocus(commentElem.isVisible());
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(addFromBrowserCtrl);
		removeAsListenerAndDispose(docEditorCtrl);
		removeAsListenerAndDispose(cmc);
		addFromBrowserCtrl = null;
		docEditorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String commentText = commentElem.getValue();
		if (StringHelper.containsNonWhitespace(commentText)) {
			handleSubscription(ureq);

			if (toBeUpdatedComment == null) {
				toBeUpdatedComment = handleCommentCreation(commentText, ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else {
				toBeUpdatedComment = commentAndRatingService.updateComment(toBeUpdatedComment, commentText);
				handleUpdateResult(ureq);
			}
		}
	}

	private void handleSubscription(UserRequest ureq) {
		if (publishingInformations != null) {
			if (subscribeOnce) {
				Subscriber subscriber = notificationsManager.getSubscriber(getIdentity(), publishingInformations.getContext());
				if (subscriber == null) {
					notificationsManager.subscribe(getIdentity(), publishingInformations.getContext(), publishingInformations.getData());
					fireEvent(ureq, new UserCommentsSubscribeNotificationsEvent());
				}
				subscribeOnce = false;
			}
			notificationsManager.markPublisherNews(publishingInformations.getContext(), null, false);
		}
	}

	private UserComment handleCommentCreation(String commentText, UserRequest ureq) {
		UserComment newComment;
		if (parentComment == null) {
			// Create new comment
			newComment = commentAndRatingService.createComment(getIdentity(), ores, resSubPath, commentText);
		} else {
			// Reply to parent comment
			newComment = commentAndRatingService.replyTo(parentComment, getIdentity(), commentText);
			if (newComment == null) {
				showError("comments.coment.reply.error");
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		}
		if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
			doFinalizeAttachmentUpload(commentAndRatingService.getCommentContainer(newComment));
		}
		return newComment;
	}

	private void handleUpdateResult(UserRequest ureq) {
		if (toBeUpdatedComment == null) {
			showError("comments.coment.update.error");
			fireEvent(ureq, Event.FAILED_EVENT);
		} else {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		cleanUpTempDir();
		cleanUp();
		super.doDispose();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		if (commentElem.isVisible()) {
			commentElem.setValue("");
			commentElem.clearError();
			toggleCommentFormElem();
			cleanUpTempDir();
			cleanUp();
			flc.setDirty(false);
		}
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @return the updated or newly created comment. Only package scope
	 */
	UserComment getComment() {
		return toBeUpdatedComment;
	}

	public boolean isCommentElemVisible() {
		return commentElem.isVisible();
	}
}
