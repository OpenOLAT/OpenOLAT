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
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.commons.services.commentAndRating;

import org.olat.core.commons.services.commentAndRating.impl.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;

/**
 * Description:<br>
 * The comment and rating service offers GUI elements to comment and rate
 * OLATResourceable objects. The objects can be specified even more precise by
 * providing an optional sub path
 * <p>
 * Get an instance of this service by calling the ServiceCreatorFactory and then
 * call the init method to initialize the service for your resource:
 * 
 * <pre>
 * //to get the service either use coreSpringFactory.getBean(CommentAndRatingService.class) or when used in a singleton manager
 * implement the CommentAndRatingServiceFactory interface and see an example using the CommentAndRatingServiceFactory as the service is
 * stateful
 * if (commentAndRatingService != null) {
 * 	// initialize the service with the resource, an optional subpath (or NULL) and the administrative flag
 * 	commentAndRatingService.init(myOlatResource, myResourceSubPath, isAdmin);
 * 	// start using the service
 * 	UserCommentsController commentsCtr = commentAndRatingService.createUserCommentsController(ureq, getWindowControl());
 * }
 * </pre>
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public interface CommentAndRatingService {

	/**
	 * Initialize the service using CommentAndRatingDefaultSecurityCallback as
	 * security callback.
	 * 
	 * @param identity
	 * @param ores
	 * @param oresSubPath
	 * @param isAdmin
	 * @param isAnonymous
	 */
	public void init(Identity identity, OLATResourceable ores, String oresSubPath, boolean isAdmin, boolean isAnonymous);

	/**
	 * Initialize the service using your own security callback
	 * 
	 * @param ores
	 * @param oresSubPath
	 * @param securityCallback
	 */
	public void init(OLATResourceable ores, String oresSubPath, CommentAndRatingSecurityCallback securityCallback);

	/**
	 * Get the user comments manager. This is normally not used by code from other
	 * packages.
	 * 
	 * @return
	 */
	public UserCommentsManager getUserCommentsManager();

	/**
	 * Get the user ratings manager. This is normally not used by code from other packages.
	 * @return
	 */
	public UserRatingsManager getUserRatingsManager();
	
	/**
	 * Create a minimized user comments controller that only shows the number of
	 * comments. The link information can be clicked to trigger something
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public UserCommentsAndRatingsController createUserCommentsControllerMinimized(UserRequest ureq, WindowControl wControl);

	/**
	 * Create a user comments controller that show the number of comments. In the
	 * initial view the comments are not visible. When clicking on the number of
	 * comments the comments are displayed.
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public UserCommentsAndRatingsController createUserCommentsControllerExpandable(UserRequest ureq, WindowControl wControl);

	/**
	 * Create a user rating controller without the commenting functionality
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public UserCommentsAndRatingsController createUserRatingsController(UserRequest ureq, WindowControl wControl);

	/**
	 * Create a minimized user comments controller that only shows the number of
	 * comments and allows the user rate the resource on a scale of 1-5. The
	 * link information can be clicked to trigger something
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public UserCommentsAndRatingsController createUserCommentsAndRatingControllerMinimized(UserRequest ureq, WindowControl wControl);

	/**
	 * Create a user comments controller that show the number of comments and
	 * allows the user rate the resource on a scale of 1-5. In the initial view
	 * the comments are not visible. When clicking on the number of comments the
	 * comments are displayed.
	 * 
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public UserCommentsAndRatingsController createUserCommentsAndRatingControllerExpandable(UserRequest ureq, WindowControl wControl);

	/**
	 * Delete all comments and ratings for this resource and subpath
	 * configuration. See also the deleteAllIgnoringSubPath() method.
	 * 
	 * @param int number of deleted comments and ratings
	 */
	public int deleteAll();

	/**
	 * Delete all comments and ratings for this resource while ignoring the
	 * resource sub path. Use this when you delete the resource and not an element
	 * within the resource.
	 * 
	 * @param int number of deleted comments and ratings
	 */
	public int deleteAllIgnoringSubPath();

}
