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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The user comments and rating controller displays a minimized view of the
 * comments and rating with the option to expand to full view. Use this
 * controller whenever you want a resource to be commented.
 * <p>
 * Events fired by this controller:
 * <ul>
 * <li>UserCommentsAndRatingsController.EVENT_COMMENT_LINK_CLICKED when user clicked the comments link</li>
 * <li>UserCommentsAndRatingsController.EVENT_RATING_CHANGED when user changed the rating</li>
 * </ul>
 * <P>
 * Initial Date: 30.11.2009 <br>
 * 
 * @author gnaegi
 */
public class UserCommentsAndRatingsController extends BasicController implements GenericEventListener {
	private static final int RATING_MAX = 5;
	// Events
	public static final Event EVENT_COMMENT_LINK_CLICKED = new Event("comment_link_clicked");
	public static final Event EVENT_RATING_CHANGED = new Event("rating_changed");

	private final VelocityContainer userCommentsAndRatingsVC;
	private final OLATResourceable userAndCommentsRatingsChannel;
	private final boolean canExpandToFullView;
	private Object userObject;
	// Comments 
	private Link commentsCountLink;
	private Long commentsCount;
	private UserCommentsController commentsCtr;
	// Ratings
	private RatingComponent ratingUserC;
	private RatingComponent ratingAverageC;
	private UserRating userRating;
	// Controller state
	private boolean isExpanded = false; // default
	private ContextualSubscriptionController subscriptionCtrl;
	
	// Configuration
	private final String oresSubPath;
	private final OLATResourceable ores;
	private final PublishingInformations publishingInformations;
	private final CommentAndRatingSecurityCallback securityCallback;
	
	@Autowired
	private CommentAndRatingService commentAndRatingService;

