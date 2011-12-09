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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.bookmark;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;

public abstract class BookmarkManager extends BasicManager {

	protected static BookmarkManager INSTANCE;
	/**
	 * The getInstance Method loads the real implementation via spring and returns
	 * it as a singleton. This is a convinience method.
	 * 
	 * @return Singleton.
	 */
	public static final synchronized BookmarkManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @param newBookmark
	 */
	public abstract void createAndPersistBookmark(Bookmark newBookmark);

	/**
	 * @param identity
	 * @return a List of found bookmarks of given subject
	 */
	public abstract List findBookmarksByIdentity(Identity identity);

	/**
	 * Finds bookmarks of a specific type for an identity
	 * 
	 * @param identity
	 * @param type
	 * @return list of bookmarks for this identity
	 */
	public abstract List findBookmarksByIdentity(Identity identity, String type);

	/**
	 * @param changedBookmark
	 * @return true if saved successfully
	 */
	public abstract void updateBookmark(Bookmark changedBookmark);

	/**
	 * @param deletableBookmark
	 * @return true if success
	 */
	public abstract void deleteBookmark(Bookmark deletableBookmark);

	/**
	 * calculates the URL for launching a bookmark
	 * 
	 * @param chosenBm
	 * @return resourceablea instance
	 */
	public abstract OLATResourceable getLaunchOlatResourceable(Bookmark chosenBm);

	/**
	 * Delete all bookmarks pointing to the given resourceable.
	 * 
	 * @param res
	 */
	public abstract void deleteAllBookmarksFor(OLATResourceable res);

	/**
	 * @param identity
	 * @param res
	 * @return true if resourceable is bookmarked
	 */
	public abstract boolean isResourceableBookmarked(Identity identity, OLATResourceable res);

	/**
	 * Delete all bookmarks for certain identity. 
	 * @see org.olat.user.UserDataDeletable#deleteUserData(org.olat.core.id.Identity)
	 */
	public abstract void deleteUserData(Identity identity, String newDeletedUserName);

	/**
	 * Launch the given bookmark
	 * @param bookmark
	 * @param ureq
	 * @param wControl
	 * @return TRUE: launch successful; FALSE: no launcher found (unknonwn bookmark type)
	 */
	public abstract boolean launchBookmark(Bookmark bookmark, UserRequest ureq, WindowControl wControl);

	/**
	 * Create a fully qualified URL that can be used to launch this bookmark e.g.
	 * from a browser bookmark or an RSS feed document
	 * 
	 * @param bookmark
	 * @return URL or NULL if not successful
	 */
	public abstract String createJumpInURL(Bookmark bookmark);

	/**
	 * Spring setter method
	 * @param newBookmarkHanlders
	 */
	public abstract void setBookmarkHandlers(List<BookmarkHandler> newBookmarkHanlders);

	

}