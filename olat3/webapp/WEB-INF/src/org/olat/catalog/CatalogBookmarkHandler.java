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
* Copyright (c) 2008 frentix GmbH,<br>
* http://www.frentix.com, Switzerland.
* <p>
*/
package org.olat.catalog;

import org.olat.bookmark.Bookmark;
import org.olat.bookmark.BookmarkHandler;
import org.olat.bookmark.BookmarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.site.RepositorySite;

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
			// set catalog param to same syntax as used in jumpin activation process
			DTabs dts = (DTabs) wControl.getWindowBackOffice().getWindow().getAttribute("DTabs");
			// encode sub view identifyer using ":" character
			dts.activateStatic(ureq, RepositorySite.class.getName(),"search.catalog:" + bookmark.getOlatreskey());		
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
			return CatalogJumpInHandlerFactory.buildRepositoryDispatchURI(bookmark.getOlatreskey());
		}
		return null;
	}

}
