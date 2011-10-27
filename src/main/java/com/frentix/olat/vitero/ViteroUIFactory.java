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
package com.frentix.olat.vitero;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

import com.frentix.olat.vitero.ui.ViteroAdminController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ViteroUIFactory {

	/**
	 * Get a controller for admin-setup of Vitero Integration
	 * used directly over extension-config, therefore needs to be static
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public static Controller createViteroAdminController(UserRequest ureq, WindowControl wControl) {
		return new ViteroAdminController(ureq, wControl);
	}
}