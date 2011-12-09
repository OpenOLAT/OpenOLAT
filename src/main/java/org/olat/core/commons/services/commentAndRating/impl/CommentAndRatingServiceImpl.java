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

package org.olat.core.commons.services.commentAndRating.impl;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.UserCommentsManager;
import org.olat.core.commons.services.commentAndRating.UserRatingsManager;
import org.olat.core.commons.services.commentAndRating.impl.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * User interface controller and manager factory for the comment and rating
 * service. This is a spring prototype. Use the init() methods after getting
 * your instance from spring to configure the service for your resource. (See
 * the interface for an code example)
 * <P>
 * Initial Date: 24.11.2009 <br>
 * 
 * @author gnaegi
 */
public class CommentAndRatingServiceImpl implements CommentAndRatingService {
	//
	private OLATResourceable ores;
	private String oresSubPath;
	private CommentAndRatingSecurityCallback secCallback;
	//
	private UserCommentsManager userCommentsManager;

	private UserRatingsManager userRatingsManager;
	
	/**
	 * [spring only]
	 */
	private CommentAndRatingServiceImpl() {
		//
	}

	public void setUserCommentsManager(UserCommentsManager userCommentsManager) {
		this.userCommentsManager = userCommentsManager;
	}
	
	public void setUserRatingsManager(UserRatingsManager userRatingsManager) {
		this.userRatingsManager = userRatingsManager;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#init(org.olat.core.id.Identity,
	 *      org.olat.core.id.OLATResourceable, java.lang.String, boolean,
	 *      boolean)
	 */
	public void init(Identity identity, OLATResourceable oRes,
			String oresSubP, boolean isAdmin, boolean isAnonymous) {
		CommentAndRatingSecurityCallback callback = new CommentAndRatingDefaultSecurityCallback(
				identity, isAdmin, isAnonymous);
		init(oRes, oresSubP, callback);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#init(org.olat.core.id.OLATResourceable,
	 *      java.lang.String,
	 *      org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback)
	 */
	public void init(OLATResourceable oRes, String oresSubP,
			CommentAndRatingSecurityCallback securityCallback) {
		if (this.ores != null) {
			throw new AssertException("Programming error - this Comment and Rating service is already used by another party. This is a spring prototype!");
		}
		this.ores = oRes;
		this.oresSubPath = oresSubP;
		this.secCallback = securityCallback;
		this.userCommentsManager.init(ores, oresSubPath);
		this.userRatingsManager.init(ores, oresSubPath);
	}

	/**
	 * 
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#createUserCommentsControllerMinimized(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public UserCommentsAndRatingsController createUserCommentsControllerMinimized(UserRequest ureq, WindowControl wControl) {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, false, false);
	}
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#createUserCommentsControllerExpandable(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public UserCommentsAndRatingsController createUserCommentsControllerExpandable(UserRequest ureq, WindowControl wControl) {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, false, true);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#createUserCommentsAndRatingControllerExpandable(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public UserCommentsAndRatingsController createUserCommentsAndRatingControllerExpandable(
			UserRequest ureq, WindowControl wControl) {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, true, true);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#createUserCommentsAndRatingControllerMinimized(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public UserCommentsAndRatingsController createUserCommentsAndRatingControllerMinimized(
			UserRequest ureq, WindowControl wControl) {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, true, true, false);
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#createUserRatingsController(org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public UserCommentsAndRatingsController createUserRatingsController(
			UserRequest ureq, WindowControl wControl) {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		return new UserCommentsAndRatingsController(ureq, wControl, ores, oresSubPath, secCallback, false, true, false);
	}

	
	
	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#deleteAll()
	 */
	public int deleteAll() {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		int delCount = getUserCommentsManager().deleteAllComments();
		delCount += getUserRatingsManager().deleteAllRatings();
		return delCount;
	}

	/**
	 * 
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#deleteAllIgnoringSubPath()
	 */
	public int deleteAllIgnoringSubPath() {
		if (ores == null || secCallback == null) {
			throw new AssertException(
					"CommentAndRatingService must be initialized first, call init method");
		}
		int delCount = getUserCommentsManager().deleteAllCommentsIgnoringSubPath();
		delCount += getUserRatingsManager().deleteAllRatingsIgnoringSubPath();
		return delCount;
	}


	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#getUserCommentsManager()
	 */
	public UserCommentsManager getUserCommentsManager() {
		return this.userCommentsManager;
	}

	/**
	 * @see org.olat.core.commons.services.commentAndRating.CommentAndRatingService#getUserRatingsManager()
	 */
	public UserRatingsManager getUserRatingsManager() {
		return this.userRatingsManager;
	}

	
}
