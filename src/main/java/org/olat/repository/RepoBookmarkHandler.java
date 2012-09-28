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
package org.olat.repository;

import java.util.Collections;

import org.olat.NewControllerFactory;
import org.olat.bookmark.Bookmark;
import org.olat.bookmark.BookmarkHandler;
import org.olat.bookmark.BookmarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;

/**
 * Description:<br>
 * Bookmark launch handler for repository entry bookmarks. Creates or activates
 * a dynamic tab and loads the olat resource from the repository entry into the
 * tab
 * 
 * <P>
 * Initial Date: 27.05.2008 <br>
 * 
 * @author gnaegi
 */
public class RepoBookmarkHandler implements BookmarkHandler {

	/**
	 * @see org.olat.bookmark.handler.BookmarkLaunchHandler#tryToLaunch(org.olat.bookmark.Bookmark, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean tryToLaunch(Bookmark bookmark, UserRequest ureq, WindowControl wControl) {
		OLATResourceable reores = BookmarkManager.getInstance().getLaunchOlatResourceable(bookmark);
		RepositoryManager rm = RepositoryManager.getInstance();
		
		RepositoryEntry re = rm.lookupRepositoryEntry(reores.getResourceableId());
		if (re == null) {
			// we can't launch this bookmark, exit with false
			return false;
		}
		// ok, this bookmark represents a repo entry, try to launch
		
		String businessPath = "[RepositoryEntry:" + re.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, wControl);
		return true; 
	}

	/**
	 * @see org.olat.bookmark.handler.BookmarkHandler#createJumpInURL(org.olat.bookmark.Bookmark)
	 */
	public String createJumpInURL(Bookmark bookmark) {
		OLATResourceable reores = BookmarkManager.getInstance().getLaunchOlatResourceable(bookmark);
		RepositoryManager rm = RepositoryManager.getInstance();
		
		RepositoryEntry re = rm.lookupRepositoryEntry(reores.getResourceableId());
		// only create jump in urls for bookmarks of type repo entry
		if (re != null) {
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(reores);
			String url = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
			return url;
		}
		return null;
	}

}
