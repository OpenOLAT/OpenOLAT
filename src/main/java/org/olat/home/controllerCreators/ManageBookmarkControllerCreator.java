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
package org.olat.home.controllerCreators;

import org.olat.bookmark.ManageBookmarkController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;

/**
 * 
 * <h3>Description:</h3>
 * Wrapper to create the bookmarks in home
 * <p>
 * Initial Date:  29 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class ManageBookmarkControllerCreator extends AutoCreator  {

	/**
	 * @see org.olat.core.gui.control.creator.AutoCreator#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	public ManageBookmarkControllerCreator() {
		super();
	}	
	
	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(UserRequest ureq, WindowControl lwControl) {
		return new ManageBookmarkController(ureq, lwControl, true, ManageBookmarkController.SEARCH_TYPE_ALL);
	}

}
