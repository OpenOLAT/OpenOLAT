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
package org.olat.repository;

import org.olat.ControllerFactory;
import org.olat.bookmark.Bookmark;
import org.olat.bookmark.BookmarkHandler;
import org.olat.bookmark.BookmarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;

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
	private static OLog log = Tracing.createLoggerFor(RepoBookmarkHandler.class);

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
		if (!rm.isAllowedToLaunch(ureq, re)) {
			Translator trans = Util.createPackageTranslator(Bookmark.class, ureq.getLocale());
			wControl.setWarning(trans.translate("warn.cantlaunch"));
		} else {
			// get the OLAT resource from this repo entry
			OLATResourceable ores = re.getOlatResource();
			//was brasato:: DTabs dts = getWindowControl().getDTabs();
			DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
			DTab dt = dts.getDTab(ores);
			if (dt == null) {
				// does not yet exist -> create and add
				dt = dts.createDTab(ores, re, bookmark.getTitle());
				if (dt == null) {
					//ups? what happend here?
					log.warn("Could not create dTab for bookmark with title::" + bookmark.getTitle() + " and ores::" + ores);
					return true;
				}
				Controller launchController = ControllerFactory.createLaunchController(ores, null, ureq, dt.getWindowControl(), true);
				dt.setController(launchController);
				dts.addDTab(dt);
			}
			// null: do not activate to a certain view
			dts.activate(ureq, dt, null); 
		}												
		// in any case return true - this was a repo entry bookmark!
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
			return RepoJumpInHandlerFactory.buildRepositoryDispatchURI(re);
		}
		return null;
	}

}
