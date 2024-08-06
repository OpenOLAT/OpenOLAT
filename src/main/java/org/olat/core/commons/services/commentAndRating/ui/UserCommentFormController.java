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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
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
 * <P>
 * Initial Date: 24.11.2009 <br>
 *
 * @author gnaegi
 */
public class UserCommentFormController extends FormBasicController {

	private final UserComment parentComment;
	private UserComment toBeUpdatedComment;
	private RichTextElement commentElem;
	private TextElement commentsPreElem;
	private FormSubmit submitButton;
	private FormCancel cancelButton;

	private final String resSubPath;
	private final OLATResourceable ores;
	private boolean subscribeOnce = true;
	private final PublishingInformations publishingInformations;

	private static final int MAX_COMMENT_LENGTH = 4000;

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
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.parentComment = parentComment;
		this.toBeUpdatedComment = toBeUpdatedComment;
		this.publishingInformations = publishingInformations;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (parentComment == null) {
			commentsPreElem = uifactory.addTextElement("commentsPreElem", null, 25, "", formLayout);
			commentsPreElem.setPlaceholderKey("comments.form.placeholder", null);
			commentsPreElem.addActionListener(FormEvent.ONCLICK);
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
		submitButton.setVisible(false);
		cancelButton = uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		cancelButton.setVisible(false);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		String commentText = commentElem.getValue();

		if (!StringHelper.containsNonWhitespace(commentText) || commentText.equals("<p></p>")) {
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
		if (source == commentsPreElem || (source == commentElem && event.equals(Event.CANCELLED_EVENT))) {
			toggleCommentFormElem();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	private void toggleCommentFormElem() {
		if (parentComment == null) {
			commentsPreElem.setVisible(!commentsPreElem.isVisible());
			commentElem.setVisible(!commentElem.isVisible());
			submitButton.setVisible(!submitButton.isVisible());
			cancelButton.setVisible(!cancelButton.isVisible());
			commentElem.setFocus(commentElem.isVisible());
		}
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
	protected void formCancelled(UserRequest ureq) {
		if (commentElem.isVisible()) {
			commentElem.setValue("");
			commentElem.clearError();
			toggleCommentFormElem();
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
