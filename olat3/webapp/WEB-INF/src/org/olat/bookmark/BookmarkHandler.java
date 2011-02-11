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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2008 frentix GmbH,<br>
 * http://www.frentix.com, Switzerland.
 * <p>
 */
package org.olat.bookmark;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;

/**
 * Description:<br>
 * The bookmark launch handler implements the launching code of a bookmark. The
 * handler must check if the given bookmark must be handled by this handler. It
 * must only launch bookmarks that are launchable by this handler.
 * <P>
 * The handlers must be added to the appropriate spring config file. See the
 * RepoBookmarkHandler for an example
 * <P>
 * When you create a new bookmark handler, you should usually also implement a
 * JumpInHandlerFactory for this type of bookmark!
 * <P>
 * Initial Date: 28.05.2008 <br>
 * 
 * @author gnaegi
 */
public interface BookmarkHandler {

	/**
	 * Try to launch the given OLAT resource
	 * 
	 * @param bookmark The bookmark to be launched
	 * @param ureq. The current user request
	 * @param wControl The current window control
	 * @return TRUE: this handler was responsible for launching this bookmark.
	 *         Note that the launching process could have failed e.g. because the
	 *         bookmark resource does not exist anymore. FALSE: this handler is
	 *         not responsible for this bookmark, another handler should try it.
	 */
	public boolean tryToLaunch(Bookmark bookmark, UserRequest ureq, WindowControl wControl);

	/**
	 * Create a fully qualified URL that can be used to launch this bookmark e.g.
	 * from a browser bookmark or an RSS feed document
	 * 
	 * @param bookmark
	 * @return URL or NULL if not successful
	 */
	public String createJumpInURL(Bookmark bookmark);

}
