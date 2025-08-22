/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.commentAndRating.ui;

import java.util.List;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.commons.services.notifications.PublishingInformations;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.commons.services.notifications.ui.PublisherDecorated;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.rating.RatingComponent;
import org.olat.core.gui.components.rating.RatingType;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
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
public class UserCommentsAndRatingsController extends BasicController implements GenericEventListener, Activateable2 {

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
	private UserCommentsController commentsCtrl;

	// Ratings
	private RatingComponent ratingUserC;
	private RatingComponent ratingAverageC;
	private UserRating userRating;

	// Controller state
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
		this.canExpandToFullView = canExpandToFullView;
		userCommentsAndRatingsVC = createVelocityContainer("userCommentsAndRatings");
		putInitialPanel(userCommentsAndRatingsVC);

		// Add comments views
		if (enableComments && securityCallback.canViewComments()) {
			initCommentsView(enableComments);
		}

		// Add ratings view
		initRatingsView(enableRatings);

		// Initialize subscription controller
		initSubscriptionCtrl(ureq);

		// Initialize comments controller if comments are enabled
		if (enableComments) {
			initCommentsCtrl(ureq);
		}

		// Register to event channel for comments count change events
		userAndCommentsRatingsChannel = ores;
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), userAndCommentsRatingsChannel);
	}

	private void initCommentsView(boolean enableComments) {
		userCommentsAndRatingsVC.contextPut("enableComments", Boolean.valueOf(enableComments));
		// Link with comments count to expand view
		commentsCountLink = LinkFactory.createLink("comments.count", userCommentsAndRatingsVC, this);
		commentsCountLink.setAriaRole(Link.ARIA_ROLE_BUTTON);
		commentsCountLink.setTitle("comments.count.tooltip");
		commentsCountLink.setDomReplacementWrapperRequired(false);
		// Init view with values from DB
		updateCommentCountView();
	}

	private void initRatingsView(boolean enableRatings) {
		userCommentsAndRatingsVC.contextPut("viewIdent", CodeHelper.getRAMUniqueID());
		userCommentsAndRatingsVC.contextPut("enableRatings", Boolean.valueOf(enableRatings));

		if (enableRatings) {
			if (securityCallback.canRate()) {
				ratingUserC = new RatingComponent("userRating", RatingType.stars, 0, RATING_MAX, true);
				ratingUserC.addListener(this);
				userCommentsAndRatingsVC.put("ratingUserC", ratingUserC);
				ratingUserC.setShowRatingAsText(true);
				ratingUserC.setTitle("rating.personal.title");
				ratingUserC.setCssClass("o_rating_personal");
			}

			if (securityCallback.canViewRatingAverage()) {
				ratingAverageC = new RatingComponent("ratingAverageC", RatingType.stars, 0, RATING_MAX, false);
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
	}

	private void initSubscriptionCtrl(UserRequest ureq) {
		if (publishingInformations != null) {
			if(publishingInformations.publisher() != null) {
				PublisherDecorated decoratedPublisher = new PublisherDecorated(publishingInformations.publisher(),
						PublisherDecorated.DEFAULT_SUBSCRIBE_I18N, false);
				subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(),
						List.of(decoratedPublisher), false);
			} else {
				subscriptionCtrl = new ContextualSubscriptionController(ureq, getWindowControl(),
						publishingInformations.context(), publishingInformations.data());
			}
			if(subscriptionCtrl != null) {
				listenTo(subscriptionCtrl);
				userCommentsAndRatingsVC.put("subscription", subscriptionCtrl.getInitialComponent());
			}
		}
	}

	private void initCommentsCtrl(UserRequest ureq) {
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, oresSubPath, publishingInformations, securityCallback);
		listenTo(commentsCtrl);
		userCommentsAndRatingsVC.put("commentsCtrl", commentsCtrl.getInitialComponent());

		// Update our counter view in case changed since last loading
		if (getCommentsCount() != commentsCtrl.getCommentsCount()) {
			updateCommentCountView();
		}
	}
	
	public void scrollToCommentsArea() {
		commentsCtrl.scrollToCommentsArea();
	}

	/**
	 * Package helper method to update the comment count view
	 */
	void updateCommentCountView() {
		if (commentsCountLink != null) {
			commentsCount = commentAndRatingService.countComments(ores, oresSubPath);
			commentsCountLink.setCustomDisplayText(translate("comments.count", commentsCount.toString()));		
			String css = commentsCount > 0l ? "o_icon o_icon_comments o_icon-lg" : "o_icon o_icon_comments_none o_icon-lg";
			commentsCountLink.setCustomEnabledLinkCSS("o_comments");
			commentsCountLink.setIconLeftCSS(css);
			
			String legendI18n = commentsCount > 1l ? "comments.legend.plural" : "comments.legend.singular";
			userCommentsAndRatingsVC.contextPut("commentsCountLegend", translate(legendI18n, commentsCount.toString()));
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("comment".equalsIgnoreCase(type) && commentsCtrl != null) {
			commentsCtrl.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Forward comments counter links to parent listeners
		if (source == commentsCountLink) {
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
		if (source == commentsCtrl) {
			if (event == UserCommentDisplayController.COMMENT_COUNT_CHANGED) {
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
		if (event instanceof UserCommentsCountChangedEvent changedEvent) {
			if (!changedEvent.isSentByMyself(this) && !canExpandToFullView) {
				// Update counter in GUI, but only when in minimized mode (otherwise might confuse user)
				if ( (oresSubPath == null && changedEvent.getOresSubPath() == null)
					|| (oresSubPath != null && oresSubPath.equals(changedEvent.getOresSubPath()))) {
					updateCommentCountView();					
				}
			}
		} else if (event instanceof UserRatingChangedEvent changedEvent) {
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