	/**
	 * Constructor for a user combined user comments and ratings controller. Use
	 * the CommentAndRatingService instead of calling this constructor directly!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param oresSubPath
	 * @param securityCallback
	 * @param publishingInformations Informations for the notifications button (can be null)
	 * @param enableComments
	 * @param enableRatings
	 * @param canExpandToFullView
	 */
	public UserCommentsAndRatingsController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String oresSubPath,
			CommentAndRatingSecurityCallback securityCallback, PublishingInformations publishingInformations,
			boolean enableComments, boolean enableRatings, boolean canExpandToFullView) {
		super(ureq, wControl);
		this.ores = ores;
		this.oresSubPath = oresSubPath;
		this.securityCallback = securityCallback;
		this.publishingInformations = publishingInformations;
		userCommentsAndRatingsVC = createVelocityContainer("userCommentsAndRatings");
		this.canExpandToFullView = canExpandToFullView;
		putInitialPanel(userCommentsAndRatingsVC);
		// Add comments views
		if (enableComments && securityCallback.canViewComments()) {
			userCommentsAndRatingsVC.contextPut("enableComments", Boolean.valueOf(enableComments));
			// Link with comments count to expand view
			commentsCountLink = LinkFactory.createLink("comments.count", userCommentsAndRatingsVC, this);
			commentsCountLink.setTitle("comments.count.tooltip");
			commentsCountLink.setDomReplacementWrapperRequired(false);
			// Init view with values from DB
			updateCommentCountView();
		}
		// Add ratings view
		userCommentsAndRatingsVC.contextPut("viewIdent", CodeHelper.getRAMUniqueID());
		userCommentsAndRatingsVC.contextPut("enableRatings", Boolean.valueOf(enableRatings));
		if (enableRatings) {
			if (securityCallback.canRate()) {
				ratingUserC = new RatingComponent("userRating", 0, RATING_MAX, true);
				ratingUserC.addListener(this);
				userCommentsAndRatingsVC.put("ratingUserC", ratingUserC);
				ratingUserC.setShowRatingAsText(true);
				ratingUserC.setTitle("rating.personal.title");
				ratingUserC.setCssClass("o_rating_personal");
			}
			
			if (securityCallback.canViewRatingAverage()) {				
				ratingAverageC = new RatingComponent("ratingAverageC", 0, RATING_MAX, false);
				ratingAverageC.addListener(this);
				userCommentsAndRatingsVC.put("ratingAverageC", ratingAverageC);
				ratingAverageC.setShowRatingAsText(true);
				ratingAverageC.setTitle("rating.average.title");
				ratingAverageC.setTranslateExplanation(false);
				ratingAverageC.setCssClass("o_rating_average");
			}
			// Init view with values from DB
			updateRatingView();
		}
		
		if (publishingInformations != null) {
			subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(),
					publishingInformations.getContext(), publishingInformations.getData());
			listenTo(subscriptionCtrl);
			userCommentsAndRatingsVC.put("subscription", subscriptionCtrl.getInitialComponent());
		}
		
		// Register to event channel for comments count change events
		userAndCommentsRatingsChannel = ores;
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), userAndCommentsRatingsChannel);

	}

	/**
	 * Method to manually expand the comments view
	 * 
	 * @param ureq
	 */
	public void expandComments(UserRequest ureq) {
		if (canExpandToFullView) { 
			commentsCtr = new UserCommentsController(ureq, getWindowControl(), ores, oresSubPath, publishingInformations, securityCallback);
			listenTo(commentsCtr);
			userCommentsAndRatingsVC.put("commentsCtr", commentsCtr.getInitialComponent());
			isExpanded = true;
			// Update our counter view in case changed since last loading
			if (getCommentsCount() != commentsCtr.getCommentsCount()) {
				updateCommentCountView();			
			}
		}
	}
	
	public void expandCommentsAt(UserRequest ureq, Long commentId) {
		expandComments(ureq);
		commentsCtr.scrollTo(commentId);
	}

	/**
	 * Method to manually collapse the comments view
	 * 
	 * @param ureq
	 */
	public void collapseComments() {
		if (canExpandToFullView) {
			userCommentsAndRatingsVC.remove(commentsCtr.getInitialComponent());
			removeAsListenerAndDispose(commentsCtr);
			commentsCtr = null;
			isExpanded = false;
		}
	}

	/**
	 * Package helper method to update the comment count view
	 */
	void updateCommentCountView() {
		if (commentsCountLink != null) {
			commentsCount = commentAndRatingService.countComments(ores, oresSubPath);
			commentsCountLink.setCustomDisplayText(translate("comments.count", commentsCount.toString()));		
			String css = commentsCount > 0 ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
			commentsCountLink.setCustomEnabledLinkCSS("o_comments");
			commentsCountLink.setIconLeftCSS(css);
		}
	}
	
	/**
	 * Package helper to update the rating view
	 */
	void updateRatingView() {
		if (ratingUserC != null) {
			userRating = commentAndRatingService.getRating(getIdentity(), ores, oresSubPath);
			if (userRating != null) {
				ratingUserC.setCurrentRating(userRating.getRating());				
			}
		}
		if (ratingAverageC != null) {
			ratingAverageC.setCurrentRating(commentAndRatingService.calculateRatingAverage(ores, oresSubPath));
			long ratingsCounter = commentAndRatingService.countRatings(ores, oresSubPath);			
			ratingAverageC.setExplanation(translate("rating.average.explanation", ratingsCounter + ""));
		}		
	}

	/**
	 * Package method to get current number of
	 * 
	 * @return
	 */
	public long getCommentsCount() {
		if (commentsCount != null) {			
			return commentsCount.longValue();
		} else {
			return 0l;
		}
	}

	@Override
	protected void doDispose() {
		// Remove event listener
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, userAndCommentsRatingsChannel);
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Forward comments counter links to parent listeners
		if (source == commentsCountLink) {
			if (canExpandToFullView) {
				if (isExpanded) {
					// Collapse
					collapseComments();
				} else {
					// Expand now
					expandComments(ureq);
				}
			}
			fireEvent(ureq, EVENT_COMMENT_LINK_CLICKED);

		} else if (source == ratingUserC) {
			// Update user rating - convert component floats to integers (only discrete values possible)
			Integer newRating = Float.valueOf(ratingUserC.getCurrentRating()).intValue();
			if (userRating == null) {
				// Create new rating
				userRating = commentAndRatingService.createRating(getIdentity(), ores, oresSubPath, newRating);
			} else {
				// Update existing rating
				userRating = commentAndRatingService.updateRating(userRating, newRating);
			}
			// Update GUI
			updateRatingView();
			// Notify other user who also have this component
			UserRatingChangedEvent changedEvent = new UserRatingChangedEvent(this, this.oresSubPath);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedEvent, userAndCommentsRatingsChannel);

		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == commentsCtr) {
			if(event instanceof UserCommentsSubscribeNotificationsEvent) {
				if(subscriptionCtrl != null && !subscriptionCtrl.isSubscribed()) {
					subscriptionCtrl.loadModel();
				}
			} else if (event == UserCommentDisplayController.COMMENT_COUNT_CHANGED) {
				updateCommentCountView();
				// notify other user who also have this component
				UserCommentsCountChangedEvent changedEvent = new UserCommentsCountChangedEvent(this, oresSubPath);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedEvent, userAndCommentsRatingsChannel);
			} else if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	/**
	 * Store a user object in this controller that can be retrieved in a later
	 * stage when a workflow is done
	 * 
	 * @param userObject
	 */
	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	/**
	 * Get the user object associated with this controller
	 * @return
	 */
	public Object getUserObject() {
		return userObject;
	}

	@Override
	public void event(Event event) {
		if (event instanceof UserCommentsCountChangedEvent) {
			UserCommentsCountChangedEvent changedEvent = (UserCommentsCountChangedEvent) event;
			if (!changedEvent.isSentByMyself(this) && !canExpandToFullView) {
				// Update counter in GUI, but only when in minimized mode (otherwise might confuse user)
				if ( (oresSubPath == null && changedEvent.getOresSubPath() == null)
					|| (oresSubPath != null && oresSubPath.equals(changedEvent.getOresSubPath()))) {
					updateCommentCountView();					
				}
			}
		} else if (event instanceof UserRatingChangedEvent) {
			UserRatingChangedEvent changedEvent = (UserRatingChangedEvent) event;
			// Update rating in GUI
			if (!changedEvent.isSentByMyself(this)) {
				if ( (oresSubPath == null && changedEvent.getOresSubPath() == null)
					|| (oresSubPath != null && oresSubPath.equals(changedEvent.getOresSubPath()))) {
					updateRatingView();
				}
			}
		}		
	}

}
