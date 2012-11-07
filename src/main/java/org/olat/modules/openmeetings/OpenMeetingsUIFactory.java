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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.openmeetings.ui.OpenMeetingsAdminController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 nov. 2012<br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OpenMeetingsUIFactory {

	/**
	 * Get a controller for admin-setup of OpenMeetings Integration
	 * used directly over extension-config, therefore needs to be static
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createAdminController(UserRequest ureq, WindowControl wControl) {
		return new OpenMeetingsAdminController(ureq, wControl);
	}
}