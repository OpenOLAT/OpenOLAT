/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.commons.services.commentAndRating.impl.ui;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.UserCommentsManager;
import org.olat.core.commons.services.commentAndRating.UserRatingsManager;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
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
import org.olat.core.logging.AssertException;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;

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
	// Configuration
	private final String oresSubPath;
	private final CommentAndRatingSecurityCallback securityCallback;
	private final VelocityContainer userCommentsAndRatingsVC;
	private final OLATResourceable USER_COMMENTS_AND_RATING_CHANNEL;
	private final boolean canExpandToFullView;
	private Object userObject;
	// Managers
	private UserCommentsManager commentManager;
	private UserRatingsManager userRatingsManager;
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

	/**
	 * Constructor for a user combined user comments and ratings controller. Use
	 * the CommentAndRatingService instead of calling this constructor directly!
	 * 
	 * @param ureq
	 * @param wControl
	 * @param ores
	 * @param oresSubPath
	 * @param securityCallback
	 * @param enableComments
	 * @param enableRatings
	 * @param canExpandToFullView
	 */
	public UserCommentsAndRatingsController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String oresSubPath,
			CommentAndRatingSecurityCallback securityCallback, boolean enableComments, boolean enableRatings, boolean canExpandToFullView) {
		super(ureq, wControl);
		this.oresSubPath = oresSubPath;
		this.securityCallback = securityCallback;
		this.userCommentsAndRatingsVC = createVelocityContainer("userCommentsAndRatings");
		this.canExpandToFullView = canExpandToFullView;
		putInitialPanel(userCommentsAndRatingsVC);
		// Add comments views
		if (enableComments && securityCallback.canViewComments()) {
			this.commentManager = UserCommentsManager.getInstance(ores, oresSubPath);
			this.userCommentsAndRatingsVC.contextPut("enableComments", Boolean.valueOf(enableComments));
			// Link with comments count to expand view
			this.commentsCountLink = LinkFactory.createLink("comments.count", this.userCommentsAndRatingsVC, this);
			this.commentsCountLink.setCustomEnabledLinkCSS("b_comments");
			this.commentsCountLink.setTooltip("comments.count.tooltip", false);
			// Init view with values from DB
			updateCommentCountView();
		}
		// Add ratings view
		this.userCommentsAndRatingsVC.contextPut("viewIdent", CodeHelper.getRAMUniqueID());
		if (enableRatings) {
			this.userRatingsManager = UserRatingsManager.getInstance(ores, oresSubPath);
			if (securityCallback.canRate()) {
				this.userCommentsAndRatingsVC.contextPut("enableRatings", Boolean.valueOf(enableRatings));
				ratingUserC = new RatingComponent("userRating", 0, RATING_MAX, true);
				ratingUserC.addListener(this);
				this.userCommentsAndRatingsVC.put("ratingUserC", ratingUserC);
				ratingUserC.setShowRatingAsText(true);
				ratingUserC.setTitle("rating.personal.title");
				ratingUserC.setCssClass("b_rating_personal");
			}
			
			if (securityCallback.canViewRatingAverage()) {				
				ratingAverageC = new RatingComponent("ratingAverageC", 0, RATING_MAX, false);
				ratingAverageC.addListener(this);
				this.userCommentsAndRatingsVC.put("ratingAverageC", ratingAverageC);
				ratingAverageC.setShowRatingAsText(true);
				ratingAverageC.setTitle("rating.average.title");
				ratingAverageC.setTranslateExplanation(false);
				ratingAverageC.setCssClass("b_rating_average");
			}
			// Init view with values from DB
			updateRatingView();

		}
		// Register to event channel for comments count change events
		USER_COMMENTS_AND_RATING_CHANNEL = ores;
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), USER_COMMENTS_AND_RATING_CHANNEL);

	}

	/**
	 * Method to manually expand the comments view
	 * 
	 * @param ureq
	 */
	public void expandComments(UserRequest ureq) {
		if (!canExpandToFullView) { throw new AssertException("Can not expand messages when controller initialized as not expandable"); }
		commentsCtr = new UserCommentsController(ureq, getWindowControl(), commentManager, securityCallback);
		listenTo(commentsCtr);
		userCommentsAndRatingsVC.put("commentsCtr", commentsCtr.getInitialComponent());
		isExpanded = true;
		// Update our counter view in case changed since last loading
		if (getCommentsCount() != commentsCtr.getCommentsCount()) {
			updateCommentCountView();			
		}
	}

	/**
	 * Method to manually collapse the comments view
	 * 
	 * @param ureq
	 */
	public void collapseComments(UserRequest ureq) {
		if (!canExpandToFullView) { throw new AssertException("Can not collapse messages when controller initialized as not expandable"); }
		userCommentsAndRatingsVC.remove(commentsCtr.getInitialComponent());
		removeAsListenerAndDispose(commentsCtr);
		commentsCtr = null;
		isExpanded = false;
	}

	/**
	 * Package helper method to update the comment count view
	 */
	void updateCommentCountView() {
		if (this.commentsCountLink != null) {
			this.commentsCount = this.commentManager.countComments();
			this.commentsCountLink.setCustomDisplayText(translate("comments.count", commentsCount.toString()));			
		}
	}
	
	/**
	 * Package helper to update the rating view
	 */
	void updateRatingView() {
		if (ratingUserC != null) {
			userRating = userRatingsManager.getRating(getIdentity());
			if (userRating != null) {
				ratingUserC.setCurrentRating(userRating.getRating());				
			}
		}
		if (ratingAverageC != null) {
			ratingAverageC.setCurrentRating(userRatingsManager.calculateRatingAverage());
			long ratingsCounter = this.userRatingsManager.countRatings();			
			ratingAverageC.setExplanation(translate("rating.average.explanation", ratingsCounter + ""));
		}		
	}

	/**
	 * Package method to get current number of
	 * 
	 * @return
	 */
	long getCommentsCount() {
		return commentsCount.longValue();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// Remove event listener
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, USER_COMMENTS_AND_RATING_CHANNEL);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Forward comments counter links to parent listeners
		if (source == commentsCountLink) {
			if (canExpandToFullView) {
				if (isExpanded) {
					// Collapse
					collapseComments(ureq);
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
				userRating = userRatingsManager.createRating(ureq.getIdentity(), newRating);
			} else {
				// Update existing rating
				userRating = userRatingsManager.updateRating(userRating, newRating);
			}
			// Update GUI
			updateRatingView();
			// Notify other user who also have this component
			UserRatingChangedEvent changedEvent = new UserRatingChangedEvent(this, this.oresSubPath);
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedEvent, USER_COMMENTS_AND_RATING_CHANNEL);

		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == commentsCtr) {
			if (event == UserCommentDisplayController.COMMENT_COUNT_CHANGED) {
				updateCommentCountView();
				// notify other user who also have this component
				UserCommentsCountChangedEvent changedEvent = new UserCommentsCountChangedEvent(this, this.oresSubPath);
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(changedEvent, USER_COMMENTS_AND_RATING_CHANNEL);
			}
		}
	}

	/**
	 * Store a user object in this controller that can be retrieved in a later
	 * stage when a workflow is done
	 * 
	 * @param userObject
	 */
	public void addUserObject(Object userObject) {
		this.userObject = userObject;
	}
	
	/**
	 * Get the user object associated with this controller
	 * @return
	 */
	public Object getUserObject() {
		return this.userObject;
	}

	@Override
	public void event(Event event) {
		if (event instanceof UserCommentsCountChangedEvent) {
			UserCommentsCountChangedEvent changedEvent = (UserCommentsCountChangedEvent) event;
			if (!changedEvent.isSentByMyself(this) && !canExpandToFullView) {
				// Update counter in GUI, but only when in minimized mode (otherwise might confuse user)
				if ( (this.oresSubPath == null && changedEvent.getOresSubPath() == null)
					|| this.oresSubPath.equals(changedEvent.getOresSubPath()) ) {
					updateCommentCountView();					
				}
			}
		} else if (event instanceof UserRatingChangedEvent) {
			UserRatingChangedEvent changedEvent = (UserRatingChangedEvent) event;
			// Update rating in GUI
			if (!changedEvent.isSentByMyself(this)) {
				if ( (this.oresSubPath == null && changedEvent.getOresSubPath() == null)
					|| this.oresSubPath.equals(changedEvent.getOresSubPath()) ) {
					updateRatingView();
				}
			}
		}		
	}

}
