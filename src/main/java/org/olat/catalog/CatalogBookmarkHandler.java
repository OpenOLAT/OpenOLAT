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
package org.olat.catalog;

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
import org.olat.core.util.resource.OresHelper;

/**
 * Description:<br>
 * Bookmark handler for catalog bookmarks: activates the repository entry site,
 * activates the catalog menu and then activates the catalog item with the given
 * id
 * 
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CatalogBookmarkHandler implements BookmarkHandler {

	/**
	 * @see org.olat.bookmark.BookmarkHandler#tryToLaunch(org.olat.bookmark.Bookmark, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean tryToLaunch(Bookmark bookmark, UserRequest ureq, WindowControl wControl) {
		OLATResourceable reores = BookmarkManager.getInstance().getLaunchOlatResourceable(bookmark);
		// only launch bookmarks of type catalog entry
		if(reores.getResourceableTypeName().equals(CatalogManager.CATALOGENTRY)){
			String businessPath = "[CatalogEntry:" + bookmark.getOlatreskey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, wControl);
			return true;
		}
		return false;
	}

	/**
	 * @see org.olat.bookmark.handler.BookmarkHandler#createJumpInURL(org.olat.bookmark.Bookmark)
	 */
	public String createJumpInURL(Bookmark bookmark) {
		OLATResourceable reores = BookmarkManager.getInstance().getLaunchOlatResourceable(bookmark);
		// only create jump in urls for bookmarks of type catalog entry
		if(reores.getResourceableTypeName().equals(CatalogManager.CATALOGENTRY)){	
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CatalogManager.CATALOGENTRY, bookmark.getOlatreskey());
			ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(ores);
			return BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		}
		return null;
	}

}
