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
package org.olat.core.commons.creator;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

/**
 * Description:<br>
 * Use this controller creator to create an avatar display controller for a
 * given identity. Configure your concrete implementation for the
 * UserAvatarDisplayControllerCreator spring bean.
 * 
 * <P>
 * Initial Date: 01.12.2009 <br>
 * 
 * @author gnaegi
 */
public abstract class UserAvatarDisplayControllerCreator {

	/**
	 * Create an avatar controller
	 * 
	 * @param ureq
	 * @param wControl
	 * @param identity
	 *            The avatar identity
	 * @param useLarge
	 *            true: a large image is used; small: a small image is used
	 * @param canLinkToHomePage
	 *            true: avatar is clickable and leads to more information about
	 *            user; false: avatar can't be clicked
	 * @return a controller that displays the users avatar
	 */
	public abstract Controller createController(UserRequest ureq,
			WindowControl wControl, Identity identity, boolean useLarge,
			boolean canLinkToHomePage);

}
