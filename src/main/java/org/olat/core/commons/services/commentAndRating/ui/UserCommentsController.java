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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.Formatter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The user comments controller shows all user comments for a certain resource
 * and offers edit functionality
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>UserCommentDisplayController.COMMENT_COUNT_CHANGED</li>
 * </ul>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsController extends BasicController {

	// Configuration
	private final CommentAndRatingSecurityCallback securityCallback;
	// Data model
	private List<UserComment> allComments;
	private Long commentsCount;
	// GUI elements
	private final VelocityContainer userCommentsVC;
	private UserCommentFormController createCommentFormCtr;
	private List<Controller> commentControllers;
	
	private Object userObject;
	
	private final String resSubPath;
	private final OLATResourceable ores;
	private final PublishingInformations publishingInformations;
	
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	/**
	 * Constructor for a user comments controller. Use the
	 * CommentAndRatingService instead of calling this constructor directly!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param commentManager
	 * @param securityCallback
	 */
	public UserCommentsController(UserRequest ureq, WindowControl wControl,
			OLATResourceable ores, String resSubPath, PublishingInformations publishingInformations,
			CommentAndRatingSecurityCallback securityCallback) {
		super(ureq, wControl);
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.securityCallback = securityCallback;
		this.publishingInformations = publishingInformations;
		// Init view
		userCommentsVC = createVelocityContainer("userComments");
		userCommentsVC.contextPut("formatter", Formatter.getInstance(getLocale()));
		userCommentsVC.contextPut("securityCallback", securityCallback);
		// Add comments
		commentControllers = new ArrayList<>();
		userCommentsVC.contextPut("commentControllers", commentControllers);
		// Init datamodel and controllers
		commentsCount = commentAndRatingService.countComments(ores, resSubPath);
		buildTopLevelComments(ureq, true);
		// Add create form
		if (securityCallback.canCreateComments()) {
			removeAsListenerAndDispose(createCommentFormCtr);
			createCommentFormCtr = new UserCommentFormController(ureq, getWindowControl(), null, null,
					ores, resSubPath, publishingInformations);
			listenTo(createCommentFormCtr);
			userCommentsVC.put("createCommentFormCtr", createCommentFormCtr.getInitialComponent());
		}
		
		putInitialPanel(userCommentsVC);
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	public int getNumOfComments() {
		return commentControllers == null ? 0 : commentControllers.size();
	}
	
	public void scrollTo(Long commentId) {
		userCommentsVC.contextPut("goToComment", new ConsumableBoolean(true));
		userCommentsVC.contextPut("goToCommentId", commentId);
	}

	@Override
	protected void doDispose() {
		// Child controllers autodisposed by basic controller
		commentControllers = null;
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == createCommentFormCtr) {
			if (event == Event.CANCELLED_EVENT) {
				// do nothing
				fireEvent(ureq, event);
			} else if(event instanceof UserCommentsSubscribeNotificationsEvent) {
				fireEvent(ureq, event);
			} else if (event == Event.CHANGED_EVENT) {
				// Add new comment to view instead of rebuilding datamodel to reduce overhead
				UserComment newComment = createCommentFormCtr.getComment();
				allComments.add(newComment);
				commentsCount = commentAndRatingService.countComments(ores, resSubPath);
				if (allComments.size() != commentsCount.longValue()) {
					// Ups, we have also other changes in the datamodel, reload everything
					buildTopLevelComments(ureq, true);
				} else {
					// Create top level comment controller
					UserCommentDisplayController commentController = new UserCommentDisplayController(ureq, getWindowControl(), newComment, allComments,
							ores, resSubPath, securityCallback, publishingInformations);
					commentControllers.add(commentController);
					listenTo(commentController);
					userCommentsVC.put(commentController.getViewCompName(), commentController.getInitialComponent());
					// Rebuild new comment form
					removeAsListenerAndDispose(createCommentFormCtr);
					createCommentFormCtr = new UserCommentFormController(ureq, getWindowControl(), null, null,
							ores, resSubPath, publishingInformations);
					listenTo(createCommentFormCtr);
					userCommentsVC.put("createCommentFormCtr", createCommentFormCtr.getInitialComponent());					
				}
				// Notify parent about change
				fireEvent(ureq, UserCommentDisplayController.COMMENT_COUNT_CHANGED);
			}
		} else if (source instanceof UserCommentDisplayController) {
			UserCommentDisplayController commentCtr = (UserCommentDisplayController) source;
			if (event == UserCommentDisplayController.DELETED_EVENT) {
				// Remove comment from view
				commentControllers.remove(commentCtr);
				userCommentsVC.remove(commentCtr.getInitialComponent());
				removeAsListenerAndDispose(commentCtr);
				doCountChanged(ureq);
			} else if (event == UserCommentDisplayController.COMMENT_COUNT_CHANGED) {
				doCountChanged(ureq);
			} else if (event == UserCommentDisplayController.COMMENT_DATAMODEL_DIRTY) {
				// Reload everything to reflect changes made by other users and us
				commentsCount = commentAndRatingService.countComments(ores, resSubPath);
				buildTopLevelComments(ureq, true);
				// Notify parent
				fireEvent(ureq, UserCommentDisplayController.COMMENT_COUNT_CHANGED);
			}
		}
	}
	
	private void doCountChanged(UserRequest ureq) {
		// Sanity check: if number of comments is not the same as in our datamodel,
		// reload everything to reflect changes made by other users
		commentsCount = commentAndRatingService.countComments(ores, resSubPath);
		if (allComments.size() != commentsCount.longValue()) {
			buildTopLevelComments(ureq, true);
		}
		// Notify parent
		fireEvent(ureq, UserCommentDisplayController.COMMENT_COUNT_CHANGED);
	}
	

	/**
	 * Internal helper to build the view controller for the direct comments
	 * (without replies)
	 * 
	 * @param ureq
	 * @param initDatamodel true: load datamodel from database; false: just rebuild GUI with existing datamodel
	 */
	private void buildTopLevelComments(UserRequest ureq, boolean initDatamodel) {
		// First remove all old replies		
		for (Controller commentController : commentControllers) {
			removeAsListenerAndDispose(commentController);
		}
		commentControllers.clear();
		if (initDatamodel) {
			allComments = commentAndRatingService.getComments(ores, resSubPath);
		}
		// Build replies again
		for (UserComment comment : allComments) {
			if (comment.getParent() == null) {
				// Create top level comment controller
				UserCommentDisplayController commentController = new UserCommentDisplayController(ureq, getWindowControl(), comment, allComments,
						ores, resSubPath, securityCallback, publishingInformations);
				commentControllers.add(commentController);
				listenTo(commentController);
				userCommentsVC.put(commentController.getViewCompName(), commentController.getInitialComponent());
			}
		}
	}

	/**
	 * Helper method to access the current number of comments in the list
	 * displayed by this controller.
	 * 
	 * @return a number representing the number of comments viewable by this controller
	 */
	public long getCommentsCount() {
		return commentsCount.longValue();
	}
}