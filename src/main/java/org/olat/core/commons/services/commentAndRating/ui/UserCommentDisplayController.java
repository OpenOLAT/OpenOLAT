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
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.PublishingInformations;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
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
	private List<UserComment> allComments;
	private List<Controller> replyControllers;
	private UserComment userComment;
	// Delete workflow
	private Link deleteLink;
	private DialogBoxController deleteDialogCtr;
	public static final Event DELETED_EVENT = new Event("comment_deleted");
	public static final Event COMMENT_COUNT_CHANGED = new Event("comment_count_changed");
	public static final Event COMMENT_DATAMODEL_DIRTY = new Event("comment_datamode_dirty");
	// Reply workflow
	private Link replyLink;
	private CloseableModalController replyCmc;
	private UserCommentFormController replyCommentFormCtr;
	
	private final String resSubPath;
	private final OLATResourceable ores;
	private final PublishingInformations publishingInformations;
	
	@Autowired
	private UserManager userManager;
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
		userCommentDisplayVC.contextPut("formatter", Formatter.getInstance(getLocale()));
		userCommentDisplayVC.contextPut("securityCallback", securityCallback);
		userCommentDisplayVC.contextPut("comment", userComment);
		// Creator information
		TextComponent creator = TextFactory.createTextComponentFromI18nKey("creator", null, null, null, true, userCommentDisplayVC);
		String name = userManager.getUserDisplayName(userComment.getCreator());
		creator.setText(translate("comments.comment.creator", new String[]{name}));
		// Portrait
		if (CoreSpringFactory.containsBean(UserAvatarDisplayControllerCreator.class.getName())) {
			UserAvatarDisplayControllerCreator avatarControllerCreator = (UserAvatarDisplayControllerCreator) CoreSpringFactory.getBean(UserAvatarDisplayControllerCreator.class);
			Controller avatarCtr = avatarControllerCreator.createController(ureq, getWindowControl(), userComment.getCreator(), false, true);
			listenTo(avatarCtr);
			userCommentDisplayVC.put("avatarCtr", avatarCtr.getInitialComponent());
		}
		// Delete link
		if(securityCallback.canDeleteComment(userComment)) {
			deleteLink = LinkFactory.createCustomLink("deleteLink", "delete", "delete", Link.BUTTON_XSMALL, userCommentDisplayVC, this);
			deleteLink.setElementCssClass("o_delete");
		}
		// Reply link
		if(securityCallback.canReplyToComment(userComment)) {
			replyLink = LinkFactory.createCustomLink("replyLink", "reply", "comments.coment.reply", Link.BUTTON_XSMALL, userCommentDisplayVC, this);
			replyLink.setElementCssClass("o_reply");
		}
		//
		// Add all replies
		replyControllers = new ArrayList<>();
		buildReplyComments(ureq);
		userCommentDisplayVC.contextPut("replyControllers", replyControllers);
		//
		putInitialPanel(this.userCommentDisplayVC);
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
		for (Controller replyController : replyControllers) {
			removeAsListenerAndDispose(replyController);
		}
		replyControllers.clear();
		// Build replies again
		for (UserComment reply : allComments) {
			if (reply.getParent() == null) continue;
			if (reply.getParent().getKey().equals(userComment.getKey())) {
				// Create child controller
				UserCommentDisplayController replyCtr = new UserCommentDisplayController(ureq, getWindowControl(), reply, allComments,
						ores, resSubPath, securityCallback, publishingInformations);
				replyControllers.add(replyCtr);
				listenTo(replyCtr);
				userCommentDisplayVC.put(replyCtr.getViewCompName(), replyCtr.getInitialComponent());
			}
		}
	}

	@Override
	protected void doDispose() {
		// Child controllers disposed by basic controller
		replyControllers = null;
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == replyLink) {
			doReply(ureq);	
		} else if (source == deleteLink) {
			doConfirmDelete(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteDialogCtr) {
			boolean hasReplies = ((Boolean)deleteDialogCtr.getUserObject()).booleanValue();
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
				} else if (buttonPos == 1 && ! hasReplies) {		
					// Nothing to do, cancel button
				}
			}
			// Cleanup delete dialog
			removeAsListenerAndDispose(deleteDialogCtr);
			deleteDialogCtr = null;
			
		} else if (source == replyCmc) {
			// User closed modal dialog (cancel)
			cleanUp();
		} else if (source == replyCommentFormCtr) {
			// User Saved or canceled form
			replyCmc.deactivate();
			if (event == Event.CHANGED_EVENT) {
				// Update view
				UserComment newReply = replyCommentFormCtr.getComment();
				allComments.add(newReply);
				// Create child controller
				UserCommentDisplayController replyCtr = new UserCommentDisplayController(ureq, getWindowControl(), newReply, allComments, ores, resSubPath, securityCallback, publishingInformations);
				replyControllers.add(replyCtr);
				listenTo(replyCtr);
				userCommentDisplayVC.put(replyCtr.getViewCompName(), replyCtr.getInitialComponent());
				// notify parent
				fireEvent(ureq, COMMENT_COUNT_CHANGED);
			} else if (event == Event.FAILED_EVENT) {
				// Reply failed - reload everything
				fireEvent(ureq, COMMENT_DATAMODEL_DIRTY);						
			}
			cleanUp();
		} else if (source instanceof UserCommentDisplayController) {
			UserCommentDisplayController replyCtr = (UserCommentDisplayController) source;
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
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(replyCommentFormCtr);
		removeAsListenerAndDispose(replyCmc);
		replyCommentFormCtr = null;
		replyCmc = null;
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
		deleteDialogCtr = DialogBoxUIFactory.createGenericDialog(ureq, getWindowControl(), translate("comments.dialog.delete.title"), deleteText, buttonLabels);
		listenTo(deleteDialogCtr);
		deleteDialogCtr.activate();
		// Add replies info as user object to retrieve it later when evaluating the events
		deleteDialogCtr.setUserObject(Boolean.valueOf(hasReplies));
	}
	
	private void doReply(UserRequest ureq) {
		// Init reply workflow
		replyCommentFormCtr = new UserCommentFormController(ureq, getWindowControl(), userComment, null,
				ores, resSubPath, publishingInformations);
		listenTo(replyCommentFormCtr);
		
		String name = userManager.getUserDisplayName(userComment.getCreator());
		String title = translate("comments.coment.reply.title", new String[]{ name });
		replyCmc = new CloseableModalController(getWindowControl(), "close", replyCommentFormCtr.getInitialComponent(), true, title);
		listenTo(replyCmc);
		replyCmc.activate();	
	}
}
