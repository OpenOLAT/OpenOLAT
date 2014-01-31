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
package org.olat.home.controllerCreators;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.AutoCreator;
import org.olat.core.gui.control.generic.portal.PortalMainController;


/**
 * 
 * <h3>Description:</h3>
 * Wrapper to create the notification in home
 * with a panel for quick jump to other areas of Olat
 * <p>
 * Initial Date:  29 nov. 2010 <br>
 * @author srosse, srosse@frentix.com, www.frentix.com
 */
public class HomePortalControllerCreator extends AutoCreator  {

	/**
	 * @see org.olat.core.gui.control.creator.AutoCreator#getClassName()
	 */
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}

	public HomePortalControllerCreator() {
		super();
	}	
	
	/**
	 * @see org.olat.core.gui.control.creator.ControllerCreator#createController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createController(UserRequest ureq, WindowControl lwControl) {
		return new PortalMainController(ureq, lwControl);
	}
	

}
