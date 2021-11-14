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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
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
 * <P>
 * Initial Date: 24.11.2009 <br>
 *
 * @author gnaegi
 */
public class UserCommentFormController extends FormBasicController {
	private UserComment parentComment;
	private UserComment toBeUpdatedComment;
	private RichTextElement commentElem;

	private final String resSubPath;
	private final OLATResourceable ores;
	private boolean subscribeOnce = true;
	private PublishingInformations publishingInformations;

	@Autowired
	private UserManager userManager;
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
	 * @param commentManager
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
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// Add parent and parent first / last name
		if (parentComment != null) {
			String name = userManager.getUserDisplayName(parentComment.getCreator());
			String[] args = new String[] {name};
			setFormTitle("comments.form.reply.title", args);
		} else {
			setFormTitle("comments.form.new.title");
		}

		commentElem = uifactory.addRichTextElementForStringDataMinimalistic(
				"commentElem", null, "", -1, -1, formLayout, getWindowControl());
		commentElem.setMaxLength(4000);
		FormLayoutContainer buttonContainer = FormLayoutContainer
				.createButtonLayout("buttonContainer", getTranslator());
		formLayout.add(buttonContainer);

		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
		uifactory.addFormSubmitButton("submit", buttonContainer);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String commentText = commentElem.getValue();
		if(commentText.length() <= 4000) {
			commentElem.clearError();
		} else {
			commentElem.setErrorKey("input.toolong", new String[]{"4000"});
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String commentText = commentElem.getValue();
		if (StringHelper.containsNonWhitespace(commentText)) {
			if(publishingInformations != null) {
				if(subscribeOnce) {
					Subscriber subscriber = notificationsManager.getSubscriber(getIdentity(), publishingInformations.getContext());
					if(subscriber == null) {
						notificationsManager.subscribe(getIdentity(), publishingInformations.getContext(), publishingInformations.getData());
						fireEvent(ureq, new UserCommentsSubscribeNotificationsEvent());
					}
					subscribeOnce = false;
				}
				notificationsManager.markPublisherNews(publishingInformations.getContext(), null, false);
			}
			
			if (toBeUpdatedComment == null) {
				if (parentComment == null) {
					// create new comment
					toBeUpdatedComment = commentAndRatingService.createComment(getIdentity(), ores, resSubPath, commentText);
					// notify listeners that we finished.
					fireEvent(ureq, Event.CHANGED_EVENT);
				} else {
					// reply to parent comment
					toBeUpdatedComment = commentAndRatingService.replyTo(parentComment, getIdentity(), commentText);
					if (toBeUpdatedComment == null) {
						showError("comments.coment.reply.error");
						fireEvent(ureq, Event.FAILED_EVENT);
					} else {
						fireEvent(ureq, Event.CHANGED_EVENT);
					}
				}
			} else {
				toBeUpdatedComment = commentAndRatingService.updateComment(toBeUpdatedComment, commentText);
				if (toBeUpdatedComment == null) {
					showError("comments.coment.update.error");
					fireEvent(ureq, Event.FAILED_EVENT);
				} else {
					fireEvent(ureq, Event.CHANGED_EVENT);
				}
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	/**
	 * @return the updated or new created comment. Only package scope
	 */
	UserComment getComment() {
		return toBeUpdatedComment;
	}
}
